package com.mobileprogramming.finsheet.domain.usecase.transaction

import com.mobileprogramming.finsheet.domain.model.TransactionItemModel
import com.mobileprogramming.finsheet.domain.repository.CategoryRepository
import com.mobileprogramming.finsheet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetAllTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {
    operator fun invoke(): Flow<List<TransactionItemModel>> {
        return combine(
            transactionRepository.getAllActiveTransactions(),
            categoryRepository.getAllActiveCategories()
        ) { transactions, categories ->
            transactions.map { tx ->
                val category = categories.find { it.id == tx.categoryId }
                TransactionItemModel(
                    id = tx.id,
                    title = tx.notes?.takeIf { it.isNotBlank() } ?: category?.categoryName ?: "Transaksi",
                    timeMillis = tx.transactionDate,
                    createdAt = tx.createdAt,
                    categoryName = category?.categoryName ?: "Lainnya",
                    iconName = category?.icon,
                    colorHex = category?.color,
                    amount = tx.amount,
                    isExpense = tx.transactionType == "EXPENSE",
                    transactionDate = tx.transactionDate,
                    receiptLocalPath = tx.receiptLocalPath
                )
            }.sortedByDescending { it.transactionDate }
        }
    }
}
