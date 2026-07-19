package com.mobileprogramming.finsheet.domain.repository

import com.mobileprogramming.finsheet.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getAllActiveBudgets(): Flow<List<BudgetEntity>>
    suspend fun insertBudget(budget: BudgetEntity)
    suspend fun updateBudget(budget: BudgetEntity)
    suspend fun deleteBudget(id: String)
    
    suspend fun insertBudgetMutation(mutation: com.mobileprogramming.finsheet.data.local.entity.BudgetMutationEntity)
    fun getAllBudgetMutationsFlow(): Flow<List<com.mobileprogramming.finsheet.data.local.entity.BudgetMutationEntity>>
    suspend fun getAllBudgetMutations(): List<com.mobileprogramming.finsheet.data.local.entity.BudgetMutationEntity>
}