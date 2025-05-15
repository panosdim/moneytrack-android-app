package com.panosdim.moneytrack.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.panosdim.moneytrack.models.Expense
import com.panosdim.moneytrack.paddingLarge
import com.panosdim.moneytrack.ui.theme.ExpenseColor
import com.panosdim.moneytrack.utils.formatDate
import com.panosdim.moneytrack.utils.moneyFormat
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesOnCategorySheet(
    bottomSheetState: SheetState,
    category: String,
    expenses: List<Expense>
) {
    val scope = rememberCoroutineScope()
    val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    val listState = rememberLazyListState()

    // Sheet content
    if (bottomSheetState.isVisible) {
        ModalBottomSheet(
            onDismissRequest = { scope.launch { bottomSheetState.hide() } },
            sheetState = bottomSheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = paddingLarge, end = paddingLarge)
            ) {
                Text(
                    text = category,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxWidth(),
                )

                LazyColumn(state = listState) {
                    items(expenses) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    it.date.formatDate(
                                        dateFormatter,
                                        addTodayAndYesterdayInfo = false
                                    ),
                                    style = MaterialTheme.typography.headlineSmall,
                                )
                            },
                            supportingContent = {
                                if (it.comment.isNotBlank()) {
                                    Text(it.comment)
                                }
                            },
                            trailingContent = {
                                Text(
                                    text = moneyFormat(it.amount),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = ExpenseColor
                                )
                            },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}