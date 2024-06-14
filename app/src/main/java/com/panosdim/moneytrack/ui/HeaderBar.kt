package com.panosdim.moneytrack.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.panosdim.moneytrack.R
import com.panosdim.moneytrack.paddingLarge
import kotlinx.coroutines.launch

@Composable
fun HeaderBar(
    onSort: () -> Unit,
    isFilterSet: Boolean,
    showBackToTop: Boolean,
    listState: LazyListState,
    onFilter: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = paddingLarge, end = paddingLarge),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = { onSort() }) {
            Icon(
                Icons.AutoMirrored.Filled.Sort,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(
                stringResource(id = R.string.sort)
            )
        }

        if (showBackToTop) {
            TextButton(onClick = { scope.launch { listState.animateScrollToItem(0) } }) {
                Icon(
                    Icons.Default.ArrowUpward,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(
                    stringResource(id = R.string.back_to_top)
                )
            }
        }

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