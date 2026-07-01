package com.mobileprogramming.finsheet.domain.model

data class TransactionItemModel(
    val id: String,
    val title: String,
    val timeMillis: Long,
    val categoryName: String,
    val iconName: String?,
    val colorHex: String?,
    val amount: Int,
    val isExpense: Boolean,
    val transactionDate: Long
)
