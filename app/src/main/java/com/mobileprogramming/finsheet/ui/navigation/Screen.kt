package com.mobileprogramming.finsheet.ui.navigation

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    object Dashboard : Screen()
    @Serializable
    data class AddTransaction(val transactionId: String? = null) : Screen()
    @Serializable
    object SelectCategory : Screen()
    @Serializable
    data class AddCategory(val categoryId: String? = null, val type: String? = null) : Screen()
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
    @Serializable
    object AccountList : Screen()
    @Serializable
    data class AddEditAccount(val accountId: String? = null) : Screen()
    @Serializable
    data class AddTransfer(val transferId: String? = null) : Screen()
    @Serializable
    data class AddEditReminder(val reminderId: String? = null) : Screen()
}