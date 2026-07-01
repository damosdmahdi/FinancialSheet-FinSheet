package com.mobileprogramming.finsheet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "category_name")
    val categoryName: String,
    
    val type: String, // Misal: "INCOME" atau "EXPENSE"
    val icon: String? = null,
    val color: String? = null,
    
    @ColumnInfo(name = "sync_status")
    val syncStatus: String = "PENDING", // PENDING, SYNCED
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null
)