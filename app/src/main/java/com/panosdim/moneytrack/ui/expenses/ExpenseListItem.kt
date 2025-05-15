package com.panosdim.moneytrack.ui.expenses

import androidx.compose.foundation.clickable
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.panosdim.moneytrack.models.Category
import com.panosdim.moneytrack.models.Expense
import com.panosdim.moneytrack.ui.theme.CategoryColor
import com.panosdim.moneytrack.ui.theme.ExpenseColor
import com.panosdim.moneytrack.utils.extractEmojis
import com.panosdim.moneytrack.utils.formatDate
import com.panosdim.moneytrack.utils.getCategoryName
import com.panosdim.moneytrack.utils.moneyFormat
import com.panosdim.moneytrack.utils.removeEmojis
import java.time.format.DateTimeFormatter

@Composable
fun ExpenseListItem(
    expenseItem: Expense,
    categories: List<Category>,
    selectedExpense: (expense: Expense) -> Unit
) {
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val categoryName = getCategoryName(
        expenseItem.category,
        categories
    )

    ListItem(
        overlineContent = {
            Text(
                removeEmojis(categoryName),
                color = CategoryColor
            )
        },
        headlineContent = {
            Text(
                expenseItem.date.formatDate(
                    dateFormatter,
                    addTodayAndYesterdayInfo = false
                ),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        leadingContent = {
            Text(
                extractEmojis(categoryName),
                style = MaterialTheme.typography.headlineLarge
            )
        },
        supportingContent = {
            if (expenseItem.comment.isNotBlank()) {
                Text(expenseItem.comment)
            }
        },
        trailingContent = {
            Text(
                text = moneyFormat(expenseItem.amount),
                style = MaterialTheme.typography.headlineSmall,
                color = ExpenseColor
            )
        },
        modifier = Modifier.clickable {
            selectedExpense(expenseItem)
        }
    )
}