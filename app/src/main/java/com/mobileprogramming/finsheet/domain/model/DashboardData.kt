package com.mobileprogramming.finsheet.domain.model

data class DashboardData(
    val totalBalance: Long,
    val incomeThisMonth: Long,
    val expenseThisMonth: Long,
    val categoryExpenses: List<CategoryExpenseModel>,
    val monthlyBudgets: List<BudgetProgressModel>
)

data class CategoryExpenseModel(
    val categoryId: String,
    val categoryName: String,
    val icon: String? = null,
    val color: String? = null,
    val totalAmount: Long
)

data class BudgetProgressModel(
    val budgetId: String,
    val budgetName: String,
    val categoryId: String,
    val icon: String? = null,
    val color: String? = null,
    val limitAmount: Long,
    val usedAmount: Long
)
