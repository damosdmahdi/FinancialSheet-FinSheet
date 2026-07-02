package com.mobileprogramming.finsheet.domain.model

data class DashboardData(
    val totalBalance: Double,
    val incomeThisMonth: Double,
    val expenseThisMonth: Double,
    val categoryExpenses: List<CategoryExpenseModel>,
    val monthlyBudgets: List<BudgetProgressModel>
)

data class CategoryExpenseModel(
    val categoryId: String,
    val categoryName: String,
    val icon: String? = null,
    val color: String? = null,
    val totalAmount: Double
)

data class BudgetProgressModel(
    val budgetId: String,
    val budgetName: String,
    val categoryId: String,
    val icon: String? = null,
    val color: String? = null,
    val limitAmount: Double,
    val usedAmount: Double
)
