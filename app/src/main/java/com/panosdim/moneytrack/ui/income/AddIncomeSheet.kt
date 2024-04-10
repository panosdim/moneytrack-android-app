package com.panosdim.moneytrack.ui.income

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
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
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeSheet(bottomSheetState: SheetState) {
    if (bottomSheetState.isVisible) {
        val context = LocalContext.current
        val viewModel: IncomeViewModel = viewModel()
        val scope = rememberCoroutineScope()

        val incomeComment = remember { FieldState("") }
        val incomeAmount = remember { FieldState("") }
        val incomeDate by remember { mutableStateOf(LocalDate.now()) }
        val datePickerState =
            rememberDatePickerState(initialSelectedDateMillis = incomeDate.toEpochMilli())

        var isLoading by remember {
            mutableStateOf(false)
        }

        fun validateAmount() {
            incomeAmount.removeError()
            if (incomeAmount.value.isEmpty()) {
                incomeAmount.setError(App.instance.getString(R.string.amount_error_empty))
            }
        }

        fun isFormValid(): Boolean {
            return !(incomeAmount.hasError)
        }

        validateAmount()

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
                    stringResource(id = R.string.add_income),
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
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Button(
                        enabled = isFormValid() && !isLoading,
                        onClick = {
                            datePickerState.selectedDateMillis?.toLocalDate()
                                ?.let { localDate ->
                                    val newIncome =
                                        Income(
                                            null,
                                            date = localDate.toString(),
                                            amount = incomeAmount.value.toFloat(),
                                            comment = incomeComment.value
                                        )

                                    scope.launch {
                                        viewModel.addIncome(newIncome).collect {
                                            when (it) {
                                                is Response.Success -> {
                                                    isLoading = false

                                                    Toast.makeText(
                                                        context, R.string.income_added,
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
