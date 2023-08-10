package com.panosdim.moneytrack.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.paddingLarge

@Composable
fun <T> HeaderBar(
    onSort: () -> Unit,
    listToSearch: List<T>,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    isFilterSet: Boolean,
    onFilter: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = paddingLarge, end = paddingLarge),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = { onSort() }) {
            Icon(
                Icons.Default.Sort,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                stringResource(id = R.string.sort)
            )
        }

        SearchDialog(
            listToSearch = listToSearch,
            searchQuery = searchQuery,
            onSearchChanged = onSearchChanged
        )

        TextButton(
            onClick = { onFilter() },
            colors = ButtonDefaults.textButtonColors(
                contentColor = if (isFilterSet) {
                    Color.Green
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        ) {
            Icon(
                Icons.Default.FilterList,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                stringResource(id = R.string.filter)
            )
        }
    }
}