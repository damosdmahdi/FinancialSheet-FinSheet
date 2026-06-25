package com.mobileprogramming.finsheet.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object AddTransaction : Screen("add_transaction")
    object SelectCategory : Screen("select_category")
    object AddCategory : Screen("add_category")
    object History : Screen("history")
    object Report : Screen("report")
    object Settings : Screen("settings")
}
