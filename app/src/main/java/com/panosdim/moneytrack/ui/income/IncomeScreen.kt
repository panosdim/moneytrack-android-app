package com.panosdim.moneytrack.ui.income

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
import com.panosdim.moneytrack.models.Income
import com.panosdim.moneytrack.models.LoginResponse
import com.panosdim.moneytrack.models.Response
import com.panosdim.moneytrack.paddingLarge
import com.panosdim.moneytrack.prefs
import com.panosdim.moneytrack.rest.client
import com.panosdim.moneytrack.ui.HeaderBar
import com.panosdim.moneytrack.utils.DisposableEffectWithLifecycle
import com.panosdim.moneytrack.utils.IncomeSortField
import com.panosdim.moneytrack.utils.SortDirection
import com.panosdim.moneytrack.utils.filter
import com.panosdim.moneytrack.utils.formatDate
import com.panosdim.moneytrack.utils.isJWTExpired
import com.panosdim.moneytrack.utils.moneyFormat
import com.panosdim.moneytrack.utils.sort
import com.panosdim.moneytrack.utils.unaccent
import com.panosdim.moneytrack.viewmodels.IncomeFilterViewModel
import com.panosdim.moneytrack.viewmodels.IncomeSortViewModel
import com.panosdim.moneytrack.viewmodels.IncomeViewModel
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun IncomeScreen() {
    val context = LocalContext.current
    val resources = context.resources
    val scope = rememberCoroutineScope()
    val incomeSortViewModel: IncomeSortViewModel = viewModel()
    val incomeFilterViewModel: IncomeFilterViewModel = viewModel()
    val incomeViewModel: IncomeViewModel = viewModel()
    val listState = rememberLazyListState()
    val expandedFab by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }
    val skipPartiallyExpanded by remember { mutableStateOf(true) }
    val incomeSortSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val incomeFilterSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )

    val addIncomeSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val editIncomeSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )

    val incomeSortField =
        incomeSortViewModel.incomeSortField.collectAsStateWithLifecycle(initialValue = IncomeSortField.DATE)

    val sortDirection =
        incomeSortViewModel.sortDirection.collectAsStateWithLifecycle(initialValue = SortDirection.DESC)

    val dateFilter =
        incomeFilterViewModel.filterDate.collectAsStateWithLifecycle(initialValue = null)

    val isFilterSet by remember {
        derivedStateOf {
            dateFilter.value != null
        }
    }

    var isLoadingIncome by remember {
        mutableStateOf(false)
    }

    var isTokenExpired by remember {
        mutableStateOf(isJWTExpired())
    }

    val isLoading by remember {
        derivedStateOf { isLoadingIncome || isTokenExpired }
    }

    var searchText by rememberSaveable { mutableStateOf("") }

    var incomeList by remember { mutableStateOf(emptyList<Income>()) }
    var income: Income? by remember { mutableStateOf(null) }

    DisposableEffectWithLifecycle(
        onResume = {
            if (isTokenExpired) {
                scope.launch {
                    kotlin.runCatching {
                        client.post("login") {
                            contentType(io.ktor.http.ContentType.Application.Json)
                            setBody(com.panosdim.moneytrack.models.LoginRequest())
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
        val incomeResponse =
            incomeViewModel.income.collectAsStateWithLifecycle(initialValue = Response.Loading)

        when (incomeResponse.value) {
            is Response.Success -> {
                isLoadingIncome = false

                incomeList =
                    (incomeResponse.value as Response.Success<List<Income>>).data
            }

            is Response.Error -> {
                Toast.makeText(
                    context,
                    (incomeResponse.value as Response.Error).errorMessage,
                    Toast.LENGTH_SHORT
                )
                    .show()

                isLoadingIncome = false
            }

            is Response.Loading -> {
                isLoadingIncome = true
            }
        }
    }

    val refreshing by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    fun refresh() = scope.launch {
        isRefreshing = true
        incomeViewModel.fetchAllIncome().collect {
            incomeList = it
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
                        onClick = { scope.launch { addIncomeSheetState.show() } },
                        expanded = expandedFab,
                        icon = {
                            Icon(
                                Icons.Filled.Add,
                                stringResource(id = R.string.add_income)
                            )
                        },
                        text = { Text(text = stringResource(id = R.string.add_income)) },
                    )
                }

            ) { contentPadding ->
                Column {
                    HeaderBar(
                        onSort = { scope.launch { incomeSortSheetState.show() } },
                        listToSearch = incomeList,
                        searchQuery = searchText,
                        onSearchChanged = { searchText = it },
                        isFilterSet = isFilterSet
                    ) {
                        scope.launch { incomeFilterSheetState.show() }
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
                        // Show income
                        LazyColumn(
                            Modifier
                                .fillMaxWidth()
                                .padding(paddingLarge),
                            contentPadding = contentPadding,
                            state = listState
                        ) {
                            if (incomeSortField.value == IncomeSortField.DATE && searchText.isBlank() && !isFilterSet) {
                                val data = sort(
                                    incomeList,
                                    incomeSortField.value,
                                    sortDirection.value
                                ).groupBy {
                                    it.date.formatDate(
                                        DateTimeFormatter.ofPattern("MMMM yyyy"),
                                        false
                                    )
                                }

                                if (data.isNotEmpty()) {
                                    data.iterator().forEachRemaining {
                                        item {
                                            IncomeCardAggByDate(it.key, it.value) {
                                                income = it
                                                scope.launch { editIncomeSheetState.show() }
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
                                                contentDescription = stringResource(id = R.string.no_income),
                                                modifier = Modifier

                                            )
                                            Text(
                                                text = stringResource(id = R.string.no_income)
                                            )
                                        }
                                    }
                                }
                            } else {
                                // Sort
                                var data = sort(
                                    incomeList,
                                    incomeSortField.value,
                                    sortDirection.value
                                )

                                // Filter
                                data = filter(data, dateFilter.value)

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
                                    items(data) { incomeItem ->
                                        IncomeListItem(incomeItem) {
                                            income = it
                                            scope.launch { editIncomeSheetState.show() }
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
                                                moneyFormat(data.fold(0f) { acc, incomeDetails -> acc + incomeDetails.amount })
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
                                                contentDescription = stringResource(id = R.string.no_income),
                                                modifier = Modifier

                                            )
                                            Text(
                                                text = stringResource(id = R.string.no_income)
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

            IncomeSortSheet(bottomSheetState = incomeSortSheetState)
            AddIncomeSheet(bottomSheetState = addIncomeSheetState)

            IncomeFilterSheet(bottomSheetState = incomeFilterSheetState)

            income?.let {
                EditIncomeSheet(
                    it,
                    bottomSheetState = editIncomeSheetState
                )
            }
        }
    }
}