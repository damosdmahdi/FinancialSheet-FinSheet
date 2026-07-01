package com.mobileprogramming.finsheet.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.mobileprogramming.finsheet.ui.features.addtransaction.AddCategoryScreen
import com.mobileprogramming.finsheet.ui.features.addtransaction.AddTransactionScreen
import com.mobileprogramming.finsheet.ui.features.addtransaction.SelectCategoryScreen
import com.mobileprogramming.finsheet.ui.features.dashboard.DashboardScreen
import com.mobileprogramming.finsheet.ui.features.history.HistoryScreen
import com.mobileprogramming.finsheet.ui.features.report.ReportScreen
import com.mobileprogramming.finsheet.ui.features.settings.SettingsScreen
import com.mobileprogramming.finsheet.ui.features.budget.AddBudgetScreen
import com.mobileprogramming.finsheet.ui.features.auth.LoginScreen
import androidx.navigation.NavGraph.Companion.findStartDestination

private fun NavHostController.navigateBottomNav(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

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
                    navController.navigate(Screen.AddTransaction())
                },
                onNavigateToHistory = {
                    navController.navigateBottomNav(Screen.History)
                },
                onNavigateToReport = {
                    navController.navigate(Screen.Report)
                },
                onNavigateToSettings = {
                    navController.navigateBottomNav(Screen.Settings)
                },
                onNavigateToAnggaran = {
                    navController.navigateBottomNav(Screen.Budget)
                }
            )
        }
        
        composable<Screen.History> {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction())
                },
                onNavigateToDashboard = {
                    navController.navigateBottomNav(Screen.Dashboard)
                },
                onNavigateToTransaction = {
                    navController.navigate(Screen.AddTransaction)
                },
                onNavigateToAnggaran = {
                    navController.navigateBottomNav(Screen.Budget)
                },
                onNavigateToSettings = {
                    navController.navigateBottomNav(Screen.Settings)
                }
            )
        }

        composable<Screen.AddTransaction> { backStackEntry ->
            val args = backStackEntry.arguments
            val transactionId = args?.getString("transactionId") // Need to extract from savedStateHandle or args? Wait, with serialization it's backStackEntry.toRoute<Screen.AddTransaction>(). Let's use toRoute
            val route = backStackEntry.toRoute<Screen.AddTransaction>()
            val context = androidx.compose.ui.platform.LocalContext.current
            val viewModel: com.mobileprogramming.finsheet.ui.features.addtransaction.AddEditTransactionViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.mobileprogramming.finsheet.di.Injection.provideTransactionViewModelFactory(context)
            )
            androidx.compose.runtime.LaunchedEffect(route.transactionId) {
                viewModel.initForEdit(route.transactionId)
            }
            AddTransactionScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSelectCategory = {
                    navController.navigate(Screen.SelectCategory)
                },
                onNavigateToAddCategory = {
                    navController.navigate(Screen.AddCategory)
                }
            )
        }

        composable<Screen.SelectCategory> { backStackEntry ->
            val parentEntry = androidx.compose.runtime.remember(backStackEntry) {
                navController.getBackStackEntry<Screen.AddTransaction>()
            }
            val context = androidx.compose.ui.platform.LocalContext.current
            val viewModel: com.mobileprogramming.finsheet.ui.features.addtransaction.AddEditTransactionViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                parentEntry,
                factory = com.mobileprogramming.finsheet.di.Injection.provideTransactionViewModelFactory(context)
            )
            SelectCategoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddCategory = {
                    navController.navigate(Screen.AddCategory)
                }
            )
        }

        composable<Screen.AddCategory> {
            val context = androidx.compose.ui.platform.LocalContext.current
            val viewModel: com.mobileprogramming.finsheet.ui.features.addtransaction.AddCategoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.mobileprogramming.finsheet.di.Injection.provideTransactionViewModelFactory(context)
            )
            AddCategoryScreen(
                viewModel = viewModel,
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
                    navController.navigateBottomNav(Screen.Dashboard)
                },
                onNavigateToTransaksi = {
                    navController.navigateBottomNav(Screen.History)
                },
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction())
                },
                onNavigateToAnggaran = {
                    navController.navigateBottomNav(Screen.Budget)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable<Screen.Budget> {
            com.mobileprogramming.finsheet.ui.features.budget.BudgetScreen(
                onNavigateToBeranda = {
                    navController.navigateBottomNav(Screen.Dashboard)
                },
                onNavigateToTransaksi = {
                    navController.navigateBottomNav(Screen.History)
                },
                onNavigateToAddTransaction = {
                    navController.navigate(Screen.AddTransaction())
                },
                onNavigateToSettings = {
                    navController.navigateBottomNav(Screen.Settings)
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