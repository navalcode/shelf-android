package dev.navalcode.shelf.presentation

import android.os.Build
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import dev.navalcode.shelf.MainActivity
import dev.navalcode.shelf.ControlledHttp
import dev.navalcode.shelf.booksReply
import dev.navalcode.shelf.R
import dev.navalcode.shelf.domain.Book
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import org.junit.Rule
import org.junit.Test


/** ATF scans run on API34+; names, actions, target bounds and recovery run on every supported test API. */
class AccessibilityJourneyTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val book = Book("/works/OL77W", "The Lighthouse Across the Winter Sea", listOf("Avery Sol"), 2002, 4)

    @Before fun setup() {
        ControlledHttp.reset()
        if (Build.VERSION.SDK_INT >= 34) rule.enableAccessibilityChecks()
    }
    @After fun allRepliesConsumed() { assertTrue(ControlledHttp.replies.isEmpty()) }

    private fun scan() {
        if (Build.VERSION.SDK_INT >= 34) rule.onRoot().tryPerformAccessibilityChecks()
    }

    private fun assertTarget(node: SemanticsNodeInteraction) {
        val bounds = node.getUnclippedBoundsInRoot()
        assertTrue(bounds.right - bounds.left >= 48.dp)
        assertTrue(bounds.bottom - bounds.top >= 48.dp)
    }

    @Test fun namesTargetsKeyboardAndDetailAreAccessible() {
        ControlledHttp.replies += booksReply(book)
        scan()
        val input = rule.onNodeWithTag("search_input")
        input.assert(hasSetTextAction())
        assertTarget(input)
        input.performTextInput("lighthouse")
        input.assertIsFocused() // The initial panel's collapse must not replace the editing node.
        val clear = rule.onNodeWithTag("clear_search")
        assertTarget(clear)
        input.performImeAction()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("book_${book.id}").fetchSemanticsNodes().isNotEmpty() }
        rule.waitUntil(5_000) {
            ViewCompat.getRootWindowInsets(rule.activity.window.decorView)
                ?.isVisible(WindowInsetsCompat.Type.ime()) == false
        }
        rule.onNodeWithTag("search_screen").performScrollToNode(hasTestTag("book_${book.id}"))
        val row = rule.onNodeWithTag("book_${book.id}")
        row.assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
        assertEquals(rule.activity.getString(R.string.open_book, book.title),
            row.fetchSemanticsNode().config[SemanticsActions.OnClick].label)
        assertTarget(row)
        scan()
        row.performClick()
        rule.onNodeWithTag("detail_screen").assert(SemanticsMatcher.expectValue(
            SemanticsProperties.PaneTitle, rule.activity.getString(R.string.detail_title)))
        scan()
        rule.onNodeWithTag("back").performClick()
        rule.revealSearchInput()
        rule.onNodeWithTag("search_input").assertTextContains("lighthouse")
    }

    @Test fun failureAndEmptyStatesAnnounceAndRecover() {
        val allowSuccess = AtomicBoolean(false)
        val successes = AtomicInteger()
        ControlledHttp.handler = {
            if (!allowSuccess.get()) throw IOException("Controlled offline response")
            successes.incrementAndGet()
            booksReply()
        }
        rule.onNodeWithTag("search_input").performTextInput("empty catalogue")
        rule.onNodeWithTag("search_input").performImeAction()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("retry").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("search_screen").performScrollToNode(hasTestTag("retry"))
        rule.onNodeWithTag("error_state").assert(SemanticsMatcher.expectValue(
            SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        scan()
        allowSuccess.set(true)
        rule.onNodeWithTag("retry").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("empty_state").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("empty_state").assert(SemanticsMatcher.expectValue(
            SemanticsProperties.LiveRegion, LiveRegionMode.Polite))
        scan()
        assertEquals(1, successes.get())
    }
    @Test fun absentMetadataIsNamedWithoutInventingValues() {
        ControlledHttp.replies += booksReply(book.copy(authors = emptyList(), firstPublished = null, editionCount = null))
        rule.onNodeWithTag("search_input").performTextInput("missing metadata")
        rule.onNodeWithTag("search_input").performImeAction()
        rule.waitUntil(5_000) { rule.onAllNodesWithTag("book_${book.id}").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("search_screen").performScrollToNode(hasTestTag("book_${book.id}"))
        rule.onNodeWithTag("book_${book.id}").performClick()
        rule.onNodeWithText(rule.activity.getString(R.string.unknown_author)).assertExists()
        rule.onAllNodesWithText(rule.activity.getString(R.string.unknown)).assertCountEquals(2)
        rule.onNodeWithText("OL77W").assertExists()
        scan()
    }
}
