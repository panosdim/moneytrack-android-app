package com.panosdim.moneytrack.ui.expenses

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.window.Dialog
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
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(categories: List<Category>, open: Boolean, onClose: () -> Unit) {
    if (open) {
        val context = LocalContext.current
        val viewModel: ExpensesViewModel = viewModel()
        val scope = rememberCoroutineScope()

        val expenseComment = remember { FieldState("") }
        val expenseAmount = remember { FieldState("") }
        val expenseDate by remember { mutableStateOf(LocalDate.now()) }
        val datePickerState =
            rememberDatePickerState(initialSelectedDateMillis = expenseDate.toEpochMilli())
        val expenseCategory = remember { FieldState<Category?>(null) }

        var isLoading by remember {
            mutableStateOf(false)
        }

        fun validateAmount() {
            expenseAmount.removeError()
            if (expenseAmount.value.isEmpty()) {
                expenseAmount.setError(App.instance.getString(R.string.amount_error_empty))
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
            return !(expenseAmount.hasError || expenseCategory.hasError)
        }

        validateAmount()
        validateCategory()

        Dialog(
            onDismissRequest = {
                onClose()
            }
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(
                    modifier = Modifier
                        .padding(paddingLarge),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(
                            id = R.string.add_expense
                        ),
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
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Button(
                            enabled = isFormValid() && !isLoading,
                            onClick = {
                                datePickerState.selectedDateMillis?.toLocalDate()
                                    ?.let { localDate ->
                                        expenseCategory.value?.id?.let { categoryId ->
                                            val newExpense =
                                                Expense(
                                                    null,
                                                    date = localDate.toString(),
                                                    amount = expenseAmount.value.toFloat(),
                                                    category = categoryId,
                                                    comment = expenseComment.value
                                                )

                                            scope.launch {
                                                viewModel.addExpense(newExpense).collect {
                                                    when (it) {
                                                        is Response.Success -> {
                                                            isLoading = false

                                                            Toast.makeText(
                                                                context, R.string.expense_added,
                                                                Toast.LENGTH_LONG
                                                            ).show()

                                                            onClose()
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
                                Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(stringResource(id = R.string.create))
                        }
                    }
                }
            }
        }
    }
}
