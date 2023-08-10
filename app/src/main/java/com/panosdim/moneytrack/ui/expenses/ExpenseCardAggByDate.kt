package com.panosdim.moneytrack.ui.expenses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.models.Category
import com.panosdim.moneytrack.models.Expense
import com.panosdim.moneytrack.paddingLarge
import com.panosdim.moneytrack.paddingSmall
import com.panosdim.moneytrack.ui.theme.CategoryColor
import com.panosdim.moneytrack.ui.theme.ExpenseColor
import com.panosdim.moneytrack.utils.extractEmojis
import com.panosdim.moneytrack.utils.formatDate
import com.panosdim.moneytrack.utils.getCategoryName
import com.panosdim.moneytrack.utils.moneyFormat
import com.panosdim.moneytrack.utils.removeEmojis

@Composable
fun ExpenseCardAggByDate(
    date: String,
    expensesList: List<Expense>,
    categories: List<Category>,
    selectedExpense: (expense: Expense) -> Unit
) {
    val context = LocalContext.current
    val resources = context.resources

    Card(
        modifier = Modifier
            .padding(paddingSmall)
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.padding(paddingLarge)) {
                Text(
                    text = date.formatDate(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(bottom = paddingLarge)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                expensesList.forEach { expenseDetails ->
                    val categoryName = getCategoryName(
                        expenseDetails.category,
                        categories
                    )
                    ListItem(
                        headlineContent = {
                            Text(
                                removeEmojis(categoryName),
                                color = CategoryColor
                            )
                        },
                        leadingContent = {
                            Text(
                                extractEmojis(categoryName),
                                style = MaterialTheme.typography.headlineLarge
                            )
                        },
                        supportingContent = {
                            if (expenseDetails.comment.isNotBlank()) {
                                Text(expenseDetails.comment)
                            }
                        },
                        trailingContent = {
                            Text(
                                text = moneyFormat(expenseDetails.amount),
                                style = MaterialTheme.typography.headlineSmall,
                                color = ExpenseColor
                            )
                        },
                        modifier = Modifier.clickable {
                            selectedExpense(expenseDetails)
                        }
                    )
                    Divider()
                }

                Text(
                    text = resources.getString(
                        R.string.total,
                        moneyFormat(expensesList.fold(0f) { acc, expenseDetails -> acc + expenseDetails.amount })
                    ),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}