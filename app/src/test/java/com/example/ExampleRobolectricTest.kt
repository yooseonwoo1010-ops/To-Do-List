package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ScheduleItem
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("원형 시간표 투두", appName)
    }

    @Test
    fun `schedule item has 15-minute reminder by default`() {
        val item = ScheduleItem(
            title = "팀 회의",
            startMinutes = 600, // 10:00
            endMinutes = 660    // 11:00
        )
        assertTrue(item.hasReminder)
        assertEquals(15, item.reminderMinutesBefore)
        assertEquals("15분 전 알림", item.formattedReminderText())
        assertEquals("10:00 ~ 11:00", item.formattedTimeSpan())
    }

    @Test
    fun `notification channel creation and alarm scheduling work cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        NotificationHelper.createNotificationChannel(context)

        val item = ScheduleItem(
            id = 101L,
            title = "알고리즘 코딩 테스트 스터디",
            startMinutes = 840, // 14:00
            endMinutes = 960,   // 16:00
            hasReminder = true,
            reminderMinutesBefore = 15
        )
        AlarmScheduler.scheduleReminder(context, item)
        AlarmScheduler.cancelReminder(context, item.id)
    }
}

