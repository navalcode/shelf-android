package dev.navalcode.shelf.data

import dev.navalcode.shelf.domain.Book
import dev.navalcode.shelf.domain.BookRepository
import dev.navalcode.shelf.domain.ResultSource
import dev.navalcode.shelf.domain.SearchBooks
import dev.navalcode.shelf.domain.SearchResult
import dev.navalcode.shelf.domain.canonicalQuery
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun interface BookSource {
    suspend fun search(query: String): List<Book>
}

fun interface MonotonicClock {
    fun millis(): Long
}

/** No network call under the cache lock. Cancellation is never turned into a value. */
class CachedBookRepository(
    private val source: BookSource,
    private val clock: MonotonicClock,
    private val ttlMillis: Long = 180_000,
    private val capacity: Int = 20,
) : BookRepository {
    init {
        require(ttlMillis > 0 && capacity > 0)
    }

    private data class Entry(val books: List<Book>, val storedAt: Long)

    private val entries = LinkedHashMap<String, Entry>(16, 0.75f, true)
    private val mutex = Mutex()

    override suspend fun search(query: String): SearchResult {
        val key = canonicalQuery(query)
        require(key.isNotEmpty() && key.length <= SearchBooks.MAX_QUERY_LENGTH)
        currentCoroutineContext().ensureActive()
        val cached = mutex.withLock {
            val entry = entries[key]
            if (entry != null && clock.millis() - entry.storedAt < ttlMillis) entry.books
            else {
                entries.remove(key)
                null
            }
        }
        if (cached != null) return SearchResult(cached, ResultSource.CACHE)
        val books = source.search(key).map { it.copy(authors = it.authors.toList()) }.toList()
        // Also protects against a source that returns despite cancellation.
        currentCoroutineContext().ensureActive()
        mutex.withLock {
            entries[key] = Entry(books, clock.millis())
            while (entries.size > capacity) entries.remove(entries.keys.first())
        }
        return SearchResult(books, ResultSource.NETWORK)
    }
}
