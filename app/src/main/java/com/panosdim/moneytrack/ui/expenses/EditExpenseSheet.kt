package com.panosdim.moneytrack.ui.expenses

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panosdim.moneytrack.App
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.models.Category
import com.panosdim.moneytrack.models.Expense
import com.panosdim.moneytrack.models.FieldState
import com.panosdim.moneytrack.models.Response
import com.panosdim.moneytrack.paddingLarge
import com.panosdim.moneytrack.utils.toEpochMilli
import com.panosdim.moneytrack.utils.toLocalDate
import com.panosdim.moneytrack.viewmodels.ExpensesViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseSheet(
    expense: Expense,
    categories: List<Category>,
    bottomSheetState: SheetState,
) {
    if (bottomSheetState.isVisible) {
        val context = LocalContext.current
        val viewModel: ExpensesViewModel = viewModel()
        val scope = rememberCoroutineScope()
        val openDeleteDialog = remember { mutableStateOf(false) }

        val expenseComment = remember { FieldState(expense.comment) }
        val expenseAmount = remember { FieldState(expense.amount.toString()) }
        val expenseDate by remember { mutableStateOf(expense.date.toLocalDate()) }
        val datePickerState =
            rememberDatePickerState(initialSelectedDateMillis = expenseDate.toEpochMilli())
        val expenseCategory = remember { FieldState(categories.find { it.id == expense.category }) }

        var isLoading by remember {
            mutableStateOf(false)
        }

        if (openDeleteDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    openDeleteDialog.value = false
                },
                title = {
                    Text(text = stringResource(id = R.string.delete_expense_dialog_title))
                },
                text = {
                    Text(
                        stringResource(id = R.string.delete_expense_dialog_description)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            openDeleteDialog.value = false

                            scope.launch {
                                viewModel.removeExpense(expense).collect {
                                    when (it) {
                                        is Response.Success -> {
                                            isLoading = false

                                            Toast.makeText(
                                                context, R.string.delete_expense_toast,
                                                Toast.LENGTH_LONG
                                            ).show()

                                            scope.launch {
                                                bottomSheetState.hide()
                                            }
                                        }

                                        is Response.Error -> {
                                            Toast.makeText(
                                                context,
                                                it.errorMessage,
                                                Toast.LENGTH_SHORT
                                            )
                                                .show()

                                            isLoading = false
                                        }

                                        is Response.Loading -> {
                                            isLoading = true
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        Text(stringResource(id = R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            openDeleteDialog.value = false
                        }
                    ) {
                        Text(stringResource(id = R.string.dismiss))
                    }
                }
            )
        }

        fun validateAmount() {
            if (expenseAmount.value != expense.amount.toString()) {
                expenseAmount.removeError()
                if (expenseAmount.value.isEmpty()) {
                    expenseAmount.setError(App.instance.getString(R.string.amount_error_empty))
                }
            }
        }

        fun validateCategory() {
            expenseCategory.removeError()
            // Check if category is selected
            if (expenseCategory.value == null) {
                expenseCategory.setError(App.instance.getString(R.string.category_error_not_selected))
            }
        }

        fun isFormValid(): Boolean {
            if (expenseAmount.hasError || expenseCategory.hasError) {
                return false
            } else {
                // Check if we change something in the object
                datePickerState.selectedDateMillis?.toLocalDate()
                    ?.let { localDate ->
                        expenseCategory.value?.id?.let { categoryId ->
                            if (expense.date == localDate.toString() &&
                                expense.amount == expenseAmount.value.toFloat() &&
                                expense.category == categoryId &&
                                expense.comment == expenseComment.value
                            ) {
                                return false
                            }
                        }
                    }
            }
            return true
        }

        validateAmount()
        validateCategory()

        ModalBottomSheet(
            onDismissRequest = { scope.launch { bottomSheetState.hide() } },
            sheetState = bottomSheetState,
        ) {
            Column(
                modifier = Modifier
                    .padding(paddingLarge)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(id = R.string.edit_expense),
                    style = MaterialTheme.typography.headlineMedium
                )

                ExpenseForm(
                    categories,
                    datePickerState,
                    expenseAmount,
                    expenseComment,
                    expenseCategory,
                    { validateAmount() },
                    { validateCategory() }
                )

                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = paddingLarge)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { openDeleteDialog.value = true },
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(id = R.string.delete))
                    }

                    Button(
                        enabled = isFormValid() && !isLoading,
                        onClick = {
                            datePickerState.selectedDateMillis?.toLocalDate()
                                ?.let { localDate ->
                                    expenseCategory.value?.id?.let { categoryId ->
                                        val updatedExpense = expense.copy(
                                            date = localDate.toString(),
                                            amount = expenseAmount.value.toFloat(),
                                            category = categoryId,
                                            comment = expenseComment.value
                                        )

                                        scope.launch {
                                            viewModel.updateExpense(updatedExpense).collect {
                                                when (it) {
                                                    is Response.Success -> {
                                                        isLoading = false

                                                        Toast.makeText(
                                                            context, R.string.expense_updated,
                                                            Toast.LENGTH_LONG
                                                        ).show()

                                                        scope.launch {
                                                            bottomSheetState.hide()
                                                        }
                                                    }

                                                    is Response.Error -> {
                                                        Toast.makeText(
                                                            context,
                                                            it.errorMessage,
                                                            Toast.LENGTH_SHORT
                                                        )
                                                            .show()

                                                        isLoading = false
                                                    }

                                                    is Response.Loading -> {
                                                        isLoading = true
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                        },
                    ) {
                        Icon(
                            Icons.Filled.Save,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(id = R.string.update))
                    }
                }
            }
        }
    }
}
