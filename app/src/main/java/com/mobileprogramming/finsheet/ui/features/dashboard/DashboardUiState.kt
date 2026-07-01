package com.mobileprogramming.finsheet.ui.features.dashboard

data class DashboardUiState(
    val totalBalance: String = "Rp 0",
    val incomeThisMonth: String = "Rp 0",
    val expenseThisMonth: String = "Rp 0",
    val selectedFilterIndex: Int = 2, // 0: Hari Ini, 1: Minggu Ini, 2: Bulan Ini
    val totalExpenseForFilter: String = "Rp 0",
    val categoryExpenses: List<CategoryExpenseData> = emptyList(),
    val monthlyBudgets: List<BudgetProgressData> = emptyList()
)

data class CategoryExpenseData(
    val iconName: String?,
    val colorHex: String?,
    val categoryName: String,
    val percentage: String
)

data class BudgetProgressData(
    val iconName: String?,
    val colorHex: String?,
    val budgetName: String,
    val percentage: String,
    val progress: Float,
    val usedAmountStr: String,
    val totalAmountStr: String,
    val remainingAmountStr: String
)
