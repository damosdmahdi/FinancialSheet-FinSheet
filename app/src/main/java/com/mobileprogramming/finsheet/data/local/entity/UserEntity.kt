package com.mobileprogramming.finsheet.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "display_name")
    val displayName: String,
    
    val email: String,
    
    @ColumnInfo(name = "google_spreadsheet_id")
    val googleSpreadsheetId: String?,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long, // Menggunakan Unix Timestamp (milliseconds)
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long?
)