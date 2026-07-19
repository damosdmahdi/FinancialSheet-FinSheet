package com.mobileprogramming.finsheet.domain.model

data class TransactionItemModel(
    val id: String,
    val title: String,
    val timeMillis: Long,
    val createdAt: Long,
    val categoryName: String,
    val iconName: String?,
    val colorHex: String?,
    val amount: Double,
    val isExpense: Boolean,
    val transactionDate: Long,
    val receiptLocalPath: String? = null,
    val transactionType: String = "EXPENSE",
    val status: String? = null,
    val isDitalangin: Boolean = false
)
