package com.mobileprogramming.finsheet.domain.usecase.budget

import com.mobileprogramming.finsheet.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.first

class DeleteBudgetUseCase(
    private val budgetRepository: BudgetRepository
) {
    suspend operator fun invoke(id: String) {
        budgetRepository.deleteBudget(id)
    }
    
    suspend fun deleteByCategoryId(categoryId: String) {
        val activeBudgets = budgetRepository.getAllActiveBudgets().first()
        val budget = activeBudgets.find { it.categoryId == categoryId }
        if (budget != null) {
            budgetRepository.deleteBudget(budget.id)
        }
    }
}
