package dev.navalcode.shelf.domain

import java.util.Locale

data class Book(
    val id: String,
    val title: String,
    val authors: List<String>,
    val firstPublished: Int?,
    val editionCount: Int?,
)

enum class ResultSource {
    NETWORK,
    CACHE,
}

data class SearchResult(val books: List<Book>, val source: ResultSource)

/** Searches may throw SearchException; coroutine cancellation is always propagated unchanged. */
fun interface BookRepository {
    suspend fun search(query: String): SearchResult
}

/** Identical canonical keys reach both the API and the per-query cache. */
fun canonicalQuery(input: String): String =
    input.trim().replace(Regex("\\s+"), " ").lowercase(Locale.ROOT)

class SearchBooks(private val repository: BookRepository) {
    suspend operator fun invoke(input: String): SearchResult {
        val query = canonicalQuery(input)
        require(query.isNotEmpty() && query.length <= MAX_QUERY_LENGTH)
        return repository.search(query)
    }

    companion object {
        const val MAX_QUERY_LENGTH = 120
    }
}

/** Stable application errors; transport and parser details belong to Data. */
enum class SearchFailureKind {
    NETWORK,
    RATE_LIMIT,
    SERVICE,
    INVALID_DATA,
}

class SearchException(val kind: SearchFailureKind, cause: Throwable? = null) :
    Exception(kind.name, cause)
