package com.panosdim.moneytrack.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class FieldState<T>(
    fieldValue: T
) {
    var value by mutableStateOf(fieldValue)

    var hasError: Boolean by mutableStateOf(false)
        private set

    var errorMessage: String by mutableStateOf("")
        private set

    fun setError(message: String) {
        hasError = true
        errorMessage = message
    }

    fun removeError() {
        hasError = false
        errorMessage = ""
    }
}