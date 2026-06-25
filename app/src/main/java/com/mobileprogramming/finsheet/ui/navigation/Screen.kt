package com.mobileprogramming.finsheet.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object AddTransaction : Screen("add_transaction")
    object Report : Screen("report")
    object Settings : Screen("settings")
    object Budget : Screen("budget")
    object AddBudget : Screen("add_budget")
}
