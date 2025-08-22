package com.panosdim.moneytrack.ui.expenses

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.models.Category
import com.panosdim.moneytrack.paddingLarge
import com.panosdim.moneytrack.ui.OutlinedDateRangePicker
import com.panosdim.moneytrack.utils.toEpochMilli
import com.panosdim.moneytrack.viewmodels.ExpensesFilterViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExpensesFilterSheet(
    bottomSheetState: SheetState,
    categories: List<Category>
) {
    val scope = rememberCoroutineScope()
    val expensesFilterViewModel: ExpensesFilterViewModel = viewModel()

    val dateFilter =
        expensesFilterViewModel.filterDate.collectAsStateWithLifecycle(initialValue = null)
    val categoryFilter =
        expensesFilterViewModel.filterCategory.collectAsStateWithLifecycle(initialValue = null)

    // Sheet content
    if (bottomSheetState.isVisible) {
        val selectedCategories =
            remember { mutableStateListOf<Category>().apply { categoryFilter.value?.let { addAll(it) } } }

        val dateRangePickerState =
            rememberDateRangePickerState(
                initialSelectedStartDateMillis = dateFilter.value?.first,
                initialSelectedEndDateMillis = dateFilter.value?.second
            )

        val isFilterSet by remember {
            derivedStateOf {
                selectedCategories.isNotEmpty() || dateRangePickerState.selectedEndDateMillis != null
            }
        }

        val isFilterActive by remember {
            derivedStateOf {
                dateFilter.value != null || categoryFilter.value != null
            }
        }

        ModalBottomSheet(
            onDismissRequest = { scope.launch { bottomSheetState.hide() } },
            sheetState = bottomSheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = paddingLarge, end = paddingLarge)
                    .navigationBarsPadding()
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall,
                    text = stringResource(
                        R.string.filter_options
                    )
                )
                OutlinedDateRangePicker(
                    state = dateRangePickerState,
                    label = stringResource(id = R.string.date),
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Row(
                    modifier = Modifier
                        .padding(bottom = paddingLarge)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AssistChip(
                        onClick = {
                            val today = LocalDate.now()
                            val startOfMonth = today.withDayOfMonth(1)
                            val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())

                            dateRangePickerState.setSelection(
                                startOfMonth.toEpochMilli(),
                                endOfMonth.toEpochMilli()
                            )
                        },
                        label = { Text(stringResource(id = R.string.this_month)) },
                    )
                    AssistChip(
                        onClick = {
                            val previousMonth = LocalDate.now().minusMonths(1)
                            val startOfMonth = previousMonth.withDayOfMonth(1)
                            val endOfMonth =
                                previousMonth.withDayOfMonth(previousMonth.lengthOfMonth())

                            dateRangePickerState.setSelection(
                                startOfMonth.toEpochMilli(),
                                endOfMonth.toEpochMilli()
                            )
                        },
                        label = { Text(stringResource(id = R.string.previous_month)) },
                    )
                    AssistChip(
                        onClick = {
                            val today = LocalDate.now()
                            val startOfYear = today.withDayOfMonth(1).withMonth(1)
                            val endOfYear = today.withMonth(12).withDayOfMonth(31)

                            dateRangePickerState.setSelection(
                                startOfYear.toEpochMilli(),
                                endOfYear.toEpochMilli()
                            )
                        },
                        label = { Text(stringResource(id = R.string.this_year)) },
                    )
                }

                Text(stringResource(id = R.string.categories))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = paddingLarge),
                    horizontalArrangement = Arrangement.spacedBy(paddingLarge),
                ) {
                    selectedCategories.forEach {
                        ElevatedFilterChip(
                            selected = true,
                            onClick = {
                                selectedCategories.remove(it)
                            },
                            label = { Text(it.category) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                            }
                        )
                    }
                }

                FlowRow(
                    Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = paddingLarge),
                    horizontalArrangement = Arrangement.spacedBy(paddingLarge),
                ) {
                    categories.sortedByDescending { it.count }.forEach {
                        if (!selectedCategories.contains(it)) {
                            ElevatedFilterChip(
                                    selected = false,
                                    onClick = {
                                        selectedCategories.add(it)
                                    },
                                    label = { Text(it.category) },
                                    leadingIcon = null
                                )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    OutlinedButton(
                        enabled = isFilterActive,
                        onClick = {
                            expensesFilterViewModel.clearFilters()
                            scope.launch {
                                bottomSheetState.hide()
                            }
                        },
                    ) {
                        Icon(
                            Icons.Default.FilterAltOff,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(id = R.string.clear))
                    }

                    Button(
                        enabled = isFilterSet,
                        onClick = {
                            expensesFilterViewModel.setDateFilter(
                                Pair(
                                    dateRangePickerState.selectedStartDateMillis,
                                    dateRangePickerState.selectedEndDateMillis
                                )
                            )
                            expensesFilterViewModel.setCategoryFilter(selectedCategories)
                            scope.launch { bottomSheetState.hide() }
                        },
                    ) {
                        Icon(
                            Icons.Filled.FilterAlt,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(id = R.string.set))
                    }
                }
            }
        }
    }
}