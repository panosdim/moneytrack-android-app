package com.panosdim.moneytrack.viewmodels

import androidx.lifecycle.ViewModel
import com.panosdim.moneytrack.utils.ExpenseSortField
import com.panosdim.moneytrack.utils.SortDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class ExpensesSortViewModel : ViewModel() {
    private val _sortField = MutableStateFlow(ExpenseSortField.DATE)
    val expenseSortField: Flow<ExpenseSortField> = _sortField
    private val _sortDirection = MutableStateFlow(SortDirection.DESC)
    var sortDirection: Flow<SortDirection> = _sortDirection

    fun setSortField(sortField: String) {
        ExpenseSortField.values().find { it.title == sortField }?.let {
            _sortField.value = it
        }
    }

    fun setSortDirection(sortDirection: String) {
        SortDirection.values().find { it.title == sortDirection }?.let {
            _sortDirection.value = it
        }
    }
}