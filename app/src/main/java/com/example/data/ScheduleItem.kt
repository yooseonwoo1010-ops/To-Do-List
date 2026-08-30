package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Entity(tableName = "schedules")
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val startMinutes: Int = 0, // 0..1439 (minutes from midnight 00:00)
    val endMinutes: Int = 60,   // 0..1439
    val isAllDay: Boolean = false, // If true, treated as non-timed to-do
    val isCompleted: Boolean = false,
    val category: String = "일반",
    val colorHex: String = "#4F46E5",
    val note: String = "",
    val date: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE), // YYYY-MM-DD
    val priority: Int = 0, // 0: 일반, 1: 중요, 2: 매우 중요
    val hasReminder: Boolean = true, // 15분 전 알림 활성화 여부
    val reminderMinutesBefore: Int = 15 // 시작 N분 전 알림 (기본 15분)
) {
    // Helper to format start time as HH:mm
    fun formattedStartTime(): String {
        val hour = (startMinutes / 60) % 24
        val min = startMinutes % 60
        return String.format("%02d:%02d", hour, min)
    }

    // Helper to format end time as HH:mm
    fun formattedEndTime(): String {
        val hour = (endMinutes / 60) % 24
        val min = endMinutes % 60
        return String.format("%02d:%02d", hour, min)
    }

    fun formattedTimeSpan(): String {
        if (isAllDay) return "종일 할 일"
        return "${formattedStartTime()} ~ ${formattedEndTime()}"
    }

    // Duration in minutes
    fun durationMinutes(): Int {
        return if (endMinutes >= startMinutes) {
            endMinutes - startMinutes
        } else {
            // crosses midnight
            (1440 - startMinutes) + endMinutes
        }
    }

    fun formattedReminderText(): String {
        if (!hasReminder || isAllDay) return ""
        return if (reminderMinutesBefore == 0) "시작 정각 알림" else "${reminderMinutesBefore}분 전 알림"
    }
}

