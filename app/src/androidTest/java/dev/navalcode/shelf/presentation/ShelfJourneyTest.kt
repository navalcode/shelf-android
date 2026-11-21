package dev.navalcode.shelf.presentation

import androidx.compose.ui.test.*
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.pressBack
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.navalcode.shelf.MainActivity
import dev.navalcode.shelf.ControlledHttp
import dev.navalcode.shelf.booksReply
import dev.navalcode.shelf.domain.Book
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Before
import org.junit.After
import org.junit.Rule
import org.junit.Test


class ShelfJourneyTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val book = Book("/works/OL1W", "The Glass Orchard", listOf("Alex Rivera"), 2001, 7)
    private val captures = NativeCapture(rule)
    private fun awaitResults() {
        closeSoftKeyboard()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("book_/works/OL1W").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("search_screen").performScrollToNode(hasTestTag("book_/works/OL1W"))
    }
    @Before fun resetHttp() = ControlledHttp.reset()
    @After fun allRepliesConsumed() { org.junit.Assert.assertTrue(ControlledHttp.replies.isEmpty()) }
    @Test fun searchDetailAndBackPreserveQuery() {
        ControlledHttp.replies += booksReply(book)
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
        rule.revealSearchInput()
        rule.onNodeWithTag("search_input").assertTextContains("orchard")
        rule.onNodeWithTag("book_/works/OL1W").assertExists()
    }
    @Test fun errorRetryAndClearRecover() {
        val offline = AtomicBoolean(true)
        ControlledHttp.handler = {
            if (offline.get()) throw IOException("Controlled offline response")
            booksReply(book)
        }
        rule.onNodeWithTag("search_input").performTextInput("recovery orchard")
        closeSoftKeyboard()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("retry").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("search_screen").performScrollToNode(hasTestTag("retry"))
        captures.frame("error")
        offline.set(false)
        rule.onNodeWithTag("retry").performClick()
        awaitResults()
        rule.revealSearchInput()
        rule.onNodeWithTag("clear_search").performClick()
        rule.onNodeWithTag("book_/works/OL1W").assertDoesNotExist()
    }
    @Test fun emptySearchDoesNotRenderOldItems() {
        ControlledHttp.handler = { request ->
            if (request.url.queryParameter("q") == "empty") booksReply() else booksReply(book)
        }
        rule.onNodeWithTag("search_input").performTextInput("present orchard")
        awaitResults()
        rule.revealSearchInput()
        rule.onNodeWithTag("search_input").performTextReplacement("empty")
        rule.onNodeWithTag("book_/works/OL1W").assertDoesNotExist()
        closeSoftKeyboard()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("empty_state").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("search_screen").performScrollToNode(hasTestTag("empty_state"))
        rule.onNodeWithTag("empty_state").assertIsDisplayed()
        captures.frame("empty")
    }
    @Test fun suggestionSearchAndSystemBackKeepContext() {
        ControlledHttp.replies += booksReply(book)
        rule.onNodeWithTag("search_screen").performScrollToNode(hasText("Dune"))
        rule.onNodeWithText("Dune").performClick()
        awaitResults()
        org.junit.Assert.assertEquals("dune", ControlledHttp.requests.single().url.queryParameter("q"))
        rule.onNodeWithTag("book_/works/OL1W").performClick()
        rule.onNodeWithTag("detail_screen").assertExists()
        pressBack()
        rule.revealSearchInput()
        rule.onNodeWithTag("search_input").assertTextContains("Dune")
    }

}
