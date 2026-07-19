package com.mobileprogramming.finsheet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "category_id")
    val categoryId: String? = null,
    
    val amount: Double,
    
    @ColumnInfo(name = "transaction_type")
    val transactionType: String,
    
    val notes: String? = null,
    
    @ColumnInfo(name = "transaction_date")
    val transactionDate: Long,
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "PENDING",
    
    @ColumnInfo(name = "receipt_local_path")
    val receiptLocalPath: String? = null,
    
    @ColumnInfo(name = "receipt_cloud_url")
    val receiptCloudUrl: String? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null
)