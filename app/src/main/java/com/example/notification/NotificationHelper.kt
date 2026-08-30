package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_ID = "schedule_reminder_channel"
    private const val CHANNEL_NAME = "일정 시작 알림"
    private const val CHANNEL_DESCRIPTION = "일정 시작 15분 전 미리 알림을 전달합니다."

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showScheduleNotification(
        context: Context,
        scheduleId: Long,
        title: String,
        category: String,
        timeSpan: String,
        minutesBefore: Int,
        note: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent to open app on click
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("schedule_id", scheduleId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            scheduleId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val advanceText = if (minutesBefore > 0) "${minutesBefore}분 후 시작" else "지금 시작"
        val subtitle = if (note.isNotBlank()) "[$category] $timeSpan\n📝 $note" else "[$category] $timeSpan 예정"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_alarm)
            .setContentTitle("⏰ $advanceText: $title")
            .setContentText("[$category] $timeSpan")
            .setStyle(NotificationCompat.BigTextStyle().bigText(subtitle))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setColor(0xFF3B82F6.toInt())
            .build()

        notificationManager.notify(scheduleId.toInt(), notification)
    }
}
