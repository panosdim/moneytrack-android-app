package com.panosdim.moneytrack.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.paddingLarge
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderBar(
    onSort: () -> Unit,
    isFilterSet: Boolean,
    showBackToTop: Boolean,
    listState: LazyListState,
    onFilter: () -> Unit,
    onSearch: (String?) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var searchText by rememberSaveable { mutableStateOf("") }

    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = paddingLarge),
        inputField = {
            SearchBarDefaults.InputField(
                expanded = false,
                onExpandedChange = {},
                placeholder = { Text(stringResource(id = R.string.comment_search)) },
                leadingIcon = {
                    if (searchText.isNotEmpty()) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.clickable {
                                searchText = ""
                                onSearch(null)
                            })
                    } else {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null
                        )
                    }
                },
                trailingIcon = {
                    Row {
                        if (showBackToTop) {
                            IconButton(onClick = { scope.launch { listState.animateScrollToItem(0) } }) {
                                Icon(
                                    Icons.Default.ArrowUpward,
                                    contentDescription = null
                                )
                            }
                        }
                        IconButton(onClick = { onSort() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = null
                            )
                        }
                        IconButton(
                            onClick = { onFilter() },
                        ) {
                            Icon(
                                imageVector = if (isFilterSet) {
                                    Icons.Default.FilterAlt
                                } else {
                                    Icons.Default.FilterAltOff
                                },
                                contentDescription = null
                            )
                        }
                    }
                },
                query = searchText,
                onQueryChange = {
                    searchText = it
                    onSearch(searchText)
                },
                onSearch = {}
            )
        },
        expanded = false,
        onExpandedChange = {}
    ) {}
}