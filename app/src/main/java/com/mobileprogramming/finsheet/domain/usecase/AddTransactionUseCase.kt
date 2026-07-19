package com.mobileprogramming.finsheet.domain.usecase

import com.mobileprogramming.finsheet.data.local.entity.TransactionEntity
import com.mobileprogramming.finsheet.domain.repository.TransactionRepository
import java.util.UUID

class AddTransactionUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        categoryId: String?,
        amount: Double,
        transactionType: String,
        notes: String?,
        transactionDate: Long,
        receiptLocalPath: String?,
        accountId: String? = null,
        isDitalangin: Boolean = false
    ) {
        val transaction = TransactionEntity(
            id = UUID.randomUUID().toString(),
            categoryId = categoryId,
            amount = amount,
            accountId = accountId,
            transactionType = transactionType,
            notes = notes,
            transactionDate = transactionDate,
            receiptLocalPath = receiptLocalPath,
            isDitalangin = isDitalangin
        )
        transactionRepository.insertTransaction(transaction)
    }
}
