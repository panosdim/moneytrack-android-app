package com.panosdim.moneytrack.utils

import com.panosdim.moneytrack.models.Category
import com.panosdim.moneytrack.models.Expense
import com.panosdim.moneytrack.models.Income

enum class ExpenseSortField(val title: String) {
    DATE("Date"), AMOUNT("Amount"), CATEGORY("Category"), COMMENT("Comment")
}

enum class IncomeSortField(val title: String) {
    DATE("Date"), SALARY("Salary"), COMMENT("Comment")
}

enum class SortDirection(val title: String) {
    ASC("Ascending"), DESC("Descending")
}

fun sort(
    expenseList: List<Expense>,
    categories: List<Category>,
    expenseSortField: ExpenseSortField,
    sortDirection: SortDirection
): List<Expense> {
    val data = expenseList.toMutableList()
    when (expenseSortField) {
        ExpenseSortField.DATE -> when (sortDirection) {
            SortDirection.ASC -> data.sortBy { it.date }
            SortDirection.DESC -> data.sortByDescending { it.date }
        }

        ExpenseSortField.AMOUNT -> when (sortDirection) {
            SortDirection.ASC -> data.sortBy { it.amount.toDouble() }
            SortDirection.DESC -> data.sortByDescending { it.amount.toDouble() }
        }

        ExpenseSortField.CATEGORY -> when (sortDirection) {
            SortDirection.ASC -> data.sortBy { getCategoryName(it.category, categories) }
            SortDirection.DESC -> data.sortByDescending {
                getCategoryName(
                    it.category,
                    categories
                )
            }
        }

        ExpenseSortField.COMMENT -> when (sortDirection) {
            SortDirection.ASC -> data.sortBy { it.comment }
            SortDirection.DESC -> data.sortByDescending { it.comment }
        }
    }

    return data
}

fun sort(
    incomeList: List<Income>,
    incomeSortField: IncomeSortField,
    sortDirection: SortDirection
): List<Income> {
    val data = incomeList.toMutableList()
    when (incomeSortField) {
        IncomeSortField.DATE -> when (sortDirection) {
            SortDirection.ASC -> data.sortBy { it.date }
            SortDirection.DESC -> data.sortByDescending { it.date }
        }

        IncomeSortField.SALARY -> when (sortDirection) {
            SortDirection.ASC -> data.sortBy { it.amount.toDouble() }
            SortDirection.DESC -> data.sortByDescending { it.amount.toDouble() }
        }

        IncomeSortField.COMMENT -> when (sortDirection) {
            SortDirection.ASC -> data.sortBy { it.comment }
            SortDirection.DESC -> data.sortByDescending { it.comment }
        }
    }

    return data
}