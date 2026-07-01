package com.mobileprogramming.finsheet.domain.repository

import com.mobileprogramming.finsheet.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getAllActiveBudgets(): Flow<List<BudgetEntity>>
    suspend fun insertBudget(budget: BudgetEntity)
    suspend fun updateBudget(budget: BudgetEntity)
    suspend fun deleteBudget(id: String)
}