package com.mobileprogramming.finsheet.data.repository

import com.mobileprogramming.finsheet.data.local.dao.BudgetDao
import com.mobileprogramming.finsheet.data.local.entity.BudgetEntity
import com.mobileprogramming.finsheet.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow

class BudgetRepositoryImpl(private val budgetDao: BudgetDao) : BudgetRepository {
    override fun getAllActiveBudgets(): Flow<List<BudgetEntity>> =
        budgetDao.getAllActiveBudgets()
}