package com.mobileprogramming.finsheet.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
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
import com.mobileprogramming.finsheet.di.Injection

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
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(120, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(120))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(120, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(120))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(120, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(120))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(120, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(120))
        }
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
                },
                onNavigateToAddTransfer = {
                    navController.navigate(Screen.AddTransfer())
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
                onNavigateToTransaction = { transactionId ->
                    navController.navigate(Screen.AddTransaction(transactionId = transactionId))
                },
                onNavigateToTransfer = { transferId ->
                    navController.navigate(Screen.AddTransfer(transferId = transferId))
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

            val newCategoryId by backStackEntry.savedStateHandle.getStateFlow<String?>("new_category_id", null).collectAsState()
            LaunchedEffect(newCategoryId) {
                newCategoryId?.let { id ->
                    viewModel.selectCategoryById(id)
                    backStackEntry.savedStateHandle.set("new_category_id", null)
                }
            }

            AddTransactionScreen(
                viewModel = viewModel,
                transactionId = route.transactionId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddCategory = {
                    navController.navigate(Screen.AddCategory(type = viewModel.state.value.transactionType))
                },
                onNavigateToEditCategory = { categoryId ->
                    navController.navigate(Screen.AddCategory(categoryId = categoryId, type = viewModel.state.value.transactionType))
                }
            )
        }

        composable<Screen.AddCategory> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.AddCategory>()
            val context = androidx.compose.ui.platform.LocalContext.current
            val viewModel: com.mobileprogramming.finsheet.ui.features.addtransaction.AddCategoryViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = com.mobileprogramming.finsheet.di.Injection.provideTransactionViewModelFactory(context)
            )
            androidx.compose.runtime.LaunchedEffect(route.categoryId, route.type) {
                viewModel.initForEdit(route.categoryId, route.type)
            }
            AddCategoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onCategorySaved = { categoryId ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("new_category_id", categoryId)
                }
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
                },
                onNavigateToAddAccount = {
                    navController.navigate(Screen.AddEditAccount())
                },
                onNavigateToEditAccount = { accountId ->
                    navController.navigate(Screen.AddEditAccount(accountId = accountId))
                },
                onNavigateToAddReminder = {
                    navController.navigate(Screen.AddEditReminder())
                },
                onNavigateToEditReminder = { id ->
                    navController.navigate(Screen.AddEditReminder(reminderId = id))
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
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddCategory = {
                    navController.navigate(Screen.AddCategory(type = "EXPENSE"))
                },
                onNavigateToEditCategory = { categoryId ->
                    navController.navigate(Screen.AddCategory(categoryId = categoryId, type = "EXPENSE"))
                }
            )
        }

        composable<Screen.AccountList> {
            val context = androidx.compose.ui.platform.LocalContext.current
            val viewModel: com.mobileprogramming.finsheet.ui.features.account.AccountViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = Injection.provideAccountViewModelFactory(context)
            )
            com.mobileprogramming.finsheet.ui.features.account.AccountListScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddAccount = { navController.navigate(Screen.AddEditAccount()) },
                onNavigateToEditAccount = { accountId -> navController.navigate(Screen.AddEditAccount(accountId = accountId)) }
            )
        }

        composable<Screen.AddEditAccount> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.AddEditAccount>()
            val context = androidx.compose.ui.platform.LocalContext.current
            val viewModel: com.mobileprogramming.finsheet.ui.features.account.AccountViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = Injection.provideAccountViewModelFactory(context)
            )
            com.mobileprogramming.finsheet.ui.features.account.AddEditAccountScreen(
                viewModel = viewModel,
                accountId = route.accountId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.AddTransfer> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.AddTransfer>()
            val context = androidx.compose.ui.platform.LocalContext.current
            val viewModel: com.mobileprogramming.finsheet.ui.features.transfer.TransferViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = Injection.provideTransferViewModelFactory(context)
            )
            androidx.compose.runtime.LaunchedEffect(route.transferId) {
                viewModel.initForm(route.transferId)
            }
            com.mobileprogramming.finsheet.ui.features.transfer.AddTransferScreen(
                viewModel = viewModel,
                transferId = route.transferId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.AddEditReminder> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.AddEditReminder>()
            val context = androidx.compose.ui.platform.LocalContext.current
            val viewModel: com.mobileprogramming.finsheet.ui.features.settings.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                factory = Injection.provideSettingsViewModelFactory(context)
            )
            com.mobileprogramming.finsheet.ui.features.settings.AddEditReminderScreen(
                viewModel = viewModel,
                reminderId = route.reminderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}