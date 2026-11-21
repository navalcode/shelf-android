package dev.navalcode.shelf.data

import com.google.gson.annotations.SerializedName
import dev.navalcode.shelf.domain.Book
import java.io.IOException
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenLibraryApi {
    @GET("search.json")
    suspend fun search(
        @Query("q") query: String,
        @Query("limit") limit: Int = 20,
        @Query("fields") fields: String = "key,title,author_name,first_publish_year,edition_count",
    ): SearchDto
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
        val docs = api.search(query).docs ?: throw IOException("Missing search document list")
        val books = docs.map { dto ->
            dto ?: throw IOException("Null search document")
            val id = dto.key?.takeIf { it.matches(Regex("/works/OL[0-9]+W")) }
                ?: throw IOException("Invalid work identifier")
            val title = dto.title?.trim()?.takeIf { it.isNotEmpty() }
                ?: throw IOException("Missing book title")
            Book(id, title, dto.authors.orEmpty().mapNotNull { it?.trim()?.takeIf(String::isNotEmpty) },
                dto.year, dto.editions?.takeIf { it >= 0 })
        }
        books.distinctBy { it.id }
    }
}

fun openLibraryApi(builder: Retrofit.Builder = Retrofit.Builder().baseUrl("https://openlibrary.org/")): OpenLibraryApi =
    builder.addConverterFactory(GsonConverterFactory.create()).build().create(OpenLibraryApi::class.java)
