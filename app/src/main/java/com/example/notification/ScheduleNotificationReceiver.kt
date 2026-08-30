package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScheduleNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra("schedule_id", 0L)
        val title = intent.getStringExtra("title") ?: "일정 알림"
        val category = intent.getStringExtra("category") ?: "일정"
        val timeSpan = intent.getStringExtra("time_span") ?: ""
        val minutesBefore = intent.getIntExtra("minutes_before", 15)
        val note = intent.getStringExtra("note") ?: ""

        NotificationHelper.showScheduleNotification(
            context = context,
            scheduleId = scheduleId,
            title = title,
            category = category,
            timeSpan = timeSpan,
            minutesBefore = minutesBefore,
            note = note
        )
    }
}
