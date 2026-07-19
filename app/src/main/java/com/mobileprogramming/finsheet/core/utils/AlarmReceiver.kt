package com.mobileprogramming.finsheet.core.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mobileprogramming.finsheet.data.local.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getStringExtra("reminder_id") ?: return
        
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val db = AppDatabase.getDatabase(context, scope)
            val reminderDao = db.reminderDao()
            val reminder = reminderDao.getReminderById(reminderId)
            
            if (reminder != null && reminder.isActive) {
                // Show notification
                NotificationHelper.showReminderNotification(
                    context = context,
                    title = reminder.name,
                    message = reminder.comment.takeIf { it.isNotBlank() } ?: "Waktunya mencatat pengeluaran harian Anda!",
                    notificationId = reminder.id.hashCode()
                )
                
                // Calculate and schedule next trigger if repeating
                if (reminder.frequency == "Sekali") {
                    reminderDao.updateReminder(reminder.copy(isActive = false, updatedAt = System.currentTimeMillis()))
                } else {
                    val nextTime = calculateNextTriggerTime(reminder.frequency, System.currentTimeMillis())
                    val updatedReminder = reminder.copy(
                        startDate = nextTime,
                        updatedAt = System.currentTimeMillis()
                    )
                    reminderDao.updateReminder(updatedReminder)
                    AlarmScheduler.scheduleAlarm(context, updatedReminder)
                }
            }
        }
    }

    private fun calculateNextTriggerTime(frequency: String, baseTimeMillis: Long): Long {
        val calendar = Calendar.getInstance().apply { timeInMillis = baseTimeMillis }
        when (frequency) {
            "Harian" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            "Mingguan" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            "2 Minggu Sekali" -> calendar.add(Calendar.WEEK_OF_YEAR, 2)
            "Setiap 4 Minggu" -> calendar.add(Calendar.WEEK_OF_YEAR, 4)
            "Bulanan" -> calendar.add(Calendar.MONTH, 1)
            "Setiap 2 Bulan" -> calendar.add(Calendar.MONTH, 2)
            "3 Bulan Sekali" -> calendar.add(Calendar.MONTH, 3)
            "Setiap 6 Bulan" -> calendar.add(Calendar.MONTH, 6)
            "Setiap Tahun" -> calendar.add(Calendar.YEAR, 1)
            else -> calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }
}
