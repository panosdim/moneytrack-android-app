package com.panosdim.moneytrack.ui.expenses

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.models.Category
import com.panosdim.moneytrack.models.Expense
import com.panosdim.moneytrack.models.LoginResponse
import com.panosdim.moneytrack.models.Response
import com.panosdim.moneytrack.paddingLarge
import com.panosdim.moneytrack.prefs
import com.panosdim.moneytrack.rest.client
import com.panosdim.moneytrack.ui.HeaderBar
import com.panosdim.moneytrack.utils.DisposableEffectWithLifecycle
import com.panosdim.moneytrack.utils.ExpenseSortField
import com.panosdim.moneytrack.utils.SortDirection
import com.panosdim.moneytrack.utils.filter
import com.panosdim.moneytrack.utils.isJWTExpired
import com.panosdim.moneytrack.utils.moneyFormat
import com.panosdim.moneytrack.utils.sort
import com.panosdim.moneytrack.utils.unaccent
import com.panosdim.moneytrack.viewmodels.CategoriesViewModel
import com.panosdim.moneytrack.viewmodels.ExpensesFilterViewModel
import com.panosdim.moneytrack.viewmodels.ExpensesSortViewModel
import com.panosdim.moneytrack.viewmodels.ExpensesViewModel
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ExpensesScreen() {
    val context = LocalContext.current
    val resources = context.resources
    val scope = rememberCoroutineScope()
    val expensesSortViewModel: ExpensesSortViewModel = viewModel()
    val expensesFilterViewModel: ExpensesFilterViewModel = viewModel()
    val expensesViewModel: ExpensesViewModel = viewModel()
    val categoriesViewModel: CategoriesViewModel = viewModel()
    val listState = rememberLazyListState()
    val expandedFab by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }
    val skipPartiallyExpanded by remember { mutableStateOf(true) }
    val expensesSortSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val expensesFilterSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )

    val addExpenseSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val editExpenseSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )

    val expenseSortField =
        expensesSortViewModel.expenseSortField.collectAsStateWithLifecycle(initialValue = ExpenseSortField.DATE)

    val sortDirection =
        expensesSortViewModel.sortDirection.collectAsStateWithLifecycle(initialValue = SortDirection.DESC)

    val dateFilter =
        expensesFilterViewModel.filterDate.collectAsStateWithLifecycle(initialValue = null)
    val categoryFilter =
        expensesFilterViewModel.filterCategory.collectAsStateWithLifecycle(initialValue = null)
    val isFilterSet by remember {
        derivedStateOf {
            dateFilter.value != null || categoryFilter.value != null
        }
    }

    var isLoadingExpenses by remember {
        mutableStateOf(false)
    }

    var isLoadingCategories by remember {
        mutableStateOf(false)
    }

    var isJWTExpired by remember {
        mutableStateOf(false)
    }

    val isLoading by remember {
        derivedStateOf { isLoadingExpenses || isLoadingCategories || isJWTExpired }
    }

    var searchText by rememberSaveable { mutableStateOf("") }

    var expenses by remember { mutableStateOf(emptyList<Expense>()) }
    var categories by remember { mutableStateOf(emptyList<Category>()) }
    var expense: Expense? by remember { mutableStateOf(null) }

    DisposableEffectWithLifecycle(
        onResume = {
            if (isJWTExpired()) {
                isJWTExpired = true
                scope.launch {
                    kotlin.runCatching {
                        client.post("login") {
                            contentType(io.ktor.http.ContentType.Application.Json)
                            setBody(com.panosdim.moneytrack.models.LoginRequest())
                        }.body<LoginResponse>()
                    }.onSuccess {
                        prefs.token = it.token
                        isJWTExpired = false
                    }
                }
            }
        }
    )

    if (!isJWTExpired) {
        val expensesResponse =
            expensesViewModel.expenses.collectAsStateWithLifecycle(initialValue = Response.Loading)

        val categoriesResponse =
            categoriesViewModel.categories.collectAsStateWithLifecycle(initialValue = Response.Loading)

        when (expensesResponse.value) {
            is Response.Success -> {
                isLoadingExpenses = false

                expenses =
                    (expensesResponse.value as Response.Success<List<Expense>>).data
            }

            is Response.Error -> {
                Toast.makeText(
                    context,
                    (expensesResponse.value as Response.Error).errorMessage,
                    Toast.LENGTH_SHORT
                )
                    .show()

                isLoadingExpenses = false
            }

            is Response.Loading -> {
                isLoadingExpenses = true
            }
        }

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

    val refreshing by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    fun refresh() = scope.launch {
        isRefreshing = true
        expensesViewModel.fetchAllExpenses().collect {
            expenses = it
            isRefreshing = false
        }
    }

    val state = rememberPullRefreshState(refreshing, ::refresh)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    ) {
        if (isLoading || isRefreshing) {
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
                        onClick = { scope.launch { addExpenseSheetState.show() } },
                        expanded = expandedFab,
                        icon = {
                            Icon(
                                Icons.Filled.Add,
                                stringResource(id = R.string.add_expense)
                            )
                        },
                        text = { Text(text = stringResource(id = R.string.add_expense)) },
                    )
                }

            ) { contentPadding ->
                Column {
                    HeaderBar(
                        onSort = { scope.launch { expensesSortSheetState.show() } },
                        listToSearch = expenses,
                        searchQuery = searchText,
                        onSearchChanged = { searchText = it },
                        isFilterSet = isFilterSet
                    ) {
                        scope.launch { expensesFilterSheetState.show() }
                    }

                    // Back to top button
                    if (!expandedFab) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = paddingLarge, end = paddingLarge),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(onClick = { scope.launch { listState.animateScrollToItem(0) } }) {
                                Icon(
                                    Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text(
                                    stringResource(id = R.string.back_to_top)
                                )
                            }
                        }
                    }

                    Box(Modifier.pullRefresh(state)) {
                        // Show expenses
                        LazyColumn(
                            Modifier
                                .fillMaxWidth()
                                .padding(paddingLarge),
                            contentPadding = contentPadding,
                            state = listState
                        ) {
                            if (expenseSortField.value == ExpenseSortField.DATE && searchText.isBlank() && !isFilterSet) {
                                val data = sort(
                                    expenses,
                                    categories,
                                    expenseSortField.value,
                                    sortDirection.value
                                ).groupBy { it.date }

                                if (data.isNotEmpty()) {
                                    data.iterator().forEachRemaining {
                                        item {
                                            ExpenseCardAggByDate(it.key, it.value, categories) {
                                                expense = it
                                                scope.launch { editExpenseSheetState.show() }
                                            }
                                        }
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
                                                contentDescription = stringResource(id = R.string.no_expenses),
                                                modifier = Modifier

                                            )
                                            Text(
                                                text = stringResource(id = R.string.no_expenses)
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Sort
                                var data = sort(
                                    expenses,
                                    categories,
                                    expenseSortField.value,
                                    sortDirection.value
                                )

                                // Filter
                                data = filter(data, dateFilter.value, categoryFilter.value)

                                // Search
                                if (searchText.isNotBlank()) {
                                    data = data.filter {
                                        it.comment.unaccent().contains(
                                            searchText.unaccent().trim(),
                                            ignoreCase = true
                                        )
                                    }
                                }

                                if (data.isNotEmpty()) {
                                    items(data) { expenseItem ->
                                        ExpenseListItem(expenseItem, categories) {
                                            expense = it
                                            scope.launch { editExpenseSheetState.show() }
                                        }
                                    }

                                    item {
                                        // Calculate total cost
                                        Text(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = paddingLarge),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.headlineMedium,
                                            text = resources.getString(
                                                R.string.total,
                                                moneyFormat(data.fold(0f) { acc, expenseDetails -> acc + expenseDetails.amount })
                                            )
                                        )
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
                                                contentDescription = stringResource(id = R.string.no_expenses),
                                                modifier = Modifier

                                            )
                                            Text(
                                                text = stringResource(id = R.string.no_expenses)
                                            )
                                        }
                                    }
                                }
                            }

                            item { Spacer(modifier = Modifier.padding(bottom = 64.dp)) }
                        }

                        PullRefreshIndicator(
                            refreshing,
                            state,
                            Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
            }

            ExpensesSortSheet(bottomSheetState = expensesSortSheetState)
            AddExpenseSheet(
                categories,
                bottomSheetState = addExpenseSheetState,
            )

            ExpensesFilterSheet(
                bottomSheetState = expensesFilterSheetState,
                categories = categories
            )

            expense?.let {
                EditExpenseSheet(
                    it,
                    categories,
                    bottomSheetState = editExpenseSheetState,
                )
            }
        }
    }
}