package dev.navalcode.shelf.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import dev.navalcode.shelf.R
import dev.navalcode.shelf.domain.*

@Composable
fun ShelfTheme(content: @Composable () -> Unit) {
    val scheme = if (isSystemInDarkTheme()) darkColorScheme(
        primary = Color(0xFFAFD0BB), background = Color(0xFF101B18), surface = Color(0xFF18231F),
    ) else lightColorScheme(
        primary = Color(0xFF205347), onPrimary = Color.White,
        background = Color(0xFFFAF8F2), surface = Color(0xFFFAF8F2),
        surfaceVariant = Color(0xFFE8EDE5), onSurface = Color(0xFF192D28),
    )
    MaterialTheme(colorScheme = scheme, content = content)
}

@Composable
fun ShelfApp(model: SearchViewModel) {
    val nav = rememberNavController()
    val state by model.state.collectAsStateWithLifecycle()
    Scaffold { padding ->
        NavHost(nav, startDestination = "search", modifier = Modifier.padding(padding)) {
            composable("search") {
                SearchScreen(state, model::edit, model::retry) { book ->
                    model.select(book)
                    nav.navigate("detail") { launchSingleTop = true }
                }
            }
            composable("detail") { DetailScreen(model.selected) { nav.popBackStack() } }
        }
    }
}

@Composable
fun SearchScreen(state: SearchState, edit: (String) -> Unit, retry: () -> Unit, select: (Book) -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize().imePadding().testTag("search_screen"),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text("Shelf", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(28.dp))
            Text(stringResource(R.string.eyebrow), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.headline), style = MaterialTheme.typography.headlineLarge,
                fontFamily = FontFamily.Serif, modifier = Modifier.semantics { heading() })
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.intro), style = MaterialTheme.typography.bodyLarge)
        }
        item {
            OutlinedTextField(state.query, edit,
                modifier = Modifier.fillMaxWidth().testTag("search_input"),
                label = { Text(stringResource(R.string.query_label)) },
                singleLine = true, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                trailingIcon = if (state.query.isNotEmpty()) ({
                    TextButton(onClick = { edit("") }) { Text(stringResource(R.string.clear)) }
                }) else null)
        }
        when (val content = state.content) {
            SearchContent.Idle -> item { Message(R.string.initial_title, R.string.initial_body) }
            SearchContent.Loading -> item {
                Row(Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    Text(stringResource(R.string.loading))
                }
            }
            is SearchContent.Failed -> item {
                Column(Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
                    Text(stringResource(when (content.reason) {
                        Failure.NETWORK -> R.string.network_error
                        Failure.RATE_LIMIT -> R.string.rate_error
                        Failure.SERVICE -> R.string.service_error
                        Failure.INVALID_QUERY -> R.string.query_error
                        Failure.UNEXPECTED -> R.string.unexpected_error
                    }), color = MaterialTheme.colorScheme.error)
                    if (content.reason != Failure.INVALID_QUERY) {
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = retry, modifier = Modifier.testTag("retry")) { Text(stringResource(R.string.retry)) }
                    }
                }
            }
            is SearchContent.Results -> {
                if (content.result.books.isEmpty()) item { Box(Modifier.testTag("empty_state")) { Message(R.string.empty_title, R.string.empty_body) } }
                else {
                    item {
                        Text(stringResource(if (content.result.source == ResultSource.CACHE)
                            R.string.results_cache else R.string.results_network),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                    }
                    items(content.result.books, key = { it.id }) { book ->
                        OutlinedCard(onClick = { select(book) }, modifier = Modifier.fillMaxWidth().testTag("book_${book.id}")) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(book.title, style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif)
                                Text(book.authors.joinToString().ifEmpty { stringResource(R.string.unknown_author) },
                                    style = MaterialTheme.typography.bodyMedium)
                                book.firstPublished?.let { Text(it.toString(), style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary) }
                            }
                        }
                    }
                }
                item { Text(stringResource(R.string.results_limit), style = MaterialTheme.typography.bodySmall) }
            }
        }
    }
}

@Composable
private fun Message(title: Int, body: Int) {
    Column(Modifier.padding(vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(title), style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif)
        Text(stringResource(body), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun DetailScreen(book: Book?, back: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp).testTag("detail_screen"),
        verticalArrangement = Arrangement.spacedBy(24.dp)) {
        TextButton(back, modifier = Modifier.testTag("back")) { Text(stringResource(R.string.back)) }
        if (book == null) Text(stringResource(R.string.missing_detail))
        else {
            Text(stringResource(R.string.catalogue_record), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary)
            Text(book.title, style = MaterialTheme.typography.headlineLarge, fontFamily = FontFamily.Serif,
                modifier = Modifier.semantics { heading() })
            HorizontalDivider()
            Field(R.string.author, book.authors.joinToString().ifEmpty { stringResource(R.string.unknown_author) })
            Field(R.string.year, book.firstPublished?.toString() ?: stringResource(R.string.unknown))
            Field(R.string.editions, book.editionCount?.toString() ?: stringResource(R.string.unknown))
            Field(R.string.identifier, book.id.removePrefix("/works/"))
            HorizontalDivider()
            Text(stringResource(R.string.detail_note), style = MaterialTheme.typography.bodySmall)
        }
    }
}
@Composable
private fun Field(label: Int, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
