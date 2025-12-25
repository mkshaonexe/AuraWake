package com.aura.wake.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class WakeStatus {
    COMPLETED, // User successfully completed the alarm challenge
    MISSED,    // Alarm was not addressed (phone off, etc.)
    SKIPPED    // User snoozed or skipped the alarm
}

@Entity(tableName = "wake_history")
data class AlarmWakeHistory(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: Long, // Timestamp of the date (midnight)
    val alarmHour: Int, // Scheduled alarm hour
    val alarmMinute: Int, // Scheduled alarm minute
    val wakeUpTime: Long, // Actual timestamp when user completed the alarm
    val status: WakeStatus // COMPLETED, MISSED, SKIPPED
)
