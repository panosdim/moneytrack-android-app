package com.panosdim.moneytrack.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import com.panosdim.moneytrack.api.IncomeRepository
import com.panosdim.moneytrack.api.data.Resource
import com.panosdim.moneytrack.model.Income
import com.panosdim.moneytrack.utils.unaccent
import java.time.LocalDate

class IncomeViewModel : ViewModel() {
    enum class SortField {
        DATE, AMOUNT, COMMENT
    }

    enum class SortDirection {
        ASC, DESC
    }

    private val incomeRepository = IncomeRepository()
    var sortField: SortField = SortField.DATE
    var sortDirection: SortDirection = SortDirection.DESC

    var filterDate: Pair<LocalDate, LocalDate>? = null
    var filterAmount: List<Float>? = null
    var filterComment: String? = null

    var isFilterSet: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { this.value = false }

    private var _income: LiveData<List<Income>> =
        incomeRepository.get().switchMap { data ->
            MutableLiveData<List<Income>>().apply {
                this.value = data
            }
        }
    val income = MediatorLiveData<List<Income>>()

    init {
        income.addSource(_income) {
            var data = filter(it)
            data = sort(data)
            income.value = data
        }
    }

    fun removeIncome(income: Income): LiveData<Resource<Income>> {
        return incomeRepository.delete(income)
    }

    fun addIncome(income: Income): LiveData<Resource<Income>> {
        return incomeRepository.add(income)
    }

    fun updateIncome(income: Income): LiveData<Resource<Income>> {
        return incomeRepository.update(income)
    }

    private fun sort(incomeList: List<Income>): List<Income> {
        val data = incomeList.toMutableList()
        when (sortField) {
            SortField.DATE -> when (sortDirection) {
                SortDirection.ASC -> data.sortBy { it.date }
                SortDirection.DESC -> data.sortByDescending { it.date }
            }

            SortField.AMOUNT -> when (sortDirection) {
                SortDirection.ASC -> data.sortBy { it.amount.toDouble() }
                SortDirection.DESC -> data.sortByDescending { it.amount.toDouble() }
            }

            SortField.COMMENT -> when (sortDirection) {
                SortDirection.ASC -> data.sortBy { it.comment }
                SortDirection.DESC -> data.sortByDescending { it.comment }
            }
        }

        return data
    }

    fun clearFilters() = _income.value?.let {
        filterAmount = null
        filterDate = null
        filterComment = null
        income.value = sort(it)
        isFilterSet.value = false
    }

    private fun filter(incomeList: List<Income>): List<Income> {
        val data = incomeList.map { it.copy() }.toMutableList()
        // Date Filter
        filterDate?.let { (first, second) ->
            data.retainAll {
                val date = LocalDate.parse(it.date)
                (date.isAfter(first) || date.isEqual(first)) && (date.isBefore(second) || date.isEqual(
                    second
                ))
            }
        }

        // Amount Filter
        filterAmount?.let { range: List<Float> ->
            data.retainAll {
                it.amount >= range[0] && it.amount <= range[1]
            }
        }

        // Comment Search
        filterComment?.let { filter: String ->
            data.retainAll {
                it.comment.unaccent().contains(filter, ignoreCase = true)
            }
        }

        isFilterSet.value = isFilterSet()

        return data
    }

    fun refreshIncome(fetchAll: Boolean = false) {
        income.removeSource(_income)
        _income = incomeRepository.get(fetchAll).switchMap { data ->
            MutableLiveData<List<Income>>().apply {
                this.value = data
            }
        }
        income.addSource(_income) {
            var data = filter(it)
            data = sort(data)
            income.value = data
        }
    }

    private fun isFilterSet(): Boolean {
        return filterAmount != null || filterDate != null
    }
}