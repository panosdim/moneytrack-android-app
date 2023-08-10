package com.panosdim.moneytrack.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Loyalty
import androidx.compose.material.icons.filled.Payments
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(var title: String, var icon: ImageVector, var screenRoute: String) {
    data object Expenses : BottomNavItem("Expenses", Icons.Default.Payments, "expenses")
    data object Income : BottomNavItem("Income", Icons.Default.AccountBalanceWallet, "income")
    data object Categories : BottomNavItem("Categories", Icons.Default.Loyalty, "categories")
    data object Dashboard : BottomNavItem("Dashboard", Icons.Default.Dashboard, "dashboard")
}