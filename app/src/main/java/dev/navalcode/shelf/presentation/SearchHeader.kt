@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package dev.navalcode.shelf.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.navalcode.shelf.R

/** A stable input node across idle/loading/results, so typing never replaces keyboard focus. */
@Composable
internal fun SearchHeader(
    state: SearchState,
    compact: Boolean,
    edit: (String) -> Unit,
    submit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val opening = state.content == SearchContent.Idle && !compact
    val keyboard = LocalSoftwareKeyboardController.current
    Surface(
        modifier.testTag("search_controls"),
        shape = if (opening) MaterialTheme.shapes.large else RectangleShape,
        color =
            if (opening) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface,
        contentColor =
            if (opening) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            Modifier.padding(if (opening) 24.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                stringResource(R.string.headline),
                style =
                    if (opening) MaterialTheme.typography.displayMedium
                    else MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
            OutlinedTextField(
                state.query,
                edit,
                Modifier.fillMaxWidth().testTag("search_input"),
                label = { Text(stringResource(R.string.query_label)) },
                shape = MaterialTheme.shapes.medium,
                leadingIcon = {
                    Icon(painterResource(R.drawable.ic_search), contentDescription = null)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions =
                    KeyboardActions(
                        onSearch = {
                            submit()
                            keyboard?.hide()
                        },
                    ),
                trailingIcon =
                    if (state.query.isNotEmpty())
                        ({
                            IconButton({ edit("") }, Modifier.size(48.dp).testTag("clear_search")) {
                                Icon(
                                    painterResource(R.drawable.ic_clear),
                                    stringResource(R.string.clear),
                                )
                            }
                        })
                    else null,
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedTextColor = LocalContentColor.current,
                        unfocusedTextColor = LocalContentColor.current,
                        unfocusedLabelColor = LocalContentColor.current,
                        unfocusedLeadingIconColor = LocalContentColor.current,
                        unfocusedBorderColor =
                            if (opening) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.outline,
                    ),
            )
            if (opening)
                Text(stringResource(R.string.intro), style = MaterialTheme.typography.bodyLarge)
        }
    }
}
