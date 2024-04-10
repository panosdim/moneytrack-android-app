package com.panosdim.moneytrack.ui.categories

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.models.Category
import com.panosdim.moneytrack.models.LoginRequest
import com.panosdim.moneytrack.models.LoginResponse
import com.panosdim.moneytrack.models.Response
import com.panosdim.moneytrack.paddingLarge
import com.panosdim.moneytrack.paddingSmall
import com.panosdim.moneytrack.prefs
import com.panosdim.moneytrack.rest.client
import com.panosdim.moneytrack.utils.DisposableEffectWithLifecycle
import com.panosdim.moneytrack.utils.extractEmojis
import com.panosdim.moneytrack.utils.isJWTExpired
import com.panosdim.moneytrack.utils.removeEmojis
import com.panosdim.moneytrack.viewmodels.CategoriesViewModel
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val categoriesViewModel: CategoriesViewModel = viewModel()
    val listState = rememberLazyListState()
    val expandedFab by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }
    var category: Category? by remember { mutableStateOf(null) }

    val skipPartiallyExpanded by remember { mutableStateOf(true) }
    val addCategorySheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val editCategorySheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )

    var isLoadingCategories by remember {
        mutableStateOf(false)
    }

    var isTokenExpired by remember {
        mutableStateOf(isJWTExpired())
    }

    val isLoading by remember {
        derivedStateOf { isLoadingCategories || isTokenExpired }
    }

    var searchText by rememberSaveable { mutableStateOf("") }
    var active by rememberSaveable { mutableStateOf(false) }

    var categories by remember { mutableStateOf(emptyList<Category>()) }

    DisposableEffectWithLifecycle(
        onResume = {
            if (isTokenExpired) {
                scope.launch {
                    kotlin.runCatching {
                        client.post("login") {
                            contentType(ContentType.Application.Json)
                            setBody(LoginRequest())
                        }.body<LoginResponse>()
                    }.onSuccess {
                        prefs.token = it.token
                        isTokenExpired = isJWTExpired()
                    }
                }
            }
        }
    )

    if (!isTokenExpired) {
        val categoriesResponse =
            categoriesViewModel.categories.collectAsStateWithLifecycle(initialValue = Response.Loading)

        when (categoriesResponse.value) {
            is Response.Success -> {
                isLoadingCategories = false

                categories =
                    (categoriesResponse.value as Response.Success<List<Category>>).data
            }

            is Response.Error -> {
                Toast.makeText(
                    context,
                    (categoriesResponse.value as Response.Error).errorMessage,
                    Toast.LENGTH_SHORT
                )
                    .show()

                isLoadingCategories = false
            }

            is Response.Loading -> {
                isLoadingCategories = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
            .imePadding()
    ) {
        if (isLoading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else {
            Scaffold(
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        onClick = { scope.launch { addCategorySheetState.show() } },
                        expanded = expandedFab,
                        icon = {
                            Icon(
                                Icons.Filled.Add,
                                stringResource(id = R.string.add_category)
                            )
                        },
                        text = { Text(text = stringResource(id = R.string.add_category)) },
                    )
                }
            ) { contentPadding ->
                Column {
                    SearchBar(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(paddingLarge),
                        query = searchText,
                        onQueryChange = { searchText = it },
                        onSearch = { active = false },
                        active = active,
                        onActiveChange = {
                            active = it
                        },
                        placeholder = { Text(stringResource(id = R.string.category_search)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = null,
                                    modifier = Modifier.clickable {
                                        searchText = ""
                                        active = false
                                    })
                            }
                        }
                    ) {
                        categories.filter {
                            removeEmojis(it.category).contains(searchText, ignoreCase = true)
                        }.forEach {
                            ListItem(
                                headlineContent = { Text(it.category) },
                                modifier = Modifier
                                    .clickable {
                                        searchText = removeEmojis(it.category)
                                        active = false
                                    }
                                    .fillMaxWidth()
                                    .padding(horizontal = paddingLarge, vertical = paddingSmall)
                            )
                        }
                    }

                    // Show Categories
                    LazyColumn(
                        Modifier
                            .fillMaxWidth()
                            .padding(paddingLarge),
                        contentPadding = contentPadding,
                        state = listState
                    ) {
                        val data =
                            if (searchText.isNotEmpty()) {
                                categories.sortedByDescending { it.count }.filter {
                                    removeEmojis(it.category).contains(
                                        searchText,
                                        ignoreCase = true
                                    )
                                }
                            } else {
                                categories.sortedByDescending { it.count }
                            }

                        if (data.isNotEmpty()) {
                            items(data) { categoryItem ->
                                ListItem(
                                    headlineContent = {
                                        Text(
                                            removeEmojis(categoryItem.category),
                                            style = MaterialTheme.typography.headlineSmall,
                                        )
                                    },
                                    leadingContent = {
                                        Text(
                                            extractEmojis(categoryItem.category),
                                            style = MaterialTheme.typography.headlineLarge
                                        )
                                    },
                                    modifier = Modifier.clickable {
                                        category = categoryItem
                                        scope.launch { editCategorySheetState.show() }
                                    }
                                )
                                HorizontalDivider()
                            }
                        } else {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = stringResource(id = R.string.no_categories),
                                        modifier = Modifier

                                    )
                                    Text(
                                        text = stringResource(id = R.string.no_categories)
                                    )
                                }
                            }
                        }
                        item { Spacer(modifier = Modifier.padding(bottom = 64.dp)) }
                    }
                }
            }

            AddCategorySheet(
                categories,
                bottomSheetState = addCategorySheetState,
            )

            category?.let {
                EditCategorySheet(
                    it,
                    categories,
                    bottomSheetState = editCategorySheetState,
                )
            }
        }
    }
}