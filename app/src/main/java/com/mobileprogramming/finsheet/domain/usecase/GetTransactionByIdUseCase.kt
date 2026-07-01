package com.mobileprogramming.finsheet.domain.usecase

import com.mobileprogramming.finsheet.data.local.entity.TransactionEntity
import com.mobileprogramming.finsheet.domain.repository.TransactionRepository

class GetTransactionByIdUseCase(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(id: String): TransactionEntity? {
        return transactionRepository.getTransactionById(id)
    }
}
