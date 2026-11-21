@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class)

package dev.navalcode.shelf.presentation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.navalcode.shelf.R
import dev.navalcode.shelf.domain.Book

/** One identity in two contexts: scan the index, then read the selected work. */
@Composable
internal fun BookIdentity(book: Book, detail: Boolean, shared: SharedTransitionScope,
    visibility: AnimatedVisibilityScope, modifier: Modifier = Modifier) {
    with(shared) {
        val identity = if (LocalMotionEnabled.current) modifier.sharedElement(rememberSharedContentState("book:${book.id}"),
            animatedVisibilityScope = visibility,
            boundsTransform = { _, _ -> tween(MotionTiming.IDENTITY, easing = FastOutSlowInEasing) }) else modifier
        Column(identity, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(book.title,
                style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface,
                modifier = if (detail) Modifier.semantics { heading() } else Modifier)
            Text(book.authors.joinToString().ifEmpty { stringResource(R.string.unknown_author) },
                style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
