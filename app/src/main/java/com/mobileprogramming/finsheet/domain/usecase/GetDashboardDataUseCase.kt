package com.mobileprogramming.finsheet.domain.usecase

import com.mobileprogramming.finsheet.domain.model.BudgetProgressModel
import com.mobileprogramming.finsheet.domain.model.CategoryExpenseModel
import com.mobileprogramming.finsheet.domain.model.DashboardData
import com.mobileprogramming.finsheet.domain.repository.BudgetRepository
import com.mobileprogramming.finsheet.domain.repository.CategoryRepository
import com.mobileprogramming.finsheet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar

class GetDashboardDataUseCase(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository
) {
    operator fun invoke(): Flow<DashboardData> {
        return combine(
            transactionRepository.getAllActiveTransactions(),
            categoryRepository.getAllActiveCategories(),
            budgetRepository.getAllActiveBudgets()
        ) { transactions, categories, budgets ->
            
            // 1. Kalkulasi Saldo
            var totalIncome = 0L
            var totalExpense = 0L
            var incomeThisMonth = 0L
            var expenseThisMonth = 0L
            
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)

            val expensesByCategory = mutableMapOf<String, Long>()

            for (tx in transactions) {
                val txCalendar = Calendar.getInstance().apply { timeInMillis = tx.transactionDate }
                val isThisMonth = txCalendar.get(Calendar.MONTH) == currentMonth && 
                                  txCalendar.get(Calendar.YEAR) == currentYear

                if (tx.transactionType == "INCOME") {
                    totalIncome += tx.amount
                    if (isThisMonth) incomeThisMonth += tx.amount
                } else if (tx.transactionType == "EXPENSE") {
                    totalExpense += tx.amount
                    if (isThisMonth) {
                        expenseThisMonth += tx.amount
                        tx.categoryId?.let { id ->
                            expensesByCategory[id] = (expensesByCategory[id] ?: 0L) + tx.amount
                        }
                    }
                }
            }

            val totalBalance = totalIncome - totalExpense

            // 2. Map Pengeluaran Kategori (Bulan Ini)
            val categoryExpenseModels = expensesByCategory.mapNotNull { (catId, amount) ->
                val category = categories.find { it.id == catId }
                if (category != null) {
                    CategoryExpenseModel(
                        categoryId = catId,
                        categoryName = category.categoryName,
                        totalAmount = amount
                    )
                } else null
            }.sortedByDescending { it.totalAmount }

            // 3. Map Progress Budget (Bulan Ini)
            val budgetProgressModels = budgets.mapNotNull { budget ->
                val category = categories.find { it.id == budget.categoryId }
                if (category != null) {
                    val usedAmount = expensesByCategory[budget.categoryId] ?: 0L
                    BudgetProgressModel(
                        budgetId = budget.id,
                        budgetName = budget.budgetName,
                        categoryId = budget.categoryId,
                        limitAmount = budget.amountLimit.toLong(),
                        usedAmount = usedAmount
                    )
                } else null
            }

            DashboardData(
                totalBalance = totalBalance,
                incomeThisMonth = incomeThisMonth,
                expenseThisMonth = expenseThisMonth,
                categoryExpenses = categoryExpenseModels,
                monthlyBudgets = budgetProgressModels
            )
        }
    }
}
