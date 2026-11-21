package dev.navalcode.shelf.data

import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.MalformedJsonException
import dev.navalcode.shelf.domain.Book
import dev.navalcode.shelf.domain.SearchException
import dev.navalcode.shelf.domain.SearchFailureKind
import java.io.EOFException
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val SEARCH_LIMIT = 20

interface OpenLibraryApi {
    @GET("search.json")
    suspend fun search(
        @Query("q") query: String,
        @Query("limit") limit: Int = SEARCH_LIMIT,
        @Query("fields") fields: String = "key,title,author_name,first_publish_year,edition_count",
    ): Response<SearchDto>
}

data class SearchDto(@SerializedName("docs") val docs: List<BookDto?>? = null)

data class BookDto(
    @SerializedName("key") val key: String? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("author_name") val authors: List<String?>? = null,
    @SerializedName("first_publish_year") val year: Int? = null,
    @SerializedName("edition_count") val editions: Int? = null,
)

/** Serializes requests and spaces their start times by at least 1.1 seconds. */
class OpenLibrarySource(
    private val api: OpenLibraryApi,
    private val clock: MonotonicClock,
    private val intervalMillis: Long = 1_100,
) : BookSource {
    private val gate = Mutex()
    private var lastStart: Long? = null

    override suspend fun search(query: String): List<Book> = gate.withLock {
        lastStart?.let { delay((intervalMillis - (clock.millis() - it)).coerceAtLeast(0)) }
        lastStart = clock.millis()
        val response =
            try {
                api.search(query)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: JsonParseException) {
                throw SearchException(SearchFailureKind.INVALID_DATA, error)
            } catch (error: MalformedJsonException) {
                throw SearchException(SearchFailureKind.INVALID_DATA, error)
            } catch (error: EOFException) {
                throw SearchException(SearchFailureKind.INVALID_DATA, error)
            } catch (error: IOException) {
                throw SearchException(SearchFailureKind.NETWORK, error)
            }
        if (!response.isSuccessful) {
            throw SearchException(
                if (response.code() == 429) SearchFailureKind.RATE_LIMIT
                else SearchFailureKind.SERVICE,
            )
        }
        val docs = response.body()?.docs ?: throw SearchException(SearchFailureKind.INVALID_DATA)
        val books =
            docs.take(SEARCH_LIMIT).map { dto ->
                dto ?: throw SearchException(SearchFailureKind.INVALID_DATA)
                val id =
                    dto.key?.takeIf { it.matches(Regex("/works/OL[0-9]+W")) }
                        ?: throw SearchException(SearchFailureKind.INVALID_DATA)
                val title =
                    dto.title?.trim()?.takeIf { it.isNotEmpty() }
                        ?: throw SearchException(SearchFailureKind.INVALID_DATA)
                Book(
                    id,
                    title,
                    dto.authors.orEmpty().mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) },
                    dto.year,
                    dto.editions?.takeIf { it >= 0 },
                )
            }
        books.distinctBy { it.id }
    }
}

fun openLibraryApi(
    builder: Retrofit.Builder = Retrofit.Builder().baseUrl("https://openlibrary.org/"),
): OpenLibraryApi =
    builder
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenLibraryApi::class.java)
