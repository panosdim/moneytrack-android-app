package com.panosdim.moneytrack.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.panosdim.moneytrack.ui.categories.CategoriesScreen
import com.panosdim.moneytrack.ui.expenses.ExpensesScreen
import com.panosdim.moneytrack.ui.income.IncomeScreen
import com.panosdim.moneytrack.utils.BottomNavItem


@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigation(navController = navController) }
    ) { innerPadding ->
        NavigationGraph(navController = navController, innerPadding)
    }
}

@Composable
fun NavigationGraph(navController: NavHostController, paddingValues: PaddingValues) {
    NavHost(
        navController,
        startDestination = BottomNavItem.Expenses.screenRoute,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(BottomNavItem.Expenses.screenRoute) {
            ExpensesScreen()
        }
        composable(BottomNavItem.Income.screenRoute) {
            IncomeScreen()
        }
        composable(BottomNavItem.Categories.screenRoute) {
            CategoriesScreen()
        }
        composable(BottomNavItem.Dashboard.screenRoute) {
            DashboardScreen()
        }
    }

    // Disable back button in Navigation Bar
    BackHandler {}
}

@Composable
fun BottomNavigation(navController: NavController) {
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf(
        BottomNavItem.Expenses,
        BottomNavItem.Income,
        BottomNavItem.Categories,
        BottomNavItem.Dashboard,
    )

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(item.icon, item.title) },
                label = { Text(item.title) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    navController.navigate(item.screenRoute) {

                        navController.graph.startDestinationRoute?.let { screen_route ->
                            popUpTo(screen_route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}