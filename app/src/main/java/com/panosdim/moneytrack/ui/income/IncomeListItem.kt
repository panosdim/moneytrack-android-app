package com.panosdim.moneytrack.ui.income

import androidx.compose.foundation.clickable
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.panosdim.moneytrack.models.Income
import com.panosdim.moneytrack.ui.theme.IncomeColor
import com.panosdim.moneytrack.utils.formatDate
import com.panosdim.moneytrack.utils.moneyFormat
import java.time.format.DateTimeFormatter

@Composable
fun IncomeListItem(
    incomeItem: Income,
    selectedIncome: (income: Income) -> Unit
) {
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM yyyy")

    ListItem(
        headlineContent = {
            Text(
                incomeItem.date.formatDate(
                    dateFormatter,
                    addTodayAndYesterdayInfo = false
                ),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        supportingContent = {
            if (incomeItem.comment.isNotBlank()) {
                Text(incomeItem.comment)
            }
        },
        trailingContent = {
            Text(
                text = moneyFormat(incomeItem.amount),
                style = MaterialTheme.typography.headlineSmall,
                color = IncomeColor
            )
        },
        modifier = Modifier.clickable {
            selectedIncome(incomeItem)
        }
    )
    Divider()
}