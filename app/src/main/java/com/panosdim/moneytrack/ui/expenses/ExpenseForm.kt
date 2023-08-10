package com.panosdim.moneytrack.ui.expenses

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.models.Category
import com.panosdim.moneytrack.models.FieldState
import com.panosdim.moneytrack.paddingLarge
import com.panosdim.moneytrack.ui.OutlinedDatePicker
import com.panosdim.moneytrack.utils.currencyRegex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseForm(
    categories: List<Category>,
    datePickerState: DatePickerState,
    expenseAmount: FieldState<String>,
    expenseComment: FieldState<String>,
    expenseCategory: FieldState<Category?>,
    validateAmount: () -> Unit,
    validateCategory: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    OutlinedDatePicker(
        state = datePickerState,
        label = stringResource(id = R.string.date),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = paddingLarge)
    )

    OutlinedTextField(
        value = expenseAmount.value,
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
        isError = expenseAmount.hasError,
        supportingText = {
            if (expenseAmount.hasError) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = expenseAmount.errorMessage,
                    textAlign = TextAlign.End,
                )
            }
        },
        onValueChange = { newValue ->
            if (newValue.matches(Regex(currencyRegex))) {
                expenseAmount.value = newValue
                validateAmount()
            }
        },
        label = { Text(stringResource(id = R.string.amount)) },
        modifier = Modifier
            .padding(bottom = paddingLarge)
            .fillMaxWidth()
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            readOnly = true,
            value = expenseCategory.value?.category ?: "",
            onValueChange = { validateCategory() },
            label = { Text(stringResource(id = R.string.category)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = expenseCategory.hasError,
            supportingText = {
                if (expenseCategory.hasError) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = expenseCategory.errorMessage,
                        textAlign = TextAlign.End,
                    )
                }
            }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            categories.sortedByDescending { it.count }.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption.category) },
                    onClick = {
                        expenseCategory.value = selectionOption
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }

    Row(
        modifier = Modifier
            .padding(bottom = paddingLarge)
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(paddingLarge)
    ) {
        categories.sortedByDescending { it.count }.take(6).forEach {
            AssistChip(
                onClick = { expenseCategory.value = it },
                label = { Text(it.category) },
            )
        }
    }

    OutlinedTextField(
        value = expenseComment.value,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            capitalization =
            KeyboardCapitalization.Words,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        onValueChange = { expenseComment.value = it },
        label = { Text(stringResource(id = R.string.comment)) },
        modifier = Modifier
            .padding(bottom = paddingLarge)
            .fillMaxWidth()
    )
}