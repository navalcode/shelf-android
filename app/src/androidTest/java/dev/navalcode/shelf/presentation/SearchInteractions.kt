package dev.navalcode.shelf.presentation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeTestRule

/** Compact controls share the catalogue's scroll; expanded controls own their independent pane. */
internal fun ComposeTestRule.revealSearchInput() {
    if (onAllNodesWithTag("search_input").fetchSemanticsNodes().isEmpty()) {
        onNodeWithTag("search_screen").performScrollToNode(hasTestTag("search_input"))
    } else {
        onNodeWithTag("search_input").performScrollTo()
    }
}
