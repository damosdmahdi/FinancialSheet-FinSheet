package com.mobileprogramming.finsheet.domain.repository

import com.mobileprogramming.finsheet.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getAllActiveBudgets(): Flow<List<BudgetEntity>>
}