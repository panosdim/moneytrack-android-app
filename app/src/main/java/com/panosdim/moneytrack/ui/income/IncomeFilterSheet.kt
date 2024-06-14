package com.panosdim.moneytrack.ui.income

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.paddingLarge
import com.panosdim.moneytrack.ui.OutlinedDateRangePicker
import com.panosdim.moneytrack.utils.toEpochMilli
import com.panosdim.moneytrack.viewmodels.IncomeFilterViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeFilterSheet(
    bottomSheetState: SheetState,
) {
    val scope = rememberCoroutineScope()
    val incomeFilterViewModel: IncomeFilterViewModel = viewModel()

    val dateFilter =
        incomeFilterViewModel.filterDate.collectAsStateWithLifecycle(initialValue = null)

    val commentFilter =
        incomeFilterViewModel.filterComment.collectAsStateWithLifecycle(initialValue = null)

    // Sheet content
    if (bottomSheetState.isVisible) {
        val dateRangePickerState =
            rememberDateRangePickerState(
                initialSelectedStartDateMillis = dateFilter.value?.first,
                initialSelectedEndDateMillis = dateFilter.value?.second
            )

        val searchComment = remember { mutableStateOf(commentFilter.value ?: "") }

        val isFilterSet by remember {
            derivedStateOf {
                dateRangePickerState.selectedEndDateMillis != null || searchComment.value.isNotEmpty()
            }
        }

        val isFilterActive by remember {
            derivedStateOf {
                dateFilter.value != null || commentFilter.value != null
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

                OutlinedTextField(
                    value = searchComment.value,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    onValueChange = { searchComment.value = it },
                    label = { Text(stringResource(id = R.string.comment_search)) },
                    modifier = Modifier
                        .padding(bottom = paddingLarge)
                        .fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    OutlinedButton(
                        enabled = isFilterActive,
                        onClick = {
                            incomeFilterViewModel.clearFilters()
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
                            incomeFilterViewModel.setDateFilter(
                                Pair(
                                    dateRangePickerState.selectedStartDateMillis,
                                    dateRangePickerState.selectedEndDateMillis
                                )
                            )
                            incomeFilterViewModel.setCommentFilter(searchComment.value)
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