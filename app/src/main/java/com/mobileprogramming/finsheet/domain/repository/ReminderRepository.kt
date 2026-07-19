package com.mobileprogramming.finsheet.domain.repository

import com.mobileprogramming.finsheet.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getAllRemindersFlow(): Flow<List<ReminderEntity>>
    suspend fun getActiveReminders(): List<ReminderEntity>
    suspend fun getReminderById(id: String): ReminderEntity?
    suspend fun insertReminder(reminder: ReminderEntity)
    suspend fun updateReminder(reminder: ReminderEntity)
    suspend fun deleteReminderById(id: String)
}
