package com.aura.wake.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.aura.wake.data.model.AlarmWakeHistory

@Dao
interface WakeHistoryDao {
    @Insert
    suspend fun insert(wakeHistory: AlarmWakeHistory)
    
    @Query("SELECT * FROM wake_history WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    suspend fun getHistoryForDateRange(startDate: Long, endDate: Long): List<AlarmWakeHistory>
    
    @Query("SELECT * FROM wake_history WHERE date >= :monthStart AND date < :monthEnd ORDER BY date ASC")
    suspend fun getHistoryForMonth(monthStart: Long, monthEnd: Long): List<AlarmWakeHistory>
    
    @Query("SELECT * FROM wake_history ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentHistory(limit: Int): List<AlarmWakeHistory>
}
