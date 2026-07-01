package com.mobileprogramming.finsheet.domain.repository

import com.mobileprogramming.finsheet.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    fun getAllActiveTransactions(): Flow<List<TransactionEntity>>

    fun getTotalAmountByTypeAndDate(type: String, startDate: Long, endDate: Long): Flow<Int?>

    suspend fun getTransactionById(id: String): TransactionEntity?

    suspend fun insertTransaction(transaction: TransactionEntity)

    suspend fun updateTransaction(transaction: TransactionEntity)
}