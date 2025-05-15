package com.panosdim.moneytrack.viewmodels

import androidx.lifecycle.ViewModel
import com.panosdim.moneytrack.models.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class ExpensesFilterViewModel : ViewModel() {
    private val _filterDate = MutableStateFlow<Pair<Long, Long>?>(null)
    val filterDate: MutableStateFlow<Pair<Long, Long>?> = _filterDate
    private val _filterCategory = MutableStateFlow<List<Category>?>(null)
    var filterCategory: Flow<List<Category>?> = _filterCategory
    private val _filterComment = MutableStateFlow<String?>(null)
    var filterComment: Flow<String?> = _filterComment

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

    fun setCategoryFilter(categoryFilter: List<Category>?) {
        _filterCategory.value = categoryFilter
    }

    fun setCommentFilter(commentFilter: String?) {
        _filterComment.value = commentFilter
    }

    fun clearFilters() {
        setDateFilter(null)
        setCategoryFilter(null)
    }
}