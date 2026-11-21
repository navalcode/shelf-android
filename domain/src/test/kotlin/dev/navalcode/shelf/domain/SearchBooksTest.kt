package dev.navalcode.shelf.domain

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class SearchBooksTest {
    @Test
    fun `normalizes query and delegates to mocked repository`() = runTest {
        var captured = ""
        val expected =
            SearchResult(
                listOf(Book("/works/OL1W", "A book", listOf("A"), 2000, 2)),
                ResultSource.CACHE,
            )
        val repository = BookRepository {
            captured = it
            expected
        }
        assertEquals(expected, SearchBooks(repository)("  THE   Hobbit  "))
        assertEquals("the hobbit", captured)
    }

    @Test
    fun `rejects blank and overlong queries before repository`() = runTest {
        var calls = 0
        val search = SearchBooks {
            calls++
            SearchResult(emptyList(), ResultSource.NETWORK)
        }
        for (query in listOf("  ", "x".repeat(121))) {
            try {
                search(query)
                fail("Expected validation")
            } catch (_: IllegalArgumentException) {}
        }
        assertEquals(0, calls)
    }

    @Test
    fun `preserves repository error`() = runTest {
        val error = SearchException(SearchFailureKind.NETWORK)
        try {
            SearchBooks { throw error }("books")
            fail()
        } catch (actual: SearchException) {
            assertSame(error, actual)
        }
    }

    @Test
    fun `preserves cancellation`() = runTest {
        val cancelled = CancellationException("superseded")
        try {
            SearchBooks { throw cancelled }("books")
            fail()
        } catch (actual: CancellationException) {
            assertSame(cancelled, actual)
        }
    }

    @Test
    fun `canonical keys do not change with Turkish device locale`() = runTest {
        val previous = java.util.Locale.getDefault()
        try {
            java.util.Locale.setDefault(java.util.Locale.forLanguageTag("tr-TR"))
            var received = ""
            SearchBooks {
                received = it
                SearchResult(emptyList(), ResultSource.NETWORK)
            }("  ISTANBUL  ")
            assertEquals("istanbul", received)
        } finally {
            java.util.Locale.setDefault(previous)
        }
    }
}
