@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package dev.navalcode.shelf.presentation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import dev.navalcode.shelf.R
import dev.navalcode.shelf.domain.Book
import dev.navalcode.shelf.domain.ResultSource

@Composable
internal fun SearchScreen(state: SearchState, edit: (String) -> Unit, retry: () -> Unit,
    shared: SharedTransitionScope, visibility: AnimatedVisibilityScope, select: (Book) -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(painterResource(R.drawable.ic_catalogue), null, tint = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary)
            }
        }, actions = { Text("Open Library", style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 24.dp)) })
    }) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            if (maxWidth >= ShelfLayout.ExpandedMinWidth) {
                Row(Modifier.widthIn(max = ShelfLayout.ExpandedContentWidth).fillMaxSize().padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(ShelfLayout.PaneGap)) {
                    Column(Modifier.width(ShelfLayout.SidePaneWidth).imePadding().verticalScroll(rememberScrollState())) {
                        SearchHeader(state, compact = false, edit, retry)
                    }
                    Catalogue(state.content, shared, visibility, select, edit, retry, Modifier.weight(1f))
                }
            } else {
                Catalogue(state.content, shared, visibility, select, edit, retry,
                    Modifier.widthIn(max = ShelfLayout.CompactContentWidth), header = {
                        SearchHeader(state, compact = maxHeight < 480.dp, edit, retry)
                    })
            }
        }
    }
}

@Composable
private fun Catalogue(content: SearchContent, shared: SharedTransitionScope, visibility: AnimatedVisibilityScope,
    select: (Book) -> Unit, edit: (String) -> Unit, retry: () -> Unit,
    modifier: Modifier = Modifier, header: (@Composable () -> Unit)? = null) {
    LazyColumn(modifier.fillMaxSize().imePadding().testTag("search_screen"),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)) {
        if (header != null) item(key = "controls") { header() }
        when (content) {
            SearchContent.Idle -> item { Suggestions(edit) }
            SearchContent.Loading -> item {
                Column(Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.loading), style = MaterialTheme.typography.bodyMedium)
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
            }
            is SearchContent.Failed -> item { SearchFailure(content.reason, retry) }
            is SearchContent.Results -> {
                if (content.result.books.isEmpty()) item {
                    StateMessage(R.string.empty_title, R.string.empty_body, Modifier.testTag("empty_state"))
                } else {
                    item {
                        FlowRow(Modifier.fillMaxWidth().semantics(mergeDescendants = true) {
                            liveRegion = LiveRegionMode.Polite; heading()
                        }, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(stringResource(if (content.result.source == ResultSource.CACHE)
                                R.string.results_cache else R.string.results_network),
                                style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            Text(pluralStringResource(R.plurals.works_count, content.result.books.size, content.result.books.size),
                                style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    items(content.result.books, key = { it.id }) { book -> BookRow(book, shared, visibility) { select(book) } }
                }
                item { Text(stringResource(R.string.results_limit), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
private fun Suggestions(edit: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.suggestions), style = MaterialTheme.typography.titleSmall)
        Column {
            for (query in listOf("Ursula K. Le Guin", "Jorge Luis Borges", "Dune")) {
                TextButton(onClick = { edit(query) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)) {
                    Text(query, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start)
                    Icon(painterResource(R.drawable.ic_forward), null, Modifier.padding(start = 16.dp).size(20.dp))
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
        Text(stringResource(R.string.initial_body), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BookRow(book: Book, shared: SharedTransitionScope, visibility: AnimatedVisibilityScope, click: () -> Unit) {
    val action = stringResource(R.string.open_book, book.title)
    Surface(modifier = Modifier.fillMaxWidth().testTag("book_${book.id}").clickable(
        role = Role.Button, onClickLabel = action, onClick = click),
        color = MaterialTheme.colorScheme.surface) {
        Column {
            Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically) {
                BookIdentity(book, false, shared, visibility, Modifier.weight(1f))
                Icon(painterResource(R.drawable.ic_forward), null, tint = MaterialTheme.colorScheme.primary)
            }
            book.firstPublished?.let {
                val publishedLabel = stringResource(R.string.published_in, it)
                Text(it.toString(), style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 20.dp).semantics {
                        contentDescription = publishedLabel
                    })
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun SearchFailure(reason: Failure, retry: () -> Unit) {
    Column(Modifier.testTag("error_state").semantics { liveRegion = LiveRegionMode.Polite }, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(when (reason) {
            Failure.NETWORK -> R.string.network_error
            Failure.RATE_LIMIT -> R.string.rate_error
            Failure.SERVICE -> R.string.service_error
            Failure.INVALID_DATA -> R.string.data_error
            Failure.INVALID_QUERY -> R.string.query_error
            Failure.UNEXPECTED -> R.string.unexpected_error
        }), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
        if (reason != Failure.INVALID_QUERY) Button(retry, Modifier.heightIn(min = 48.dp).testTag("retry")) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
private fun StateMessage(title: Int, body: Int, modifier: Modifier = Modifier) {
    Column(modifier.padding(vertical = 16.dp).semantics { liveRegion = LiveRegionMode.Polite }, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(body), style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
