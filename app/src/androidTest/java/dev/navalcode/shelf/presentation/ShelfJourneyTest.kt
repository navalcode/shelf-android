package dev.navalcode.shelf.presentation

import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.test.*
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.pressBack
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.navalcode.shelf.domain.*
import org.junit.Rule
import org.junit.Test

class ShelfJourneyTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val book = Book("/works/OL1W", "The Glass Orchard", listOf("Alex Rivera"), 2001, 7)
    private lateinit var model: SearchViewModel
    private val captures = NativeCapture(rule)
    private fun awaitResults() {
        rule.waitUntil(5_000) { model.state.value.content is SearchContent.Results }
        closeSoftKeyboard()
        rule.onNodeWithTag("search_screen").performScrollToNode(hasTestTag("book_/works/OL1W"))
    }
    private fun start(repository: BookRepository) {
        rule.runOnUiThread { rule.activity.enableEdgeToEdge() }
        rule.setContent {
            model = viewModel(factory = viewModelFactory {
                initializer { SearchViewModel(SearchBooks(repository), SavedStateHandle()) }
            })
            ShelfTheme { ShelfApp(model) }
        }
    }
    @Test fun searchDetailAndBackPreserveQuery() {
        start { SearchResult(listOf(book), ResultSource.NETWORK) }
        captures.frame("idle")
        rule.onNodeWithTag("search_input").performTextInput("orchard")
        awaitResults()
        captures.frame("search")
        captures.transition("motion") { rule.onNodeWithTag("book_/works/OL1W").performClick() }
        rule.onNodeWithTag("detail_screen").assertExists()
        rule.onNodeWithText("Alex Rivera").assertExists()
        rule.onNodeWithText("2001").assertExists()
        rule.onNodeWithText("7").assertExists()
        captures.frame("detail")
        captures.transition("return") { rule.onNodeWithTag("back").performClick() }
        rule.onNodeWithTag("search_screen").performScrollToNode(hasTestTag("search_input"))
        rule.onNodeWithTag("search_input").assertTextContains("orchard")
        rule.onNodeWithTag("book_/works/OL1W").assertExists()
    }
    @Test fun errorRetryAndClearRecover() {
        var calls = 0
        start { if (++calls == 1) throw SearchException(SearchFailureKind.NETWORK); SearchResult(listOf(book), ResultSource.NETWORK) }
        rule.onNodeWithTag("search_input").performTextInput("orchard")
        rule.waitUntil(5_000) { model.state.value.content is SearchContent.Failed }
        closeSoftKeyboard()
        rule.onNodeWithTag("search_screen").performScrollToNode(hasTestTag("retry"))
        captures.frame("error")
        rule.onNodeWithTag("retry").performClick()
        awaitResults()
        rule.onNodeWithTag("search_screen").performScrollToNode(hasTestTag("search_input"))
        rule.onNodeWithTag("clear_search").performClick()
        rule.onNodeWithTag("book_/works/OL1W").assertDoesNotExist()
    }
    @Test fun emptySearchDoesNotRenderOldItems() {
        start { SearchResult(if (it == "empty") emptyList() else listOf(book), ResultSource.NETWORK) }
        rule.onNodeWithTag("search_input").performTextInput("orchard")
        awaitResults()
        rule.onNodeWithTag("search_screen").performScrollToNode(hasTestTag("search_input"))
        rule.onNodeWithTag("search_input").performTextReplacement("empty")
        rule.onNodeWithTag("book_/works/OL1W").assertDoesNotExist()
        rule.waitUntil(5_000) {
            (model.state.value.content as? SearchContent.Results)?.result?.books?.isEmpty() == true
        }
        closeSoftKeyboard()
        rule.onNodeWithTag("search_screen").performScrollToNode(hasTestTag("empty_state"))
        rule.onNodeWithTag("empty_state").assertIsDisplayed()
        captures.frame("empty")
    }
    @Test fun suggestionSearchAndSystemBackKeepContext() {
        var requested = ""
        start { requested = it; SearchResult(listOf(book), ResultSource.NETWORK) }
        rule.onNodeWithTag("search_screen").performScrollToNode(hasText("Dune"))
        rule.onNodeWithText("Dune").performClick()
        awaitResults()
        org.junit.Assert.assertEquals("dune", requested)
        rule.onNodeWithTag("book_/works/OL1W").performClick()
        rule.onNodeWithTag("detail_screen").assertExists()
        pressBack()
        rule.onNodeWithTag("search_screen").performScrollToNode(hasTestTag("search_input"))
        rule.onNodeWithTag("search_input").assertTextContains("Dune")
    }

}
