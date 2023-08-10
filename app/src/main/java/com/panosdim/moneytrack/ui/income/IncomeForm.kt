package com.panosdim.moneytrack.ui.income

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.models.FieldState
import com.panosdim.moneytrack.paddingLarge
import com.panosdim.moneytrack.ui.OutlinedDatePicker
import com.panosdim.moneytrack.utils.currencyRegex
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeForm(
    datePickerState: DatePickerState,
    incomeAmount: FieldState<String>,
    incomeComment: FieldState<String>,
    validateAmount: () -> Unit
) {
    OutlinedDatePicker(
        state = datePickerState,
        label = stringResource(id = R.string.date),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = paddingLarge),
        dateFormatter = DateTimeFormatter.ofPattern("MMM yyyy")
    )

    OutlinedTextField(
        value = incomeAmount.value,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.EuroSymbol,
                contentDescription = "Euro Icon"
            )
        },
        isError = incomeAmount.hasError,
        supportingText = {
            if (incomeAmount.hasError) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = incomeAmount.errorMessage,
                    textAlign = TextAlign.End,
                )
            }
        },
        onValueChange = { newValue ->
            if (newValue.matches(Regex(currencyRegex))) {
                incomeAmount.value = newValue
                validateAmount()
            }
        },
        label = { Text(stringResource(id = R.string.amount)) },
        modifier = Modifier
            .padding(bottom = paddingLarge)
            .fillMaxWidth()
    )

    OutlinedTextField(
        value = incomeComment.value,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            capitalization =
            KeyboardCapitalization.Words,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        onValueChange = { incomeComment.value = it },
        label = { Text(stringResource(id = R.string.comment)) },
        modifier = Modifier
            .padding(bottom = paddingLarge)
            .fillMaxWidth()
    )
}