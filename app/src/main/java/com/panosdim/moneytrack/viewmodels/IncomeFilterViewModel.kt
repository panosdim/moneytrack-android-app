package com.panosdim.moneytrack.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class IncomeFilterViewModel : ViewModel() {
    private val _filterDate = MutableStateFlow<Pair<Long, Long>?>(null)
    val filterDate: MutableStateFlow<Pair<Long, Long>?> = _filterDate

    fun setDateFilter(dateFilter: Pair<Long?, Long?>?) {
        if (dateFilter != null) {
            dateFilter.first?.let { first ->
                dateFilter.second?.let { second ->
                    _filterDate.value = Pair(first, second)
                }
            } ?: kotlin.run {
                _filterDate.value = null
            }
        } else {
            _filterDate.value = null
        }
    }
    
    fun clearFilters() {
        setDateFilter(null)
    }
}