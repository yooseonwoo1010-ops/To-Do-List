package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules WHERE date = :date ORDER BY isAllDay ASC, startMinutes ASC, id ASC")
    fun getSchedulesByDate(date: String): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedules ORDER BY date DESC, startMinutes ASC")
    fun getAllSchedules(): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: Long): ScheduleItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(item: ScheduleItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(items: List<ScheduleItem>)

    @Update
    suspend fun updateSchedule(item: ScheduleItem)

    @Delete
    suspend fun deleteSchedule(item: ScheduleItem)

    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteScheduleById(id: Long)

    @Query("UPDATE schedules SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean)

    @Query("SELECT COUNT(*) FROM schedules WHERE date = :date")
    suspend fun getScheduleCountForDate(date: String): Int
}
