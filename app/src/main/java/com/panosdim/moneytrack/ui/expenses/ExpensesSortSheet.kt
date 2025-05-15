package com.panosdim.moneytrack.ui.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.paddingLarge
import com.panosdim.moneytrack.utils.ExpenseSortField
import com.panosdim.moneytrack.utils.SortDirection
import com.panosdim.moneytrack.viewmodels.ExpensesSortViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesSortSheet(
    bottomSheetState: SheetState
) {
    val scope = rememberCoroutineScope()
    val expensesSortViewModel: ExpensesSortViewModel = viewModel()

    val optionsField = enumValues<ExpenseSortField>().map { it.title }
    val optionsDirection = enumValues<SortDirection>().map { it.title }

    var expandedField by remember { mutableStateOf(false) }
    var expandedDirection by remember { mutableStateOf(false) }

    val selectedFieldText =
        expensesSortViewModel.expenseSortField.collectAsStateWithLifecycle(initialValue = ExpenseSortField.DATE)
    val selectedDirectionText =
        expensesSortViewModel.sortDirection.collectAsStateWithLifecycle(initialValue = SortDirection.DESC)

    // Sheet content
    if (bottomSheetState.isVisible) {
        ModalBottomSheet(
            onDismissRequest = { scope.launch { bottomSheetState.hide() } },
            sheetState = bottomSheetState,
        ) {
            Column {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall,
                    text = stringResource(
                        R.string.sort_options
                    )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = paddingLarge, end = paddingLarge)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expandedField,
                        onExpandedChange = { expandedField = !expandedField },
                    ) {
                        ElevatedFilterChip(
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryEditable),
                            selected = false,
                            onClick = { },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Sort, "Sort Field") },
                            label = { Text(selectedFieldText.value.title) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedField) },
                        )
                        ExposedDropdownMenu(
                            expanded = expandedField,
                            onDismissRequest = { expandedField = false },
                        ) {
                            optionsField.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        expensesSortViewModel.setSortField(selectionOption)
                                        expandedField = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                )
                            }
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = expandedDirection,
                        onExpandedChange = { expandedDirection = !expandedDirection },
                    ) {
                        ElevatedFilterChip(
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryEditable),
                            selected = false,
                            onClick = { },
                            label = { Text(selectedDirectionText.value.title) },
                            leadingIcon = { Icon(Icons.Default.SwapVert, "Sort Direction") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDirection) },
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDirection,
                            onDismissRequest = { expandedDirection = false },
                        ) {
                            optionsDirection.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        expensesSortViewModel.setSortDirection(selectionOption)
                                        expandedDirection = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}