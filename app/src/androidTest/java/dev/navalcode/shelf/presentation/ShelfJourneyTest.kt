package dev.navalcode.shelf.presentation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.*
import androidx.compose.ui.graphics.asAndroidBitmap
import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.Espresso.closeSoftKeyboard
import java.io.File
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.navalcode.shelf.domain.*
import java.io.IOException
import org.junit.Rule
import org.junit.Test

class ShelfJourneyTest {
    @get:Rule val rule = createAndroidComposeRule<ComponentActivity>()
    private val book = Book("/works/OL1W", "The Glass Orchard", listOf("Alex Rivera"), 2001, 7)
    private lateinit var model: SearchViewModel
    private fun capture(name: String) {
        val directory = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "screenshots")
        directory.mkdirs()
        File(directory, "$name.png").outputStream().use {
            rule.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
    private fun awaitResults() {
        rule.waitUntil(5_000) { model.state.value.content is SearchContent.Results }
        closeSoftKeyboard()
        rule.onNodeWithTag("search_screen").performScrollToNode(hasTestTag("book_/works/OL1W"))
    }
    private fun start(repository: BookRepository) {
        rule.setContent {
            model = viewModel(factory = viewModelFactory {
                initializer { SearchViewModel(SearchBooks(repository), SavedStateHandle()) }
            })
            ShelfTheme { ShelfApp(model) }
        }
    }
    @Test fun searchDetailAndBackPreserveQuery() {
        start { SearchResult(listOf(book), ResultSource.NETWORK) }
        rule.onNodeWithTag("search_input").performTextInput("orchard")
        awaitResults()
        capture("search")
        rule.onNodeWithTag("book_/works/OL1W").performClick()
        rule.onNodeWithTag("detail_screen").assertExists()
        rule.onNodeWithText("Alex Rivera").assertExists()
        rule.onNodeWithText("2001").assertExists()
        rule.onNodeWithText("7").assertExists()
        capture("detail")
        rule.onNodeWithTag("back").performClick()
        rule.onNodeWithTag("search_screen").performScrollToNode(hasTestTag("search_input"))
        rule.onNodeWithTag("search_input").assertTextContains("orchard")
        rule.onNodeWithTag("book_/works/OL1W").assertExists()
    }
    @Test fun errorRetryAndClearRecover() {
        var calls = 0
        start { if (++calls == 1) throw IOException("offline"); SearchResult(listOf(book), ResultSource.NETWORK) }
        rule.onNodeWithTag("search_input").performTextInput("orchard")
        rule.waitUntil(5_000) { model.state.value.content is SearchContent.Failed }
        closeSoftKeyboard()
        rule.onNodeWithTag("search_screen").performScrollToNode(hasTestTag("retry"))
        rule.onNodeWithTag("retry").performClick()
        awaitResults()
        rule.onNodeWithTag("search_screen").performScrollToNode(hasTestTag("search_input"))
        rule.onNodeWithTag("search_input").performTextClearance()
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
    }
}
