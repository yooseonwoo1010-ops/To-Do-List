package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.ScheduleItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    fun scheduleReminder(context: Context, item: ScheduleItem) {
        if (!item.hasReminder || item.isAllDay || item.isCompleted) {
            cancelReminder(context, item.id)
            return
        }

        try {
            val date = try {
                LocalDate.parse(item.date, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                LocalDate.now()
            }

            // Calculate trigger time: startMinutes minus reminderMinutesBefore (e.g. 15 mins)
            val totalStartMins = item.startMinutes
            val reminderOffset = item.reminderMinutesBefore

            var targetDate = date
            var targetMinute = totalStartMins - reminderOffset

            if (targetMinute < 0) {
                // falls into previous day
                targetDate = date.minusDays(1)
                targetMinute += 1440
            }

            val targetHour = (targetMinute / 60) % 24
            val targetMin = targetMinute % 60

            val triggerDateTime = LocalDateTime.of(targetDate, LocalTime.of(targetHour, targetMin, 0))
            val triggerEpochMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val currentMillis = System.currentTimeMillis()

            if (triggerEpochMillis <= currentMillis) {
                Log.d(TAG, "Trigger time is already in the past for item: ${item.title}")
                return
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ScheduleNotificationReceiver::class.java).apply {
                putExtra("schedule_id", item.id)
                putExtra("title", item.title)
                putExtra("category", item.category)
                putExtra("time_span", item.formattedTimeSpan())
                putExtra("minutes_before", item.reminderMinutesBefore)
                putExtra("note", item.note)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                item.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerEpochMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerEpochMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMillis,
                    pendingIntent
                )
            }

            Log.d(TAG, "Successfully scheduled alarm for '${item.title}' at $triggerDateTime (${item.reminderMinutesBefore}m before)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule alarm for item: ${item.id}", e)
        }
    }

    fun cancelReminder(context: Context, scheduleId: Long) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ScheduleNotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                scheduleId.toInt(),
                intent,
                PendingIntent.FLAG_NO_CREATE or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
            )

            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d(TAG, "Cancelled alarm for schedule ID: $scheduleId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel alarm for schedule ID: $scheduleId", e)
        }
    }
}
