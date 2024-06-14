package com.panosdim.moneytrack.utils

import com.panosdim.moneytrack.models.Category
import com.panosdim.moneytrack.models.Expense
import com.panosdim.moneytrack.models.Income
import java.time.LocalDate

fun filter(
    expenseList: List<Expense>,
    filterDate: Pair<Long, Long>?,
    filterCategory: List<Category>?,
    filterComment: String?,
): List<Expense> {
    val data = expenseList.toMutableList()

    // Date Filter
    filterDate?.let { (first, second) ->
        data.retainAll {
            val date = LocalDate.parse(it.date)

            (date.isAfter(first.toLocalDate()) || date.isEqual(first.toLocalDate()))
                    && (date.isBefore(second.toLocalDate()) || date.isEqual(
                second.toLocalDate()
            ))
        }
    }

    // Category Filter
    filterCategory?.let { categories: List<Category> ->
        if (categories.isNotEmpty()) {
            data.retainAll { expense ->
                categories.find { it.id == expense.category } != null
            }
        }
    }

    // Comment Filter
    filterComment?.let { comment: String ->
        if (comment.isNotEmpty()) {
            data.retainAll { expense ->
                expense.comment.unaccent().contains(
                    comment.unaccent().trim(),
                    ignoreCase = true
                )
            }
        }
    }

    return data
}

fun filter(
    incomeList: List<Income>,
    filterDate: Pair<Long, Long>?,
    filterComment: String?,
): List<Income> {
    val data = incomeList.toMutableList()

    // Date Filter
    filterDate?.let { (first, second) ->
        data.retainAll {
            val date = LocalDate.parse(it.date)

            (date.isAfter(first.toLocalDate()) || date.isEqual(first.toLocalDate()))
                    && (date.isBefore(second.toLocalDate()) || date.isEqual(
                second.toLocalDate()
            ))
        }
    }

    // Comment Filter
    filterComment?.let { comment: String ->
        if (comment.isNotEmpty()) {
            data.retainAll { income ->
                income.comment.unaccent().contains(
                    comment.unaccent().trim(),
                    ignoreCase = true
                )
            }
        }
    }

    return data
}