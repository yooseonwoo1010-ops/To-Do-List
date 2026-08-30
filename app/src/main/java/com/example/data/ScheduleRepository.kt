package com.example.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ScheduleRepository(private val scheduleDao: ScheduleDao) {

    fun getSchedulesForDate(date: String): Flow<List<ScheduleItem>> {
        return scheduleDao.getSchedulesByDate(date)
    }

    fun getAllSchedules(): Flow<List<ScheduleItem>> {
        return scheduleDao.getAllSchedules()
    }

    suspend fun insert(item: ScheduleItem): Long {
        return scheduleDao.insertSchedule(item)
    }

    suspend fun update(item: ScheduleItem) {
        scheduleDao.updateSchedule(item)
    }

    suspend fun delete(item: ScheduleItem) {
        scheduleDao.deleteSchedule(item)
    }

    suspend fun deleteById(id: Long) {
        scheduleDao.deleteScheduleById(id)
    }

    suspend fun toggleCompleted(item: ScheduleItem) {
        scheduleDao.updateCompletionStatus(item.id, !item.isCompleted)
    }

    suspend fun prepopulateIfEmpty(date: String) {
        val count = scheduleDao.getScheduleCountForDate(date)
        if (count == 0) {
            val sampleItems = listOf(
                ScheduleItem(
                    title = "수면 및 휴식 🌙",
                    startMinutes = 0, // 00:00
                    endMinutes = 420, // 07:00
                    isAllDay = false,
                    isCompleted = true,
                    category = "수면",
                    colorHex = "#6366F1",
                    note = "충분한 숙면 취하기",
                    date = date,
                    priority = 0
                ),
                ScheduleItem(
                    title = "기상 & 아침 스트레칭 ☀️",
                    startMinutes = 420, // 07:00
                    endMinutes = 480, // 08:00
                    isAllDay = false,
                    isCompleted = true,
                    category = "루틴",
                    colorHex = "#F59E0B",
                    note = "물 한잔 마시고 가벼운 몸풀기",
                    date = date,
                    priority = 1
                ),
                ScheduleItem(
                    title = "아침 식사 & 하루 계획 🍳",
                    startMinutes = 480, // 08:00
                    endMinutes = 540, // 09:00
                    isAllDay = false,
                    isCompleted = true,
                    category = "식사",
                    colorHex = "#10B981",
                    note = "건강한 아침 식단 챙기기",
                    date = date,
                    priority = 0
                ),
                ScheduleItem(
                    title = "집중 코딩 & 프로젝트 💻",
                    startMinutes = 540, // 09:00
                    endMinutes = 720, // 12:00
                    isAllDay = false,
                    isCompleted = false,
                    category = "공부/업무",
                    colorHex = "#3B82F6",
                    note = "안드로이드 앱 기능 완성하기",
                    date = date,
                    priority = 2
                ),
                ScheduleItem(
                    title = "점심 식사 & 산책 🥗",
                    startMinutes = 720, // 12:00
                    endMinutes = 810, // 13:30
                    isAllDay = false,
                    isCompleted = false,
                    category = "식사",
                    colorHex = "#14B8A6",
                    note = "햇볕 쬐며 가벼운 산책",
                    date = date,
                    priority = 0
                ),
                ScheduleItem(
                    title = "공부 & 자료 조사 📚",
                    startMinutes = 810, // 13:30
                    endMinutes = 1080, // 18:00
                    isAllDay = false,
                    isCompleted = false,
                    category = "공부/업무",
                    colorHex = "#8B5CF6",
                    note = "핵심 개념 정리 및 문제 풀이",
                    date = date,
                    priority = 1
                ),
                ScheduleItem(
                    title = "저녁 식사 🍱",
                    startMinutes = 1080, // 18:00
                    endMinutes = 1170, // 19:30
                    isAllDay = false,
                    isCompleted = false,
                    category = "식사",
                    colorHex = "#F97316",
                    note = "맛있는 저녁 식사",
                    date = date,
                    priority = 0
                ),
                ScheduleItem(
                    title = "러닝 / 헬스 운동 🏃‍♂️",
                    startMinutes = 1170, // 19:30
                    endMinutes = 1260, // 21:00
                    isAllDay = false,
                    isCompleted = false,
                    category = "운동",
                    colorHex = "#EF4444",
                    note = "유산소 30분 + 근력 운동",
                    date = date,
                    priority = 1
                ),
                ScheduleItem(
                    title = "독서 및 하루 회고 📖",
                    startMinutes = 1260, // 21:00
                    endMinutes = 1380, // 23:00
                    isAllDay = false,
                    isCompleted = false,
                    category = "여가",
                    colorHex = "#EC4899",
                    note = "독서 30쪽 & 다이어리 작성",
                    date = date,
                    priority = 0
                ),
                ScheduleItem(
                    title = "취침 준비 🌙",
                    startMinutes = 1380, // 23:00
                    endMinutes = 1440, // 24:00
                    isAllDay = false,
                    isCompleted = false,
                    category = "수면",
                    colorHex = "#6366F1",
                    note = "스마트폰 멀리 두고 명상",
                    date = date,
                    priority = 0
                ),
                // Non-timed To-dos
                ScheduleItem(
                    title = "영양제 챙겨먹기 💊",
                    isAllDay = true,
                    isCompleted = true,
                    category = "건강",
                    colorHex = "#06B6D4",
                    note = "비타민C, 오메가3 복용",
                    date = date,
                    priority = 1
                ),
                ScheduleItem(
                    title = "방 청소 및 환기하기 🧹",
                    isAllDay = true,
                    isCompleted = false,
                    category = "루틴",
                    colorHex = "#A855F7",
                    note = "책상 정리 및 환기 15분",
                    date = date,
                    priority = 0
                )
            )
            scheduleDao.insertSchedules(sampleItems)
        }
    }
}
