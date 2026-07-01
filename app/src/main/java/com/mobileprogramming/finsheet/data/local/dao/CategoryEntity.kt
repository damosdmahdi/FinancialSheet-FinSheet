package com.mobileprogramming.finsheet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mobileprogramming.finsheet.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    // Insert banyak kategori sekaligus (Berguna untuk seeder/default data)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    // Soft Delete: Set deleted_at dan ubah status agar disinkronisasikan ke cloud
    @Query("UPDATE categories SET deleted_at = :timestamp, sync_status = 'PENDING' WHERE id = :id")
    suspend fun softDeleteCategory(id: String, timestamp: Long)

    // Ambil semua kategori yang aktif (untuk ditampilkan di Spinner/Dropdown UI)
    @Query("SELECT * FROM categories WHERE deleted_at IS NULL ORDER BY category_name ASC")
    fun getAllActiveCategories(): Flow<List<CategoryEntity>>

    // Ambil kategori berdasarkan tipe (misal: hanya ambil "INCOME" atau "EXPENSE")
    @Query("SELECT * FROM categories WHERE type = :type AND deleted_at IS NULL ORDER BY category_name ASC")
    fun getActiveCategoriesByType(type: String): Flow<List<CategoryEntity>>

    // --- BAGIAN SINKRONISASI CLOUD ---
    
    @Query("SELECT * FROM categories WHERE sync_status = 'PENDING'")
    suspend fun getPendingSyncCategories(): List<CategoryEntity>

    @Query("UPDATE categories SET sync_status = 'SYNCED' WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}