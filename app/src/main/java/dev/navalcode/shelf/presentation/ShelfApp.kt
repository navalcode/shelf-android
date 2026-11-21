@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package dev.navalcode.shelf.presentation

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*

/** Navigation owns the relationship animation; changing a query never retains old result content. */
@Composable
fun ShelfApp(model: SearchViewModel) {
    val nav = rememberNavController()
    val state by model.state.collectAsStateWithLifecycle()
    val motion = rememberMotionEnabled()
    CompositionLocalProvider(LocalMotionEnabled provides motion) {
        Surface(Modifier.fillMaxSize().semantics { testTagsAsResourceId = true }) {
            SharedTransitionLayout {
                val shared = this
                NavHost(nav, startDestination = "search",
                    enterTransition = { if (motion) fadeIn(tween(MotionTiming.CONTENT, delayMillis = MotionTiming.CONTENT_DELAY)) else EnterTransition.None },
                    exitTransition = { if (motion) fadeOut(tween(MotionTiming.EXIT)) else ExitTransition.None },
                    popEnterTransition = { if (motion) fadeIn(tween(MotionTiming.CONTENT, delayMillis = MotionTiming.CONTENT_DELAY)) else EnterTransition.None },
                    popExitTransition = { if (motion) fadeOut(tween(MotionTiming.EXIT)) else ExitTransition.None }) {
                    composable("search") {
                        SearchScreen(state, model::edit, model::retry, shared, this) { book ->
                            model.select(book)
                            nav.navigate("detail") { launchSingleTop = true }
                        }
                    }
                    composable("detail") { DetailScreen(model.selected, shared, this) { nav.popBackStack() } }
                }
            }
        }
    }
}
