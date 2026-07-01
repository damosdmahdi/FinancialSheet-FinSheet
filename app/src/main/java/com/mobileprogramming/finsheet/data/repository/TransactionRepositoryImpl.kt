package com.mobileprogramming.finsheet.data.repository

import com.mobileprogramming.finsheet.data.local.dao.TransactionDao
import com.mobileprogramming.finsheet.data.local.entity.TransactionEntity
import com.mobileprogramming.finsheet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

// Kelas ini mengambil DAO sebagai dependensi
class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getAllActiveTransactions(): Flow<List<TransactionEntity>> =
        transactionDao.getAllActiveTransactions()

    override fun getTotalAmountByTypeAndDate(type: String, startDate: Long, endDate: Long): Flow<Int?> =
        transactionDao.getTotalAmountByTypeAndDate(type, startDate, endDate)
}