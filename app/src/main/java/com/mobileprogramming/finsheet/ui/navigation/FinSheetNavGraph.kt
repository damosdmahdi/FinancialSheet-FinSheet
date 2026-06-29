package com.mobileprogramming.finsheet.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mobileprogramming.finsheet.ui.features.addtransaction.AddCategoryScreen
import com.mobileprogramming.finsheet.ui.features.addtransaction.AddTransactionScreen
import com.mobileprogramming.finsheet.ui.features.addtransaction.SelectCategoryScreen
import com.mobileprogramming.finsheet.ui.features.dashboard.DashboardScreen
import com.mobileprogramming.finsheet.ui.features.history.HistoryScreen
import com.mobileprogramming.finsheet.ui.features.report.ReportScreen
import com.mobileprogramming.finsheet.ui.features.settings.SettingsScreen
import com.mobileprogramming.finsheet.ui.features.budget.AddBudgetScreen
@Composable
fun FinSheetNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Dashboard.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToReport = {
                    navController.navigate(Screen.Report.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToAnggaran = {
                    navController.navigate(Screen.Budget.route)
                }
            )
        }
        
        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction.route)
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToTransaction = {
                    navController.navigate(Screen.AddTransaction.route)
                }
            )
        }

        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSelectCategory = {
                    navController.navigate(Screen.SelectCategory.route)
                },
                onNavigateToAddCategory = {
                    navController.navigate(Screen.AddCategory.route)
                }
            )
        }

        composable(Screen.SelectCategory.route) {
            SelectCategoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddCategory = {
                    navController.navigate(Screen.AddCategory.route)
                }
            )
        }

        composable(Screen.AddCategory.route) {
            AddCategoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Report.route) {
            ReportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBeranda = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateToTransaksi = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction.route)
                },
                onNavigateToAnggaran = {
                    navController.navigate(Screen.Budget.route)
                }
            )
        }
        
        composable(Screen.Budget.route) {
            com.mobileprogramming.finsheet.ui.features.budget.BudgetScreen(
                onNavigateToBeranda = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onNavigateToTransaksi = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToAddBudget = {
                    navController.navigate(Screen.AddBudget.route)
                }
            )
        }

        composable(Screen.AddBudget.route) {
            AddBudgetScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}