package com.mobileprogramming.finsheet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "from_account_id")
    val fromAccountId: String,
    
    @ColumnInfo(name = "to_account_id")
    val toAccountId: String,
    
    val amount: Double,
    val notes: String? = null,
    
    @ColumnInfo(name = "transfer_date")
    val transferDate: Long,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
