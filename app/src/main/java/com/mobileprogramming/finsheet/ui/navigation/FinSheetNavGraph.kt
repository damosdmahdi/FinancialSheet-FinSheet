package com.mobileprogramming.finsheet.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mobileprogramming.finsheet.ui.features.addtransaction.AddTransactionScreen
import com.mobileprogramming.finsheet.ui.features.dashboard.DashboardScreen
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
        
        composable(Screen.AddTransaction.route) {
            AddTransactionScreen(
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
                onNavigateBack = { navController.popBackStack() }
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
