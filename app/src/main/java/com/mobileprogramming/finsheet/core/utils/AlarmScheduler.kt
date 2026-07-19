package com.mobileprogramming.finsheet.core.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mobileprogramming.finsheet.data.local.entity.ReminderEntity
import java.util.Calendar

object AlarmScheduler {
    fun scheduleAlarm(context: Context, reminder: ReminderEntity) {
        if (!reminder.isActive) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Calculate the first future trigger time
        val triggerTime = calculateFirstFutureTriggerTime(
            startDateMillis = reminder.startDate,
            hour = reminder.timeHour,
            minute = reminder.timeMinute,
            frequency = reminder.frequency
        )

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            flags
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    fun cancelAlarm(context: Context, reminderId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            flags
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun calculateFirstFutureTriggerTime(
        startDateMillis: Long,
        hour: Int,
        minute: Int,
        frequency: String
    ): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = startDateMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val now = System.currentTimeMillis()
        if (calendar.timeInMillis >= now) {
            return calendar.timeInMillis
        }
        if (frequency == "Sekali") {
            // Tomorrow
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            return calendar.timeInMillis
        }
        while (calendar.timeInMillis < now) {
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
        }
        return calendar.timeInMillis
    }
}
