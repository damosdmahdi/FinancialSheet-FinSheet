package com.mobileprogramming.finsheet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_mutations")
data class BudgetMutationEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "from_category_id")
    val fromCategoryId: String,
    
    @ColumnInfo(name = "from_category_name")
    val fromCategoryName: String,
    
    @ColumnInfo(name = "to_category_id")
    val toCategoryId: String,
    
    @ColumnInfo(name = "to_category_name")
    val toCategoryName: String,
    
    val amount: Double,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
