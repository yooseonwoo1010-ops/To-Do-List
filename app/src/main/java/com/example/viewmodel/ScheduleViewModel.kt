package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ScheduleItem
import com.example.data.ScheduleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class ViewMode {
    CIRCULAR, // 원형 시간표
    LIST,     // 나열식 리스트
    STATS     // 하루 통계 및 요약
}

enum class FilterStatus {
    ALL,
    ACTIVE,
    COMPLETED
}

data class DailyStats(
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val timedCount: Int = 0,
    val timedCompletedCount: Int = 0,
    val todoCount: Int = 0,
    val todoCompletedCount: Int = 0,
    val completionPercentage: Int = 0,
    val totalScheduledMinutes: Int = 0,
    val completedScheduledMinutes: Int = 0
)

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ScheduleRepository

    init {
        val dao = AppDatabase.getDatabase(application).scheduleDao()
        repository = ScheduleRepository(dao)
    }

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.CIRCULAR)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _filterStatus = MutableStateFlow(FilterStatus.ALL)
    val filterStatus: StateFlow<FilterStatus> = _filterStatus.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedItem = MutableStateFlow<ScheduleItem?>(null)
    val selectedItem: StateFlow<ScheduleItem?> = _selectedItem.asStateFlow()

    private val _currentMinute = MutableStateFlow(getCurrentMinuteOfDay())
    val currentMinute: StateFlow<Int> = _currentMinute.asStateFlow()

    // Real-time ticker
    init {
        viewModelScope.launch {
            // Prepopulate for today if empty
            val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            repository.prepopulateIfEmpty(todayStr)

            // Tick minute every 10 seconds to keep live needle updated
            while (true) {
                _currentMinute.value = getCurrentMinuteOfDay()
                delay(10000)
            }
        }
    }

    // Schedules for selected date
    val schedulesForSelectedDate: StateFlow<List<ScheduleItem>> = _selectedDate
        .flatMapLatest { date ->
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            viewModelScope.launch {
                repository.prepopulateIfEmpty(dateStr)
            }
            repository.getSchedulesForDate(dateStr)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered items for list view
    val filteredItems: StateFlow<List<ScheduleItem>> = combine(
        schedulesForSelectedDate,
        _filterStatus,
        _selectedCategory
    ) { items, status, category ->
        items.filter { item ->
            val statusMatch = when (status) {
                FilterStatus.ALL -> true
                FilterStatus.ACTIVE -> !item.isCompleted
                FilterStatus.COMPLETED -> item.isCompleted
            }
            val categoryMatch = category == null || item.category == category
            statusMatch && categoryMatch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Daily Stats
    val dailyStats: StateFlow<DailyStats> = schedulesForSelectedDate.combine(_currentMinute) { items, _ ->
        calculateStats(items)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DailyStats()
    )

    private fun calculateStats(items: List<ScheduleItem>): DailyStats {
        if (items.isEmpty()) return DailyStats()
        val total = items.size
        val completed = items.count { it.isCompleted }
        val timed = items.filter { !it.isAllDay }
        val timedCompleted = timed.count { it.isCompleted }
        val todos = items.filter { it.isAllDay }
        val todoCompleted = todos.count { it.isCompleted }
        val percentage = if (total > 0) ((completed.toFloat() / total) * 100).toInt() else 0

        var totalMins = 0
        var completedMins = 0
        timed.forEach {
            val dur = it.durationMinutes()
            totalMins += dur
            if (it.isCompleted) completedMins += dur
        }

        return DailyStats(
            totalCount = total,
            completedCount = completed,
            timedCount = timed.size,
            timedCompletedCount = timedCompleted,
            todoCount = todos.size,
            todoCompletedCount = todoCompleted,
            completionPercentage = percentage,
            totalScheduledMinutes = totalMins,
            completedScheduledMinutes = completedMins
        )
    }

    private fun getCurrentMinuteOfDay(): Int {
        val now = LocalTime.now()
        return now.hour * 60 + now.minute
    }

    // Actions
    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun setFilterStatus(status: FilterStatus) {
        _filterStatus.value = status
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = category
    }

    fun selectItem(item: ScheduleItem?) {
        _selectedItem.value = item
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _selectedItem.value = null
    }

    fun goToPreviousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
        _selectedItem.value = null
    }

    fun goToNextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
        _selectedItem.value = null
    }

    fun goToToday() {
        _selectedDate.value = LocalDate.now()
        _selectedItem.value = null
    }

    fun toggleComplete(item: ScheduleItem) {
        viewModelScope.launch {
            val newCompleted = !item.isCompleted
            repository.toggleCompleted(item)
            if (_selectedItem.value?.id == item.id) {
                _selectedItem.value = item.copy(isCompleted = newCompleted)
            }
            if (newCompleted) {
                com.example.notification.AlarmScheduler.cancelReminder(getApplication(), item.id)
            } else {
                if (item.hasReminder && !item.isAllDay) {
                    com.example.notification.AlarmScheduler.scheduleReminder(getApplication(), item.copy(isCompleted = false))
                }
            }
        }
    }

    fun saveSchedule(item: ScheduleItem) {
        viewModelScope.launch {
            val dateStr = _selectedDate.value.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val updated = item.copy(date = dateStr)
            val finalId = if (updated.id == 0L) {
                val newId = repository.insert(updated)
                updated.copy(id = newId)
            } else {
                repository.update(updated)
                updated
            }

            if (finalId.hasReminder && !finalId.isAllDay && !finalId.isCompleted) {
                com.example.notification.AlarmScheduler.scheduleReminder(getApplication(), finalId)
            } else {
                com.example.notification.AlarmScheduler.cancelReminder(getApplication(), finalId.id)
            }

            _selectedItem.value = null
        }
    }

    fun deleteSchedule(item: ScheduleItem) {
        viewModelScope.launch {
            com.example.notification.AlarmScheduler.cancelReminder(getApplication(), item.id)
            repository.delete(item)
            if (_selectedItem.value?.id == item.id) {
                _selectedItem.value = null
            }
        }
    }

    // Helper for formatted date
    fun getFormattedSelectedDate(): String {
        val date = _selectedDate.value
        val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
        return "${date.year}년 ${date.monthValue}월 ${date.dayOfMonth}일 (${dayOfWeek})"
    }

    fun isTodaySelected(): Boolean {
        return _selectedDate.value == LocalDate.now()
    }
}
