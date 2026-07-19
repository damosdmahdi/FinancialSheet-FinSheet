package com.mobileprogramming.finsheet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mobileprogramming.finsheet.data.local.entity.BudgetMutationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetMutationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMutation(mutation: BudgetMutationEntity)

    @Query("SELECT * FROM budget_mutations ORDER BY created_at DESC")
    fun getAllMutationsFlow(): Flow<List<BudgetMutationEntity>>

    @Query("SELECT * FROM budget_mutations ORDER BY created_at DESC")
    suspend fun getAllMutations(): List<BudgetMutationEntity>
}
