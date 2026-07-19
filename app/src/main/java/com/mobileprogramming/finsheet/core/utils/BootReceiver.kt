package com.mobileprogramming.finsheet.core.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mobileprogramming.finsheet.data.local.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val db = AppDatabase.getDatabase(context, scope)
                val activeReminders = db.reminderDao().getActiveReminders()
                activeReminders.forEach { reminder ->
                    AlarmScheduler.scheduleAlarm(context, reminder)
                }
            }
        }
    }
}
