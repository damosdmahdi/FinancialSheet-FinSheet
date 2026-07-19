package com.mobileprogramming.finsheet.domain.model

data class DashboardData(
    val totalBalance: Double,
    val incomeThisMonth: Double,
    val expenseThisMonth: Double,
    val categoryExpensesToday: List<CategoryExpenseModel>,
    val categoryExpensesThisWeek: List<CategoryExpenseModel>,
    val categoryExpensesThisMonth: List<CategoryExpenseModel>,
    val totalExpenseToday: Double,
    val totalExpenseThisWeek: Double,
    val totalExpenseThisMonth: Double,
    val monthlyBudgets: List<BudgetProgressModel>,
    val totalDebt: Double = 0.0,
    val totalReceivable: Double = 0.0
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
