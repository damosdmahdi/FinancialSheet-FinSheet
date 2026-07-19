package com.mobileprogramming.finsheet.data.repository

import com.mobileprogramming.finsheet.data.local.dao.BudgetDao
import com.mobileprogramming.finsheet.data.local.dao.BudgetMutationDao
import com.mobileprogramming.finsheet.data.local.entity.BudgetEntity
import com.mobileprogramming.finsheet.data.local.entity.BudgetMutationEntity
import com.mobileprogramming.finsheet.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow

class BudgetRepositoryImpl(
    private val budgetDao: BudgetDao,
    private val budgetMutationDao: BudgetMutationDao
) : BudgetRepository {
    override fun getAllActiveBudgets(): Flow<List<BudgetEntity>> =
        budgetDao.getAllActiveBudgets()

    override suspend fun insertBudget(budget: BudgetEntity) {
        budgetDao.insertBudget(budget)
    }

    override suspend fun updateBudget(budget: BudgetEntity) {
        budgetDao.updateBudget(budget)
    }

    override suspend fun deleteBudget(id: String) {
        budgetDao.softDeleteBudget(id, System.currentTimeMillis())
    }

    override suspend fun insertBudgetMutation(mutation: BudgetMutationEntity) {
        budgetMutationDao.insertMutation(mutation)
    }

    override fun getAllBudgetMutationsFlow(): Flow<List<BudgetMutationEntity>> {
        return budgetMutationDao.getAllMutationsFlow()
    }

    override suspend fun getAllBudgetMutations(): List<BudgetMutationEntity> {
        return budgetMutationDao.getAllMutations()
    }
}