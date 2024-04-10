package com.panosdim.moneytrack.ui.income

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.models.Income
import com.panosdim.moneytrack.paddingLarge
import com.panosdim.moneytrack.paddingSmall
import com.panosdim.moneytrack.ui.theme.IncomeColor
import com.panosdim.moneytrack.utils.moneyFormat

@Composable
fun IncomeCardAggByDate(
    date: String,
    incomesList: List<Income>,
    selectedIncome: (income: Income) -> Unit
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
                    text = date,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(bottom = paddingLarge)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                incomesList.forEach { incomeDetails ->
                    ListItem(
                        headlineContent = {
                            if (incomeDetails.comment.isNotBlank()) {
                                Text(incomeDetails.comment)
                            }
                        },
                        trailingContent = {
                            Text(
                                text = moneyFormat(incomeDetails.amount),
                                style = MaterialTheme.typography.headlineSmall,
                                color = IncomeColor
                            )
                        },
                        modifier = Modifier.clickable {
                            selectedIncome(incomeDetails)
                        }
                    )
                    HorizontalDivider()
                }

                Text(
                    text = resources.getString(
                        R.string.total,
                        moneyFormat(incomesList.fold(0f) { acc, incomeDetails -> acc + incomeDetails.amount })
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