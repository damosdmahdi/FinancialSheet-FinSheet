package com.mobileprogramming.finsheet.domain.usecase

import com.mobileprogramming.finsheet.data.local.entity.TransactionEntity
import com.mobileprogramming.finsheet.domain.repository.TransactionRepository
import java.util.UUID

class AddTransactionUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        categoryId: String?,
        amount: Int,
        transactionType: String,
        notes: String?,
        transactionDate: Long,
        receiptLocalPath: String?
    ) {
        val transaction = TransactionEntity(
            id = UUID.randomUUID().toString(),
            categoryId = categoryId,
            amount = amount,
            transactionType = transactionType,
            notes = notes,
            transactionDate = transactionDate,
            receiptLocalPath = receiptLocalPath
        )
        transactionRepository.insertTransaction(transaction)
    }
}
