package com.mobileprogramming.finsheet.ui.navigation

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    object Dashboard : Screen()
    @Serializable
    object AddTransaction : Screen()
    @Serializable
    object SelectCategory : Screen()
    @Serializable
    object AddCategory : Screen()
    @Serializable
    object History : Screen()
    @Serializable
    object Report : Screen()
    @Serializable
    object Settings : Screen()
    @Serializable
    object Budget : Screen()
    @Serializable
    object AddBudget : Screen()
    @Serializable
    object Login : Screen()
}