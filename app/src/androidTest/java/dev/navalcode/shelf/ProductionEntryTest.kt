package dev.navalcode.shelf

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Actual launcher, Application graph, saved-state factory, Retrofit and cache; only HTTP is controlled. */
class ProductionEntryTest {
    @get:Rule val rule = createEmptyComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    @Before fun resetHttp() = ControlledHttp.reset()
    @After fun noUnexpectedRequests() { assertTrue(ControlledHttp.replies.isEmpty()) }
    private fun awaitText(text: String) {
        rule.waitUntil(5_000) { rule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("search_screen").performScrollToNode(hasText(text))
        rule.onNodeWithText(text).assertIsDisplayed()
    }
    private fun search(query: String) {
        rule.onNodeWithTag("search_screen").performScrollToNode(hasTestTag("search_input"))
        rule.onNodeWithTag("search_input").performTextReplacement(query)
        closeSoftKeyboard()
    }
    @Test fun launcherRejectsInvalidDataRetriesAndNavigatesWithRealCache() {
        ControlledHttp.replies += ControlledHttp.Reply(body = "{\"docs\":[null]}")
        ControlledHttp.replies += ControlledHttp.Reply(body = """{"docs":[{"key":"/works/OL42W","title":"The Copper Atlas","author_name":["Sam Vale"],"first_publish_year":2004,"edition_count":8}]}""")
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            search("  Entry   Wire  ")
            awaitText(context.getString(R.string.data_error))
            rule.onNodeWithText(context.getString(R.string.network_error)).assertDoesNotExist()
            rule.onNodeWithTag("retry").performClick()
            awaitText("The Copper Atlas")
            rule.onNodeWithTag("book_/works/OL42W").performClick()
            rule.onNodeWithTag("detail_screen").assertExists()
            rule.onNodeWithText("Sam Vale").assertExists()
            rule.onNodeWithText("2004").assertExists()
            rule.onNodeWithText("8").assertExists()
            // Recreate the real activity while detail is selected: production factory + Navigation restoration.
            scenario.recreate()
            rule.onNodeWithTag("detail_screen").assertExists()
            rule.onNodeWithText("Sam Vale").assertExists()
            rule.onNodeWithTag("back").performClick()
            search("")
            search("ENTRY WIRE")
            awaitText(context.getString(R.string.results_cache))
            awaitText("The Copper Atlas")
            assertEquals(2, ControlledHttp.requests.size)
            ControlledHttp.requests.forEach {
                assertEquals("https", it.url.scheme)
                assertEquals("openlibrary.org", it.url.host)
                assertEquals("entry wire", it.url.queryParameter("q"))
                assertEquals("20", it.url.queryParameter("limit"))
                assertTrue(it.header("User-Agent")!!.startsWith("ShelfAndroid/"))
            }
        }
    }
    @Test fun launcherDisplaysDistinctRateLimitAndServiceRecoveryMessages() {
        ControlledHttp.replies += ControlledHttp.Reply(429, "{}")
        ControlledHttp.replies += ControlledHttp.Reply(503, "{}")
        ActivityScenario.launch(MainActivity::class.java).use {
            search("entry errors")
            awaitText(context.getString(R.string.rate_error))
            rule.onNodeWithTag("retry").performClick()
            awaitText(context.getString(R.string.service_error))
            assertEquals(2, ControlledHttp.requests.size)
        }
    }
}
