package com.mobileprogramming.finsheet.domain.usecase.budget

import com.mobileprogramming.finsheet.domain.repository.BudgetRepository
import com.mobileprogramming.finsheet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar

enum class BudgetExceedType {
    DAILY,
    WEEKLY,
    MONTHLY,
    GLOBAL_MONTHLY
}

data class BudgetCheckResult(
    val type: BudgetExceedType,
    val categoryName: String?,
    val budgetLimit: Long,
    val spentAmount: Long
)

class CheckTransactionBudgetLimitUseCase(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        categoryId: String?,
        amount: Long,
        date: Long,
        globalMonthlyLimit: Long
    ): List<BudgetCheckResult> {
        val results = mutableListOf<BudgetCheckResult>()
        
        val allTransactions = transactionRepository.getAllActiveTransactions().first()
        val targetCalendar = Calendar.getInstance().apply { timeInMillis = date }
        val targetMonth = targetCalendar.get(Calendar.MONTH)
        val targetYear = targetCalendar.get(Calendar.YEAR)

        if (categoryId != null) {
            val activeBudgets = budgetRepository.getAllActiveBudgets().first()
            val categoryBudget = activeBudgets.find { it.categoryId == categoryId }
            
            if (categoryBudget != null) {
                val targetDay = targetCalendar.get(Calendar.DAY_OF_YEAR)
                val targetWeek = targetCalendar.get(Calendar.WEEK_OF_YEAR)
                
                val categoryExpenses = allTransactions.filter { 
                    it.categoryId == categoryId && it.transactionType == "EXPENSE" 
                }

                // DAILY CHECK (Category specific)
                val dailyLimit = categoryBudget.amountLimit / 30
                val dailySpentExcludingNew = categoryExpenses.filter { tx ->
                    val cal = Calendar.getInstance().apply { timeInMillis = tx.transactionDate }
                    cal.get(Calendar.DAY_OF_YEAR) == targetDay && cal.get(Calendar.YEAR) == targetYear
                }.sumOf { it.amount }
                val dailySpentWithNew = dailySpentExcludingNew + amount
                if (dailySpentWithNew > dailyLimit) {
                    results.add(
                        BudgetCheckResult(
                            type = BudgetExceedType.DAILY,
                            categoryName = categoryBudget.budgetName.substringAfter("Batas Anggaran ").substringAfter("Budget "),
                            budgetLimit = dailyLimit,
                            spentAmount = dailySpentWithNew
                        )
                    )
                }

                // WEEKLY CHECK (Category specific)
                val weeklyLimit = categoryBudget.amountLimit / 4
                val weeklySpentExcludingNew = categoryExpenses.filter { tx ->
                    val cal = Calendar.getInstance().apply { timeInMillis = tx.transactionDate }
                    cal.get(Calendar.WEEK_OF_YEAR) == targetWeek && cal.get(Calendar.YEAR) == targetYear
                }.sumOf { it.amount }
                val weeklySpentWithNew = weeklySpentExcludingNew + amount
                if (weeklySpentWithNew > weeklyLimit) {
                    results.add(
                        BudgetCheckResult(
                            type = BudgetExceedType.WEEKLY,
                            categoryName = categoryBudget.budgetName.substringAfter("Batas Anggaran ").substringAfter("Budget "),
                            budgetLimit = weeklyLimit,
                            spentAmount = weeklySpentWithNew
                        )
                    )
                }

                // MONTHLY CHECK (Category specific)
                val monthlyLimit = categoryBudget.amountLimit
                val monthlySpentExcludingNew = categoryExpenses.filter { tx ->
                    val cal = Calendar.getInstance().apply { timeInMillis = tx.transactionDate }
                    cal.get(Calendar.MONTH) == targetMonth && cal.get(Calendar.YEAR) == targetYear
                }.sumOf { it.amount }
                val monthlySpentWithNew = monthlySpentExcludingNew + amount
                if (monthlySpentWithNew > monthlyLimit) {
                    results.add(
                        BudgetCheckResult(
                            type = BudgetExceedType.MONTHLY,
                            categoryName = categoryBudget.budgetName.substringAfter("Batas Anggaran ").substringAfter("Budget "),
                            budgetLimit = monthlyLimit,
                            spentAmount = monthlySpentWithNew
                        )
                    )
                }
            }
        }

        // GLOBAL MONTHLY CHECK
        val globalExpensesThisMonth = allTransactions.filter { tx ->
            tx.transactionType == "EXPENSE" &&
            Calendar.getInstance().apply { timeInMillis = tx.transactionDate }.let { cal ->
                cal.get(Calendar.MONTH) == targetMonth && cal.get(Calendar.YEAR) == targetYear
            }
        }.sumOf { it.amount }
        val globalSpentWithNew = globalExpensesThisMonth + amount
        if (globalSpentWithNew > globalMonthlyLimit) {
            results.add(
                BudgetCheckResult(
                    type = BudgetExceedType.GLOBAL_MONTHLY,
                    categoryName = null,
                    budgetLimit = globalMonthlyLimit,
                    spentAmount = globalSpentWithNew
                )
            )
        }

        return results
    }
}
