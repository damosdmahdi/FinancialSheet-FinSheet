package com.mobileprogramming.finsheet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mobileprogramming.finsheet.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    // Mengambil data user yang sedang aktif (Asumsi hanya ada 1 user di lokal)
    @Query("SELECT * FROM users LIMIT 1")
    fun getActiveUser(): Flow<UserEntity?>

    // Menghapus data lokal (Berguna saat user Logout)
    @Query("DELETE FROM users")
    suspend fun clearUserData()
}