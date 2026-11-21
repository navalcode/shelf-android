package dev.navalcode.shelf.data

import dev.navalcode.shelf.domain.*
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CachedBookRepositoryTest {
    private val book = Book("/works/OL1W", "Original", listOf("A"), 2001, 2)
    @Test fun `canonical query is cache key and valid hit performs no network`() = runTest {
        var calls = 0; var now = 0L
        val repo = CachedBookRepository({ calls++; listOf(book) }, { now })
        assertEquals(ResultSource.NETWORK, repo.search("  THE  BOOK ").source)
        now = 179_999
        assertEquals(ResultSource.CACHE, repo.search("the book").source)
        assertEquals(1, calls)
    }
    @Test fun `exact expiry fetches and replaces then starts a fresh TTL`() = runTest {
        var calls = 0; var now = 0L
        val repo = CachedBookRepository({ calls++; listOf(book.copy(title = "Version $calls")) }, { now })
        repo.search("book"); now = 180_000
        assertEquals("Version 2", repo.search("book").books.single().title)
        now = 359_999
        assertEquals(ResultSource.CACHE, repo.search("book").source)
        assertEquals(2, calls)
    }
    @Test fun `different query has an independent entry and empty results are cached`() = runTest {
        var calls = 0
        val repo = CachedBookRepository({ calls++; emptyList() }, { 0 })
        repo.search("one"); repo.search("two")
        assertEquals(ResultSource.CACHE, repo.search("one").source)
        assertEquals(2, calls)
    }
    @Test fun `least recently used entry is evicted at capacity`() = runTest {
        var calls = 0
        val repo = CachedBookRepository({ calls++; listOf(book) }, { 0 }, capacity = 2)
        repo.search("one"); repo.search("two"); repo.search("one"); repo.search("three")
        assertEquals(ResultSource.CACHE, repo.search("one").source)
        assertEquals(ResultSource.NETWORK, repo.search("two").source)
        assertEquals(4, calls)
    }
    @Test fun `expired entry is never served when network fails and failure is not cached`() = runTest {
        var now = 0L; var failing = false; var calls = 0
        val repo = CachedBookRepository({ calls++; if (failing) throw IOException(); listOf(book) }, { now })
        repo.search("book"); now = 180_000; failing = true
        try { repo.search("book"); fail() } catch (_: IOException) { }
        failing = false
        assertEquals(ResultSource.NETWORK, repo.search("book").source)
        assertEquals(3, calls)
    }
    @Test fun `TTL starts after network completion`() = runTest {
        var now = 0L
        val repo = CachedBookRepository({ now = 90_000; listOf(book) }, { now })
        repo.search("book"); now = 269_999
        assertEquals(ResultSource.CACHE, repo.search("book").source)
    }
    @Test fun `cancelled source propagates and never populates cache`() = runTest {
        var calls = 0
        val repo = CachedBookRepository({ calls++; if (calls == 1) awaitCancellation(); listOf(book) }, { 0 })
        val job = launch { repo.search("book") }
        runCurrent(); job.cancelAndJoin()
        assertEquals(ResultSource.NETWORK, repo.search("book").source)
        assertEquals(2, calls)
    }
    @Test fun `non cooperative cancelled source cannot populate cache`() = runTest {
        var calls = 0
        val release = CompletableDeferred<Unit>()
        val repo = CachedBookRepository({ calls++
            if (calls == 1) withContext(NonCancellable) { release.await() }
            listOf(book)
        }, { 0 })
        val job = launch { repo.search("book") }
        runCurrent(); job.cancel(); release.complete(Unit); job.join()
        assertEquals(ResultSource.NETWORK, repo.search("book").source)
        assertEquals(2, calls)
    }
    @Test fun `slow query does not hold cache lock across network`() = runTest {
        val repo = CachedBookRepository({ if (it == "slow") awaitCancellation(); listOf(book) }, { 0 })
        repo.search("cached")
        val job = launch { repo.search("slow") }; runCurrent()
        assertEquals(ResultSource.CACHE, repo.search("cached").source)
        job.cancelAndJoin()
    }
}
