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
import com.mobileprogramming.finsheet.ui.features.auth.LoginScreen

@Composable
fun FinSheetNavGraph(
    navController: NavHostController,
    startDestination: Any = Screen.Dashboard
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<Screen.Login> {
            LoginScreen(
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard) {
                        popUpTo<Screen.Login> { inclusive = true }
                    }
                }
            )
        }
        
        composable<Screen.Dashboard> {
            DashboardScreen(
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History)
                },
                onNavigateToReport = {
                    navController.navigate(Screen.Report)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings)
                },
                onNavigateToAnggaran = {
                    navController.navigate(Screen.Budget)
                }
            )
        }
        
        composable<Screen.History> {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction)
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard) {
                        popUpTo<Screen.Dashboard> { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToTransaction = {
                    navController.navigate(Screen.AddTransaction)
                }
            )
        }

        composable<Screen.AddTransaction> {
            AddTransactionScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSelectCategory = {
                    navController.navigate(Screen.SelectCategory)
                },
                onNavigateToAddCategory = {
                    navController.navigate(Screen.AddCategory)
                }
            )
        }

        composable<Screen.SelectCategory> {
            SelectCategoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddCategory = {
                    navController.navigate(Screen.AddCategory)
                }
            )
        }

        composable<Screen.AddCategory> {
            AddCategoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable<Screen.Report> {
            ReportScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable<Screen.Settings> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToBeranda = {
                    navController.navigate(Screen.Dashboard) {
                        popUpTo<Screen.Dashboard> { inclusive = true }
                    }
                },
                onNavigateToTransaksi = {
                    navController.navigate(Screen.History)
                },
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction)
                },
                onNavigateToAnggaran = {
                    navController.navigate(Screen.Budget)
                }
            )
        }
        
        composable<Screen.Budget> {
            com.mobileprogramming.finsheet.ui.features.budget.BudgetScreen(
                onNavigateToBeranda = {
                    navController.navigate(Screen.Dashboard) {
                        popUpTo<Screen.Dashboard> { inclusive = true }
                    }
                },
                onNavigateToTransaksi = {
                    navController.navigate(Screen.History)
                },
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings)
                },
                onNavigateToAddBudget = {
                    navController.navigate(Screen.AddBudget)
                }
            )
        }

        composable<Screen.AddBudget> {
            AddBudgetScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}