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
    operator fun invoke(accountId: String? = null): Flow<DashboardData> {
        return combine(
            transactionRepository.getAllActiveTransactions(),
            categoryRepository.getAllActiveCategories(),
            budgetRepository.getAllActiveBudgets()
        ) { transactions, categories, budgets ->
            
            val filteredTransactions = if (accountId != null) {
                transactions.filter { it.accountId == accountId }
            } else {
                transactions
            }

            // 1. Kalkulasi Saldo
            var totalIncome = 0.0
            var totalExpense = 0.0
            var incomeThisMonth = 0.0
            var expenseThisMonth = 0.0
            
            val calendar = Calendar.getInstance()
            val currentDayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
            val currentWeekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)

            val expensesByCategoryToday = mutableMapOf<String, Double>()
            val expensesByCategoryThisWeek = mutableMapOf<String, Double>()
            val expensesByCategoryThisMonth = mutableMapOf<String, Double>()
            
            var totalExpenseToday = 0.0
            var totalExpenseThisWeek = 0.0
            var totalExpenseThisMonth = 0.0

            var totalDebt = 0.0
            var totalReceivable = 0.0
            var activeDebt = 0.0
            var activeReceivable = 0.0

            for (tx in filteredTransactions) {
                if (tx.transactionType == "INCOME") {
                    totalIncome += tx.amount
                    val txCalendar = Calendar.getInstance().apply { timeInMillis = tx.transactionDate }
                    val isThisMonth = txCalendar.get(Calendar.MONTH) == currentMonth && 
                                      txCalendar.get(Calendar.YEAR) == currentYear
                    if (isThisMonth) incomeThisMonth += tx.amount
                } else if (tx.transactionType == "EXPENSE") {
                    totalExpense += tx.amount
                    
                    val txCalendar = Calendar.getInstance().apply { timeInMillis = tx.transactionDate }
                    val txYear = txCalendar.get(Calendar.YEAR)
                    
                    val isToday = txYear == currentYear && txCalendar.get(Calendar.DAY_OF_YEAR) == currentDayOfYear
                    val isThisWeek = txYear == currentYear && txCalendar.get(Calendar.WEEK_OF_YEAR) == currentWeekOfYear
                    val isThisMonth = txYear == currentYear && txCalendar.get(Calendar.MONTH) == currentMonth
                    
                    if (isToday) {
                        totalExpenseToday += tx.amount
                        tx.categoryId?.let { id ->
                            expensesByCategoryToday[id] = (expensesByCategoryToday[id] ?: 0.0) + tx.amount
                        }
                    }
                    if (isThisWeek) {
                        totalExpenseThisWeek += tx.amount
                        tx.categoryId?.let { id ->
                            expensesByCategoryThisWeek[id] = (expensesByCategoryThisWeek[id] ?: 0.0) + tx.amount
                        }
                    }
                    if (isThisMonth) {
                        totalExpenseThisMonth += tx.amount
                        tx.categoryId?.let { id ->
                            expensesByCategoryThisMonth[id] = (expensesByCategoryThisMonth[id] ?: 0.0) + tx.amount
                        }
                    }
                } else if (tx.transactionType == "DEBT") {
                    if (!tx.isDitalangin) {
                        totalIncome += tx.amount
                    }
                    totalDebt += tx.amount
                    if (tx.status != "LUNAS") {
                        activeDebt += tx.amount
                    }
                } else if (tx.transactionType == "RECEIVABLE") {
                    totalExpense += tx.amount
                    totalReceivable += tx.amount
                    if (tx.status != "LUNAS") {
                        activeReceivable += tx.amount
                    }
                }
            }

            val totalCash = totalIncome - totalExpense
            val totalBalance = totalCash + activeReceivable - activeDebt

            // 2. Map Pengeluaran Kategori (Hari Ini, Minggu Ini, Bulan Ini)
            val categoryExpensesToday = expensesByCategoryToday.mapNotNull { (catId, amount) ->
                val category = categories.find { it.id == catId }
                if (category != null) {
                    CategoryExpenseModel(
                        categoryId = catId,
                        categoryName = category.categoryName,
                        icon = category.icon,
                        color = category.color,
                        totalAmount = amount
                    )
                } else null
            }.sortedByDescending { it.totalAmount }

            val categoryExpensesThisWeek = expensesByCategoryThisWeek.mapNotNull { (catId, amount) ->
                val category = categories.find { it.id == catId }
                if (category != null) {
                    CategoryExpenseModel(
                        categoryId = catId,
                        categoryName = category.categoryName,
                        icon = category.icon,
                        color = category.color,
                        totalAmount = amount
                    )
                } else null
            }.sortedByDescending { it.totalAmount }

            val categoryExpensesThisMonth = expensesByCategoryThisMonth.mapNotNull { (catId, amount) ->
                val category = categories.find { it.id == catId }
                if (category != null) {
                    CategoryExpenseModel(
                        categoryId = catId,
                        categoryName = category.categoryName,
                        icon = category.icon,
                        color = category.color,
                        totalAmount = amount
                    )
                } else null
            }.sortedByDescending { it.totalAmount }

            // 3. Map Progress Budget (Bulan Ini)
            val budgetProgressModels = budgets.mapNotNull { budget ->
                val category = categories.find { it.id == budget.categoryId }
                if (category != null) {
                    val usedAmount = expensesByCategoryThisMonth[budget.categoryId] ?: 0.0
                    BudgetProgressModel(
                        budgetId = budget.id,
                        budgetName = budget.budgetName,
                        categoryId = budget.categoryId,
                        icon = category.icon,
                        color = category.color,
                        limitAmount = budget.amountLimit,
                        usedAmount = usedAmount
                    )
                } else null
            }

            DashboardData(
                totalBalance = totalBalance,
                incomeThisMonth = incomeThisMonth,
                expenseThisMonth = totalExpenseThisMonth,
                categoryExpensesToday = categoryExpensesToday,
                categoryExpensesThisWeek = categoryExpensesThisWeek,
                categoryExpensesThisMonth = categoryExpensesThisMonth,
                totalExpenseToday = totalExpenseToday,
                totalExpenseThisWeek = totalExpenseThisWeek,
                totalExpenseThisMonth = totalExpenseThisMonth,
                monthlyBudgets = budgetProgressModels,
                totalDebt = activeDebt,
                totalReceivable = activeReceivable
            )
        }
    }
}
