package com.mobileprogramming.finsheet.domain.usecase.budget

import com.mobileprogramming.finsheet.data.local.entity.BudgetEntity
import com.mobileprogramming.finsheet.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.first
import java.util.UUID
import java.util.Calendar

class SaveCategoryBudgetsUseCase(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(categoryId: String, budgetName: String, amountLimit: Double) {
        val activeBudgets = budgetRepository.getAllActiveBudgets().first()
        val existing = activeBudgets.find { it.categoryId == categoryId }
        
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)

        if (existing != null) {
            val updated = existing.copy(
                budgetName = budgetName,
                amountLimit = amountLimit,
                updatedAt = System.currentTimeMillis(),
                syncStatus = "PENDING"
            )
            budgetRepository.updateBudget(updated)
        } else {
            val newBudget = BudgetEntity(
                id = UUID.randomUUID().toString(),
                categoryId = categoryId,
                budgetName = budgetName,
                amountLimit = amountLimit,
                startDate = currentMonth.toLong(),
                endDate = currentYear.toLong(),
                syncStatus = "PENDING",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            budgetRepository.insertBudget(newBudget)
        }
    }
}
