package dev.navalcode.shelf.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.navalcode.shelf.domain.*
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed interface SearchContent {
    data object Idle : SearchContent
    data object Loading : SearchContent
    data class Results(val result: SearchResult) : SearchContent
    data class Failed(val reason: Failure) : SearchContent
}
enum class Failure { NETWORK, RATE_LIMIT, SERVICE, INVALID_QUERY, UNEXPECTED }
data class SearchState(val query: String = "", val content: SearchContent = SearchContent.Idle)

class SearchViewModel(
    private val searchBooks: SearchBooks,
    private val saved: SavedStateHandle,
) : ViewModel() {
    private val mutable = MutableStateFlow(SearchState(saved["query"] ?: ""))
    val state = mutable.asStateFlow()
    private var request: Job? = null
    private var generation = 0L
    val selected: Book? get() {
        val id = saved.get<String>("book.id") ?: return null
        return Book(id, saved["book.title"] ?: "", saved.get<ArrayList<String>>("book.authors").orEmpty(),
            saved["book.year"], saved["book.editions"])
    }
    init { if (mutable.value.query.isNotBlank()) runSearch(debounce = true) }

    fun edit(input: String) {
        // Keep saved state bounded even when an oversized string is pasted.
        val query = input.take(SearchBooks.MAX_QUERY_LENGTH + 1)
        saved["query"] = query
        mutable.value = SearchState(query)
        runSearch(debounce = true)
    }
    fun retry() = runSearch(debounce = false)
    fun select(book: Book) {
        saved["book.id"] = book.id
        saved["book.title"] = book.title
        saved["book.authors"] = ArrayList(book.authors)
        saved["book.year"] = book.firstPublished
        saved["book.editions"] = book.editionCount
    }
    private fun runSearch(debounce: Boolean) {
        val ticket = ++generation
        request?.cancel() // Runs immediately, before waiting for the new debounce.
        val query = mutable.value.query
        if (query.isBlank()) { mutable.value = SearchState(query); return }
        if (canonicalQuery(query).length > SearchBooks.MAX_QUERY_LENGTH) {
            mutable.value = SearchState(query, SearchContent.Failed(Failure.INVALID_QUERY)); return
        }
        mutable.value = SearchState(query, SearchContent.Loading)
        request = viewModelScope.launch {
            try {
                if (debounce) delay(400)
                val result = searchBooks(query)
                if (ticket == generation) mutable.value = SearchState(query, SearchContent.Results(result))
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (error: Exception) {
                if (ticket == generation) mutable.value = SearchState(query, SearchContent.Failed(when (error) {
                    is HttpException -> if (error.code() == 429) Failure.RATE_LIMIT else Failure.SERVICE
                    is IOException -> Failure.NETWORK
                    else -> Failure.UNEXPECTED
                }))
            }
        }
    }
}
