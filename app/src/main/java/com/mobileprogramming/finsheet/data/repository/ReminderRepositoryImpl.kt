package com.mobileprogramming.finsheet.data.repository

import com.mobileprogramming.finsheet.data.local.dao.ReminderDao
import com.mobileprogramming.finsheet.data.local.entity.ReminderEntity
import com.mobileprogramming.finsheet.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow

class ReminderRepositoryImpl(
    private val reminderDao: ReminderDao
) : ReminderRepository {
    override fun getAllRemindersFlow(): Flow<List<ReminderEntity>> = reminderDao.getAllRemindersFlow()
    override suspend fun getActiveReminders(): List<ReminderEntity> = reminderDao.getActiveReminders()
    override suspend fun getReminderById(id: String): ReminderEntity? = reminderDao.getReminderById(id)
    override suspend fun insertReminder(reminder: ReminderEntity) = reminderDao.insertReminder(reminder)
    override suspend fun updateReminder(reminder: ReminderEntity) = reminderDao.updateReminder(reminder)
    override suspend fun deleteReminderById(id: String) = reminderDao.deleteReminderById(id)
}
