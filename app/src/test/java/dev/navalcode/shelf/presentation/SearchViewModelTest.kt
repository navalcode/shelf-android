package dev.navalcode.shelf.presentation

import androidx.lifecycle.SavedStateHandle
import dev.navalcode.shelf.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val book = Book("/works/OL1W", "Book", listOf("Author"), 1999, 3)

    private fun result(title: String = "Book") =
        SearchResult(listOf(book.copy(title = title)), ResultSource.NETWORK)

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `typing coalesces while cancellation happens before debounce`() =
        runTest(dispatcher) {
            val calls = mutableListOf<String>()
            var cancelled = false
            val model =
                SearchViewModel(
                    SearchBooks {
                        calls += it
                        if (it == "old")
                            try {
                                awaitCancellation()
                            } finally {
                                cancelled = true
                            }
                        result()
                    },
                    SavedStateHandle(),
                )
            model.edit("old")
            advanceTimeBy(400)
            runCurrent()
            model.edit("n")
            runCurrent()
            assertTrue(cancelled)
            assertEquals(SearchContent.Loading, model.state.value.content)
            advanceTimeBy(200)
            model.edit("new")
            advanceTimeBy(399)
            runCurrent()
            assertEquals(listOf("old"), calls)
            advanceTimeBy(1)
            runCurrent()
            assertEquals(listOf("old", "new"), calls)
        }

    @Test
    fun `late obsolete result cannot replace newer result`() =
        runTest(dispatcher) {
            val release = CompletableDeferred<Unit>()
            val model =
                SearchViewModel(
                    SearchBooks {
                        if (it == "old") withContext(NonCancellable) { release.await() }
                        result(it)
                    },
                    SavedStateHandle(),
                )
            model.edit("old")
            advanceTimeBy(400)
            runCurrent()
            model.edit("new")
            advanceTimeBy(400)
            runCurrent()
            release.complete(Unit)
            runCurrent()
            assertEquals(
                "new",
                (model.state.value.content as SearchContent.Results).result.books.single().title,
            )
        }

    @Test
    fun `clearing cancels in flight and hides previous results`() =
        runTest(dispatcher) {
            var cancelled = false
            val model =
                SearchViewModel(
                    SearchBooks {
                        try {
                            awaitCancellation()
                        } finally {
                            cancelled = true
                        }
                    },
                    SavedStateHandle(),
                )
            model.edit("book")
            advanceTimeBy(400)
            runCurrent()
            model.edit("")
            runCurrent()
            assertTrue(cancelled)
            assertEquals(SearchContent.Idle, model.state.value.content)
        }

    @Test
    fun `network failure is retryable and retry does not debounce`() =
        runTest(dispatcher) {
            var calls = 0
            val model =
                SearchViewModel(
                    SearchBooks {
                        if (++calls == 1) throw SearchException(SearchFailureKind.NETWORK)
                        result()
                    },
                    SavedStateHandle(),
                )
            model.edit("book")
            advanceUntilIdle()
            assertEquals(SearchContent.Failed(Failure.NETWORK), model.state.value.content)
            model.retry()
            runCurrent()
            assertEquals(2, calls)
            assertTrue(model.state.value.content is SearchContent.Results)
        }

    @Test
    fun `saved query is searched and detail snapshot restores independently`() =
        runTest(dispatcher) {
            val saved = SavedStateHandle(mapOf("query" to "book"))
            val model = SearchViewModel(SearchBooks { result() }, saved)
            model.select(book)
            advanceUntilIdle()
            val restored = SearchViewModel(SearchBooks { result() }, saved)
            assertEquals(book, restored.selected)
            assertEquals("book", restored.state.value.query)
            advanceUntilIdle()
            assertTrue(restored.state.value.content is SearchContent.Results)
        }

    @Test
    fun `new query removes prior results immediately and overlong query does no IO`() =
        runTest(dispatcher) {
            var calls = 0
            val model =
                SearchViewModel(
                    SearchBooks {
                        calls++
                        result()
                    },
                    SavedStateHandle(),
                )
            model.edit("book")
            advanceUntilIdle()
            model.edit("new")
            assertEquals(SearchContent.Loading, model.state.value.content)
            model.edit("x".repeat(121))
            advanceUntilIdle()
            assertEquals(1, calls)
            assertEquals(SearchContent.Failed(Failure.INVALID_QUERY), model.state.value.content)
        }

    @Test
    fun `domain failures produce distinct presentation states`() =
        runTest(dispatcher) {
            val cases =
                mapOf(
                    SearchFailureKind.RATE_LIMIT to Failure.RATE_LIMIT,
                    SearchFailureKind.SERVICE to Failure.SERVICE,
                    SearchFailureKind.INVALID_DATA to Failure.INVALID_DATA,
                )
            for ((kind, expected) in cases) {
                val model =
                    SearchViewModel(SearchBooks { throw SearchException(kind) }, SavedStateHandle())
                model.edit("book")
                advanceUntilIdle()
                assertEquals(SearchContent.Failed(expected), model.state.value.content)
            }
        }

    @Test
    fun `unexpected failures remain separate from connectivity`() =
        runTest(dispatcher) {
            val model = SearchViewModel(SearchBooks { error("unexpected") }, SavedStateHandle())
            model.edit("book")
            advanceUntilIdle()
            assertEquals(SearchContent.Failed(Failure.UNEXPECTED), model.state.value.content)
        }

    @Test
    fun `obsolete failure cannot overwrite a newer success`() =
        runTest(dispatcher) {
            val release = CompletableDeferred<Unit>()
            val model =
                SearchViewModel(
                    SearchBooks {
                        if (it == "old") {
                            withContext(NonCancellable) { release.await() }
                            throw SearchException(SearchFailureKind.SERVICE)
                        }
                        result(it)
                    },
                    SavedStateHandle(),
                )
            model.edit("old")
            advanceTimeBy(400)
            runCurrent()
            model.edit("new")
            advanceTimeBy(400)
            runCurrent()
            release.complete(Unit)
            runCurrent()
            assertEquals(
                "new",
                (model.state.value.content as SearchContent.Results).result.books.single().title,
            )
        }
}
