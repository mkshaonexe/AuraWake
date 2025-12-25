package com.aura.wake.data.repository

import com.aura.wake.data.local.WakeHistoryDao
import com.aura.wake.data.model.AlarmWakeHistory
import com.aura.wake.data.model.WakeStatus
import java.util.Calendar

class WakeHistoryRepository(private val wakeHistoryDao: WakeHistoryDao) {
    
    /**
     * Record a successful wake-up event
     */
    suspend fun recordWakeUp(alarmHour: Int, alarmMinute: Int) {
        val now = System.currentTimeMillis()
        val dateAtMidnight = getDateAtMidnight(now)
        
        val wakeHistory = AlarmWakeHistory(
            date = dateAtMidnight,
            alarmHour = alarmHour,
            alarmMinute = alarmMinute,
            wakeUpTime = now,
            status = WakeStatus.COMPLETED
        )
        
        wakeHistoryDao.insert(wakeHistory)
    }
    
    /**
     * Get wake history for a specific date range
     */
    suspend fun getHistoryForDateRange(startDate: Long, endDate: Long): List<AlarmWakeHistory> {
        return wakeHistoryDao.getHistoryForDateRange(startDate, endDate)
    }
    
    /**
     * Get wake history for the last N weeks
     */
    suspend fun getHistoryForWeeks(numWeeks: Int): List<AlarmWakeHistory> {
        val now = System.currentTimeMillis()
        val startDate = getDateAtMidnight(now - (numWeeks * 7 * 24 * 60 * 60 * 1000L))
        return wakeHistoryDao.getHistoryForDateRange(startDate, now)
    }
    
    /**
     * Calculate intensity level based on wake-up hour
     * 4-6 AM: level 4 (dense green)
     * 7 AM: level 2 (light green)
     * 8 AM: level 2 (light green)
     * 8+ AM: level 1 (lightest green)
     */
    fun calculateIntensityLevel(hour: Int): Int {
        return when {
            hour in 4..6 -> 4
            hour == 7 -> 2
            hour == 8 -> 2
            hour > 8 -> 1
            else -> 1 // Very early morning (before 4 AM) - treat as regular
        }
    }
    
    /**
     * Get date normalized to midnight
     */
    private fun getDateAtMidnight(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
