package com.mobileprogramming.finsheet.domain.usecase.transaction

import com.mobileprogramming.finsheet.domain.repository.TransactionRepository
class DeleteTransactionUseCase(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(id: String) {
        repository.deleteTransaction(id)
    }
}
