package com.mobileprogramming.finsheet.core.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {
    private const val CHANNEL_ID = "budget_warnings"
    private const val CHANNEL_NAME = "Peringatan Anggaran"

    fun showBudgetNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi ketika batas anggaran terlewati"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val largeIconBitmap = try {
            android.graphics.BitmapFactory.decodeResource(
                context.resources,
                com.mobileprogramming.finsheet.R.drawable.logo_finsheet
            )
        } catch (e: Exception) {
            null
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.mobileprogramming.finsheet.R.drawable.ic_notification)
            .apply {
                if (largeIconBitmap != null) {
                    setLargeIcon(largeIconBitmap)
                }
            }
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())
    }
}
