package com.panosdim.moneytrack.viewmodel

import androidx.lifecycle.*
import com.panosdim.moneytrack.api.CategoriesRepository
import com.panosdim.moneytrack.api.ExpensesRepository
import com.panosdim.moneytrack.api.data.Resource
import com.panosdim.moneytrack.model.Category
import com.panosdim.moneytrack.model.Expense
import com.panosdim.moneytrack.utils.getCategoryName
import com.panosdim.moneytrack.utils.unaccent
import java.time.LocalDate

class ExpensesViewModel : ViewModel() {

    enum class SortField {
        DATE, AMOUNT, CATEGORY, COMMENT
    }

    enum class SortDirection {
        ASC, DESC
    }

    private val expensesRepository = ExpensesRepository()
    private val categoriesRepository = CategoriesRepository()
    var sortField: SortField = SortField.DATE
    var sortDirection: SortDirection = SortDirection.DESC

    var filterDate: Pair<LocalDate, LocalDate>? = null
    var filterAmount: List<Float>? = null
    var filterComment: String? = null
    var filterCategory: List<Int>? = null
    var isFilterSet: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { this.value = false }

    val categories: LiveData<List<Category>> =
        Transformations.switchMap(categoriesRepository.get()) { data ->
            MutableLiveData<List<Category>>().apply {
                this.value = data
            }
        }
    private var _expenses: LiveData<List<Expense>> =
        Transformations.switchMap(expensesRepository.get()) { data ->
            MutableLiveData<List<Expense>>().apply {
                this.value = data
            }
        }
    val expenses = MediatorLiveData<List<Expense>>()

    init {
        expenses.addSource(_expenses) {
            var data = filter(it)
            data = sort(data)
            expenses.value = data
        }
    }

    fun removeExpense(expense: Expense): LiveData<Resource<Expense>> {
        return expensesRepository.delete(expense)
    }

    fun addExpense(expense: Expense): LiveData<Resource<Expense>> {
        return expensesRepository.add(expense)
    }

    fun updateExpense(expense: Expense): LiveData<Resource<Expense>> {
        return expensesRepository.update(expense)
    }

    private fun sort(expenseList: List<Expense>): List<Expense> {
        val data = expenseList.toMutableList()
        when (sortField) {
            SortField.DATE -> when (sortDirection) {
                SortDirection.ASC -> data.sortBy { it.date }
                SortDirection.DESC -> data.sortByDescending { it.date }
            }

            SortField.AMOUNT -> when (sortDirection) {
                SortDirection.ASC -> data.sortBy { it.amount.toDouble() }
                SortDirection.DESC -> data.sortByDescending { it.amount.toDouble() }
            }

            SortField.CATEGORY -> when (sortDirection) {
                SortDirection.ASC -> data.sortBy { getCategoryName(it.category, categories) }
                SortDirection.DESC -> data.sortByDescending {
                    getCategoryName(
                        it.category,
                        categories
                    )
                }
            }

            SortField.COMMENT -> when (sortDirection) {
                SortDirection.ASC -> data.sortBy { it.comment }
                SortDirection.DESC -> data.sortByDescending { it.comment }
            }
        }

        return data
    }

    fun clearFilters() = _expenses.value?.let {
        filterAmount = null
        filterDate = null
        filterComment = null
        filterCategory = null
        expenses.value = sort(it)
        isFilterSet.value = false
    }

    private fun filter(expenseList: List<Expense>): List<Expense> {
        val data = expenseList.map { it.copy() }.toMutableList()
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

        // Category Filter
        filterCategory?.let { categories: List<Int> ->
            data.retainAll {
                categories.contains(it.category)
            }
        }

        isFilterSet.value = isFilterSet()

        return data
    }

    fun refreshExpenses() {
        expenses.removeSource(_expenses)
        _expenses = Transformations.switchMap(expensesRepository.get()) { data ->
            MutableLiveData<List<Expense>>().apply {
                this.value = data
            }
        }
        expenses.addSource(_expenses) {
            var data = filter(it)
            data = sort(data)
            expenses.value = data
        }
    }

    private fun isFilterSet(): Boolean {
        return filterAmount != null || filterCategory != null || filterDate != null
    }
}