package com.panosdim.moneytrack.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.models.Category
import com.panosdim.moneytrack.models.Expense
import com.panosdim.moneytrack.models.Income
import com.panosdim.moneytrack.models.LoginRequest
import com.panosdim.moneytrack.models.LoginResponse
import com.panosdim.moneytrack.models.Response
import com.panosdim.moneytrack.paddingLarge
import com.panosdim.moneytrack.paddingSmall
import com.panosdim.moneytrack.prefs
import com.panosdim.moneytrack.rest.client
import com.panosdim.moneytrack.ui.theme.CategoryColor
import com.panosdim.moneytrack.ui.theme.ExpenseColor
import com.panosdim.moneytrack.ui.theme.IncomeColor
import com.panosdim.moneytrack.utils.DisposableEffectWithLifecycle
import com.panosdim.moneytrack.utils.extractEmojis
import com.panosdim.moneytrack.utils.isJWTExpired
import com.panosdim.moneytrack.utils.moneyFormat
import com.panosdim.moneytrack.utils.removeEmojis
import com.panosdim.moneytrack.viewmodels.CategoriesViewModel
import com.panosdim.moneytrack.viewmodels.ExpensesViewModel
import com.panosdim.moneytrack.viewmodels.IncomeViewModel
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val skipPartiallyExpanded by remember { mutableStateOf(true) }
    val expensesOnCategorySheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )

    val today = LocalDate.now()
    var startOfMonth by remember { mutableStateOf(today.withDayOfMonth(1)) }
    var endOfMonth by remember { mutableStateOf(today.withDayOfMonth(today.lengthOfMonth())) }
    var startOfYear by remember { mutableStateOf(today.withDayOfMonth(1).withMonth(1)) }
    var endOfYear by remember { mutableStateOf(today.withMonth(12).withDayOfMonth(31)) }
    var selectedMonth by remember { mutableStateOf(today.month) }
    var selectedYear by remember { mutableIntStateOf(today.year) }

    var totalMonthSavings by remember { mutableStateOf(BigDecimal(0)) }
    var totalYearSavings by remember { mutableStateOf(BigDecimal(0)) }

    var totalSavings by remember { mutableStateOf(BigDecimal(0)) }

    var monthExpensesPerCategories by remember { mutableStateOf(emptyList<Pair<Float, String>>()) }
    var expensesOnCategory: List<Expense> by remember {
        mutableStateOf(emptyList())
    }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    val expensesViewModel: ExpensesViewModel = viewModel()
    val categoriesViewModel: CategoriesViewModel = viewModel()
    val incomeViewModel: IncomeViewModel = viewModel()

    val optionsMonth = Month.entries.toTypedArray()
    val optionsYear =
        expensesViewModel.years().collectAsStateWithLifecycle(initialValue = emptyList())
    var expandedMonth by remember { mutableStateOf(false) }
    var expandedYear by remember { mutableStateOf(false) }

    var isLoadingExpenses by remember {
        mutableStateOf(false)
    }

    var isLoadingIncome by remember {
        mutableStateOf(false)
    }

    var isLoadingCategories by remember {
        mutableStateOf(false)
    }

    val isLoading by remember {
        derivedStateOf { isLoadingExpenses || isLoadingCategories || isLoadingIncome || isJWTExpired() }
    }

    var income by remember { mutableStateOf(emptyList<Income>()) }
    var expenses by remember { mutableStateOf(emptyList<Expense>()) }
    var categories by remember { mutableStateOf(emptyList<Category>()) }

    DisposableEffectWithLifecycle(
        onResume = {
            if (isJWTExpired()) {
                scope.launch {
                    runCatching {
                        client.post("login") {
                            contentType(ContentType.Application.Json)
                            setBody(LoginRequest())
                        }.body<LoginResponse>()
                    }.onSuccess {
                        prefs.token = it.token
                    }
                }
            }
        }
    )

    fun calculateSavings() {
        val totalMonthIncome = income.filter {
            val date = LocalDate.parse(it.date)
            (date.isAfter(startOfMonth) || date.isEqual(startOfMonth)) &&
                    (date.isBefore(endOfMonth) || date.isEqual(endOfMonth))
        }.sumOf { it.amount.toBigDecimal() }

        val totalMonthExpenses = expenses.filter {
            val date = LocalDate.parse(it.date)
            (date.isAfter(startOfMonth) || date.isEqual(startOfMonth)) &&
                    (date.isBefore(endOfMonth) || date.isEqual(endOfMonth))
        }.sumOf { it.amount.toBigDecimal() }

        totalMonthSavings = totalMonthIncome - totalMonthExpenses

        val totalYearIncome = income.filter {
            val date = LocalDate.parse(it.date)
            (date.isAfter(startOfYear) || date.isEqual(startOfYear)) &&
                    (date.isBefore(endOfYear) || date.isEqual(endOfYear))
        }.sumOf { it.amount.toBigDecimal() }

        val totalYearExpenses = expenses.filter {
            val date = LocalDate.parse(it.date)
            (date.isAfter(startOfYear) || date.isEqual(startOfYear)) &&
                    (date.isBefore(endOfYear) || date.isEqual(endOfYear))
        }.sumOf { it.amount.toBigDecimal() }

        totalYearSavings = totalYearIncome - totalYearExpenses

        val totalIncome = income.sumOf { it.amount.toBigDecimal() }
        val totalExpenses = expenses.sumOf { it.amount.toBigDecimal() }
        totalSavings = totalIncome - totalExpenses
    }

    if (!isJWTExpired()) {
        val expensesResponse =
            expensesViewModel.expenses.collectAsStateWithLifecycle(initialValue = Response.Loading)

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

        val incomeResponse =
            incomeViewModel.income.collectAsStateWithLifecycle(initialValue = Response.Loading)

        when (incomeResponse.value) {
            is Response.Success -> {
                isLoadingIncome = false

                income =
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

        calculateSavings()
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingLarge)
                .wrapContentSize(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = paddingLarge, end = paddingLarge),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedMonth,
                    onExpandedChange = { expandedMonth = !expandedMonth },
                ) {
                    ElevatedFilterChip(
                        modifier = Modifier
                            .menuAnchor(),
                        selected = false,
                        onClick = { },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                        label = {
                            Text(selectedMonth.name.lowercase()
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() })
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMonth,
                        onDismissRequest = { expandedMonth = false },
                    ) {
                        optionsMonth.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        selectionOption.name.lowercase().replaceFirstChar {
                                            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                                        }
                                    )
                                },
                                onClick = {
                                    selectedMonth = selectionOption
                                    val s = LocalDate.of(selectedYear, selectedMonth, 1)
                                    startOfMonth = s.withDayOfMonth(1)
                                    endOfMonth = s.withDayOfMonth(s.lengthOfMonth())
                                    expandedMonth = false
                                    calculateSavings()
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = expandedYear,
                    onExpandedChange = { expandedYear = !expandedYear },
                ) {
                    ElevatedFilterChip(
                        modifier = Modifier
                            .menuAnchor(),
                        selected = false,
                        onClick = { },
                        label = { Text(selectedYear.toString()) },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedYear) },
                    )
                    ExposedDropdownMenu(
                        expanded = expandedYear,
                        onDismissRequest = { expandedYear = false },
                    ) {
                        optionsYear.value.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption.toString()) },
                                onClick = {
                                    selectedYear = selectionOption
                                    val s = LocalDate.of(selectedYear, selectedMonth, 1)
                                    startOfYear = s.withDayOfMonth(1).withMonth(1)
                                    endOfYear = s.withMonth(12).withDayOfMonth(31)
                                    startOfMonth = s.withDayOfMonth(1)
                                    endOfMonth = s.withDayOfMonth(s.lengthOfMonth())
                                    expandedYear = false
                                    calculateSavings()
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .padding(paddingSmall)
                    .fillMaxWidth()
                    .padding(top = paddingLarge)
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(Modifier.padding(paddingLarge)) {
                    Text(
                        text = stringResource(id = R.string.savings_overview),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .fillMaxWidth(),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(id = R.string.month_savings),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = moneyFormat(totalMonthSavings),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleLarge,
                                color = if (totalMonthSavings > BigDecimal(0)) {
                                    IncomeColor
                                } else {
                                    ExpenseColor
                                }
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(id = R.string.year_savings),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = moneyFormat(totalYearSavings),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleLarge,
                                color = if (totalYearSavings > BigDecimal(0)) {
                                    IncomeColor
                                } else {
                                    ExpenseColor
                                }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.total_savings),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Spacer(modifier = Modifier.padding(horizontal = paddingSmall))
                        Text(
                            text = moneyFormat(totalSavings),
                            style = MaterialTheme.typography.headlineSmall,
                            color = if (totalSavings > BigDecimal(0)) {
                                IncomeColor
                            } else {
                                ExpenseColor
                            }
                        )
                    }
                }
            }

            // Categories Aggregated Expenses
            monthExpensesPerCategories = expenses.filter {
                val date = LocalDate.parse(it.date)
                (date.isAfter(startOfMonth) || date.isEqual(startOfMonth)) &&
                        (date.isBefore(endOfMonth) || date.isEqual(endOfMonth))
            }
                .groupBy { it.category }
                .map { (k, v) ->
                    Pair(
                        v.sumOf { it.amount.toBigDecimal() }.toFloat(),
                        categories.find { it.id == k }?.category ?: ""
                    )

                }
                .sortedByDescending { it.first }

            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .padding(paddingLarge),
                state = listState
            ) {
                items(monthExpensesPerCategories) { item ->
                    ListItem(
                        headlineContent = {
                            Text(
                                removeEmojis(item.second),
                                color = CategoryColor
                            )
                        },
                        leadingContent = {
                            Text(
                                extractEmojis(item.second),
                                style = MaterialTheme.typography.headlineLarge
                            )
                        },
                        trailingContent = {
                            Text(
                                text = moneyFormat(item.first),
                                style = MaterialTheme.typography.headlineSmall,
                                color = ExpenseColor
                            )
                        },
                        modifier = Modifier.clickable {
                            selectedCategory = categories.find { it.category == item.second }
                            selectedCategory?.let { selectedCategory ->
                                expensesOnCategory = expenses.filter {
                                    val date = LocalDate.parse(it.date)
                                    (date.isAfter(startOfMonth) || date.isEqual(startOfMonth)) &&
                                            (date.isBefore(endOfMonth) || date.isEqual(endOfMonth))
                                }.filter { it.category == selectedCategory.id }
                                    .sortedByDescending { it.amount }

                                scope.launch { expensesOnCategorySheetState.show() }
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }

        ExpensesOnCategorySheet(
            bottomSheetState = expensesOnCategorySheetState,
            selectedCategory?.category ?: "",
            expensesOnCategory
        )
    }
}