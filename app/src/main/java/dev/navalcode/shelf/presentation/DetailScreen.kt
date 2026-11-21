@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package dev.navalcode.shelf.presentation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.navalcode.shelf.R
import dev.navalcode.shelf.domain.Book

@Composable
internal fun DetailScreen(book: Book?, shared: SharedTransitionScope, visibility: AnimatedVisibilityScope, back: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.detail_title), style = MaterialTheme.typography.titleMedium) },
            navigationIcon = { IconButton(back, Modifier.testTag("back")) {
                Icon(painterResource(R.drawable.ic_back), stringResource(R.string.back))
            } })
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 720.dp).fillMaxSize().verticalScroll(rememberScrollState())
                .padding(24.dp).testTag("detail_screen"), verticalArrangement = Arrangement.spacedBy(28.dp)) {
                if (book == null) Text(stringResource(R.string.missing_detail))
                else {
                    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
                        BookIdentity(book, true, shared, visibility, Modifier.fillMaxWidth().padding(24.dp))
                    }
                    // Let the identity reach its reading position before the metadata enters.
                    val metadataMotion = if (LocalMotionEnabled.current) with(visibility) {
                        Modifier.animateEnterExit(enter = fadeIn(tween(MotionTiming.METADATA, delayMillis = MotionTiming.METADATA_DELAY)),
                            exit = fadeOut(tween(MotionTiming.EXIT)))
                    } else Modifier
                    Column(metadataMotion, verticalArrangement = Arrangement.spacedBy(28.dp)) {
                        Text(stringResource(R.string.about_work), style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.semantics { heading() })
                        Field(R.string.year, book.firstPublished?.toString() ?: stringResource(R.string.unknown))
                        Field(R.string.editions, book.editionCount?.toString() ?: stringResource(R.string.unknown))
                        Field(R.string.identifier, book.id.removePrefix("/works/"))
                        Text(stringResource(R.string.detail_note), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun Field(label: Int, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(label), style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
