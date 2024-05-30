package com.panosdim.moneytrack.ui.expenses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
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
import com.panosdim.moneytrack.utils.toEpochMilli
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val scope = rememberCoroutineScope()
    val skipPartiallyExpanded by remember { mutableStateOf(true) }
    val categoriesSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    )
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        if (expenseCategory.value == null) {
            focusRequester.requestFocus()
        }
    }

    OutlinedDatePicker(
        state = datePickerState,
        label = stringResource(id = R.string.date),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = paddingLarge)
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
                datePickerState.selectedDateMillis = today.toEpochMilli()
            },
            label = { Text(stringResource(id = R.string.today)) },
        )
        AssistChip(
            onClick = {
                val yesterday = LocalDate.now().minusDays(1)
                datePickerState.selectedDateMillis = yesterday.toEpochMilli()
            },
            label = { Text(stringResource(id = R.string.yesterday)) },
        )
    }

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
            .focusRequester(focusRequester)
    )

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                if (it.isFocused) {
                    scope.launch { categoriesSheetState.show() }
                    focusManager.clearFocus()
                }
            },
        readOnly = true,
        value = expenseCategory.value?.category ?: "",
        onValueChange = { validateCategory() },
        label = { Text(stringResource(id = R.string.category)) },
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
    if (categoriesSheetState.isVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch {
                    categoriesSheetState.hide()
                    focusManager.clearFocus()
                }
            },
            sheetState = categoriesSheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = paddingLarge, end = paddingLarge)
                    .navigationBarsPadding()
            ) {
                FlowRow(
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = paddingLarge),
                    horizontalArrangement = Arrangement.spacedBy(paddingLarge),
                ) {
                    categories.sortedByDescending { it.count }.forEach {
                        AssistChip(
                            onClick = {
                                expenseCategory.value = it
                                scope.launch { categoriesSheetState.hide() }
                            },
                            label = { Text(it.category) },
                        )
                    }
                }
            }
        }
    }

    OutlinedTextField(
        value = expenseComment.value,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Words,
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