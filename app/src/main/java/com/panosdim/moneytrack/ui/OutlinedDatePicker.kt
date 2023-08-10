package com.panosdim.moneytrack.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.utils.toLocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedDatePicker(
    state: DatePickerState,
    label: String,
    modifier: Modifier,
    dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE dd-MM-yyyy")
) {
    val openDatePickerDialog = remember { mutableStateOf(false) }
    val source = remember { MutableInteractionSource() }
    if (source.collectIsPressedAsState().value) {
        openDatePickerDialog.value = true
    }
    val confirmEnabled = remember {
        derivedStateOf { state.selectedDateMillis != null }
    }

    if (openDatePickerDialog.value) {
        DatePickerDialog(
            onDismissRequest = {
                openDatePickerDialog.value = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDatePickerDialog.value = false
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openDatePickerDialog.value = false
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = state, showModeToggle = false)
        }
    }

    OutlinedTextField(
        modifier = modifier,
        label = { Text(text = label) },
        readOnly = true,
        interactionSource = source,
        value = state.selectedDateMillis?.toLocalDate()?.format(dateFormatter) ?: "",
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Calendar Icon"
            )
        },
        onValueChange = { },
    )
}