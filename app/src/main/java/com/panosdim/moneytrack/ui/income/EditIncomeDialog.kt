package com.panosdim.moneytrack.ui.income


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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.panosdim.moneytrack.App
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.models.FieldState
import com.panosdim.moneytrack.models.Income
import com.panosdim.moneytrack.models.Response
import com.panosdim.moneytrack.paddingLarge
import com.panosdim.moneytrack.utils.toEpochMilli
import com.panosdim.moneytrack.utils.toLocalDate
import com.panosdim.moneytrack.viewmodels.IncomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditIncomeDialog(
    income: Income,
    open: Boolean,
    onClose: () -> Unit
) {
    if (open) {
        val context = LocalContext.current
        val viewModel: IncomeViewModel = viewModel()
        val scope = rememberCoroutineScope()
        val openDeleteDialog = remember { mutableStateOf(false) }

        val incomeComment = remember { FieldState(income.comment) }
        val incomeAmount = remember { FieldState(income.amount.toString()) }
        val incomeDate by remember { mutableStateOf(income.date.toLocalDate()) }
        val datePickerState =
            rememberDatePickerState(initialSelectedDateMillis = incomeDate.toEpochMilli())

        var isLoading by remember {
            mutableStateOf(false)
        }

        if (openDeleteDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    openDeleteDialog.value = false
                },
                title = {
                    Text(text = stringResource(id = R.string.delete_income_dialog_title))
                },
                text = {
                    Text(
                        stringResource(id = R.string.delete_income_dialog_description)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            openDeleteDialog.value = false

                            scope.launch {
                                viewModel.removeIncome(income).collect {
                                    when (it) {
                                        is Response.Success -> {
                                            isLoading = false

                                            Toast.makeText(
                                                context, R.string.delete_income_toast,
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
            if (incomeAmount.value != income.amount.toString()) {
                incomeAmount.removeError()
                if (incomeAmount.value.isEmpty()) {
                    incomeAmount.setError(App.instance.getString(R.string.amount_error_empty))
                }
            }
        }

        fun isFormValid(): Boolean {
            // Check if we change something in the object
            datePickerState.selectedDateMillis?.toLocalDate()
                ?.let { localDate ->
                    if (income.date == localDate.toString() &&
                        income.amount == incomeAmount.value.toFloat() &&
                        income.comment == incomeComment.value
                    ) {
                        return false
                    }
                }

            return !(incomeAmount.hasError)
        }

        validateAmount()

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
                            id = R.string.edit_income
                        ),
                        style = MaterialTheme.typography.headlineMedium
                    )

                    IncomeForm(datePickerState, incomeAmount, incomeComment) {
                        validateAmount()
                    }

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
                        modifier = Modifier
                            .fillMaxWidth()
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
                                        // Update income object
                                        income.date = localDate.toString()
                                        income.amount = incomeAmount.value.toFloat()
                                        income.comment = incomeComment.value

                                        scope.launch {
                                            viewModel.updateIncome(income).collect {
                                                when (it) {
                                                    is Response.Success -> {
                                                        isLoading = false

                                                        Toast.makeText(
                                                            context, R.string.income_updated,
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
}
