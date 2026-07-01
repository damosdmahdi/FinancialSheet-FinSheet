package com.mobileprogramming.finsheet.domain.repository

import com.mobileprogramming.finsheet.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    fun getAllActiveTransactions(): Flow<List<TransactionEntity>>

    fun getTotalAmountByTypeAndDate(type: String, startDate: Long, endDate: Long): Flow<Int?>

}