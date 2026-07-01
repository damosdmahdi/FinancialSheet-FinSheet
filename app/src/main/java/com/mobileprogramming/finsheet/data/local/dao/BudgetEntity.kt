package com.mobileprogramming.finsheet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mobileprogramming.finsheet.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Query("UPDATE budgets SET deleted_at = :timestamp, sync_status = 'PENDING' WHERE id = :id")
    suspend fun softDeleteBudget(id: String, timestamp: Long)

    // Ambil semua budget yang aktif
    @Query("SELECT * FROM budgets WHERE deleted_at IS NULL")
    fun getAllActiveBudgets(): Flow<List<BudgetEntity>>

    // Ambil detail budget spesifik untuk suatu kategori
    @Query("SELECT * FROM budgets WHERE category_id = :categoryId AND deleted_at IS NULL LIMIT 1")
    fun getBudgetByCategoryId(categoryId: String): Flow<BudgetEntity?>

    // --- BAGIAN SINKRONISASI CLOUD ---
    
    @Query("SELECT * FROM budgets WHERE sync_status = 'PENDING'")
    suspend fun getPendingSyncBudgets(): List<BudgetEntity>

    @Query("UPDATE budgets SET sync_status = 'SYNCED' WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}