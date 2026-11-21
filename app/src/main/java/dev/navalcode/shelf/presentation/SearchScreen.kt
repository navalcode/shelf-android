@file:OptIn(androidx.compose.animation.ExperimentalSharedTransitionApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package dev.navalcode.shelf.presentation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.navalcode.shelf.R
import dev.navalcode.shelf.domain.Book
import dev.navalcode.shelf.domain.ResultSource

@Composable
internal fun SearchScreen(state: SearchState, edit: (String) -> Unit, retry: () -> Unit,
    shared: SharedTransitionScope, visibility: AnimatedVisibilityScope, select: (Book) -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("Shelf", style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp)) },
            actions = { Text("Open Library", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 24.dp)) })
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            LazyColumn(Modifier.widthIn(max = 720.dp).fillMaxSize().imePadding().testTag("search_screen"),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)) {
                item {
                    Text(stringResource(R.string.headline), style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.semantics { heading() })
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.intro), style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item {
                    OutlinedTextField(state.query, edit, Modifier.fillMaxWidth().testTag("search_input"),
                        label = { Text(stringResource(R.string.query_label)) },
                        leadingIcon = { Icon(painterResource(R.drawable.ic_search), contentDescription = null) },
                        singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        trailingIcon = if (state.query.isNotEmpty()) ({
                            IconButton({ edit("") }, Modifier.testTag("clear_search")) {
                                Icon(painterResource(R.drawable.ic_clear), stringResource(R.string.clear))
                            }
                        }) else null)
                }
                when (val content = state.content) {
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
                                Text(stringResource(if (content.result.source == ResultSource.CACHE)
                                    R.string.results_cache else R.string.results_network),
                                    style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                            }
                            items(content.result.books, key = { it.id }) { book ->
                                BookRow(book, shared, visibility) { select(book) }
                            }
                        }
                        item { Text(stringResource(R.string.results_limit), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
        }
    }
}

@Composable
private fun Suggestions(edit: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.suggestions), style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (query in listOf("Ursula K. Le Guin", "Jorge Luis Borges", "Dune")) {
                AssistChip(onClick = { edit(query) }, label = { Text(query) }, modifier = Modifier.heightIn(min = 48.dp),
                    trailingIcon = { Icon(painterResource(R.drawable.ic_forward), null, Modifier.size(18.dp)) })
            }
        }
        Text(stringResource(R.string.initial_body), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BookRow(book: Book, shared: SharedTransitionScope, visibility: AnimatedVisibilityScope, click: () -> Unit) {
    Surface(onClick = click, modifier = Modifier.fillMaxWidth().testTag("book_${book.id}"),
        color = MaterialTheme.colorScheme.surface) {
        Column {
            Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically) {
                BookIdentity(book, false, shared, visibility, Modifier.weight(1f))
                Icon(painterResource(R.drawable.ic_forward), null, tint = MaterialTheme.colorScheme.primary)
            }
            book.firstPublished?.let {
                Text(it.toString(), style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 20.dp))
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun SearchFailure(reason: Failure, retry: () -> Unit) {
    Column(Modifier.semantics { liveRegion = LiveRegionMode.Polite }, verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
    Column(modifier.padding(vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(body), style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
