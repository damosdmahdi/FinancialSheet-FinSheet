package com.mobileprogramming.finsheet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mobileprogramming.finsheet.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET deleted_at = :timestamp, sync_status = 'PENDING' WHERE id = :id")
    suspend fun softDeleteTransaction(id: String, timestamp: Long)

    @Query("UPDATE transactions SET status = :status, account_id = :accountId, updated_at = :updatedAt, sync_status = 'PENDING' WHERE id = :id")
    suspend fun updateTransactionStatus(id: String, status: String?, accountId: String?, updatedAt: Long)

    // Tampilkan riwayat transaksi dari yang terbaru
    @Query("SELECT * FROM transactions WHERE deleted_at IS NULL ORDER BY transaction_date DESC")
    fun getAllActiveTransactions(): Flow<List<TransactionEntity>>

    // Ambil detail 1 transaksi berdasarkan ID yang dilempar dari Intent
    @Query("SELECT * FROM transactions WHERE id = :id AND deleted_at IS NULL LIMIT 1")
    suspend fun getTransactionById(id: String): TransactionEntity?

    // --- BAGIAN KALKULASI & REPORTING ---

    // Total pengeluaran/pemasukan dalam rentang waktu (misal: Bulan ini)
    @Query("SELECT SUM(amount) FROM transactions WHERE transaction_type = :type AND transaction_date BETWEEN :startDate AND :endDate AND deleted_at IS NULL")
    fun getTotalAmountByTypeAndDate(type: String, startDate: Long, endDate: Long): Flow<Int?>

    // Total pengeluaran berdasarkan kategori tertentu (Berguna untuk diadu dengan Budget)
    @Query("SELECT SUM(amount) FROM transactions WHERE category_id = :categoryId AND transaction_type = 'EXPENSE' AND transaction_date BETWEEN :startDate AND :endDate AND deleted_at IS NULL")
    fun getTotalExpenseByCategoryId(categoryId: String, startDate: Long, endDate: Long): Flow<Int?>

    // --- BAGIAN SINKRONISASI CLOUD ---

    @Query("SELECT * FROM transactions WHERE sync_status = 'PENDING'")
    suspend fun getPendingSyncTransactions(): List<TransactionEntity>

    @Query("UPDATE transactions SET sync_status = 'SYNCED' WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>)
}