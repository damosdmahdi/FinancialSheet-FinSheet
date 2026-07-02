package com.mobileprogramming.finsheet.domain.usecase

import com.mobileprogramming.finsheet.data.local.entity.TransactionEntity
import com.mobileprogramming.finsheet.domain.repository.TransactionRepository

class UpdateTransactionUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        existingTransaction: TransactionEntity,
        categoryId: String?,
        amount: Int,
        transactionType: String,
        notes: String?,
        transactionDate: Long,
        receiptLocalPath: String?
    ) {
        val updatedTransaction = existingTransaction.copy(
            categoryId = categoryId,
            amount = amount,
            transactionType = transactionType,
            notes = notes,
            transactionDate = transactionDate,
            receiptLocalPath = receiptLocalPath,
            updatedAt = System.currentTimeMillis()
        )
        transactionRepository.updateTransaction(updatedTransaction)
    }
}
