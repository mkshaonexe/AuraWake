package com.aura.wake.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aura.wake.data.alarm.AlarmScheduler
import com.aura.wake.data.model.Alarm
import com.aura.wake.data.model.ChallengeType
import com.aura.wake.data.repository.AlarmRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class AlarmViewModel(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler,
    private val analyticsManager: com.aura.wake.data.analytics.AnalyticsManager
) : ViewModel() {

    val allAlarms: StateFlow<List<Alarm>> = repository.getAllAlarms()
        .map { alarms ->
            val now = java.util.Calendar.getInstance()
            val currentMinutes = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE)
            
            alarms.sortedWith(
                compareByDescending<Alarm> { it.isEnabled }
                    .thenBy { alarm ->
                        val alarmMinutes = alarm.hour * 60 + alarm.minute
                        // If alarm time is earlier than now, it's for tomorrow (+24h)
                        if (alarmMinutes < currentMinutes) alarmMinutes + 24 * 60 else alarmMinutes
                    }
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addAlarm(
        hour: Int, 
        minute: Int, 
        challengeType: ChallengeType, 
        label: String? = null,
        ringtoneUri: String? = null,
        ringtoneTitle: String? = null
    ) {
        viewModelScope.launch {
            val alarm = Alarm(
                id = UUID.randomUUID().toString(),
                hour = hour,
                minute = minute,
                isEnabled = true,
                challengeType = challengeType,
                label = if (label.isNullOrBlank()) null else label,
                ringtoneUri = ringtoneUri,
                ringtoneTitle = ringtoneTitle
            )
            repository.insertAlarm(alarm)
            scheduler.schedule(alarm) // Schedule the alarm with AlarmManager
            
            // Log Event
            analyticsManager.logAlarmCreated(challengeType.name)
        }
    }

    fun toggleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = !alarm.isEnabled)
            repository.updateAlarm(updated)
            if (updated.isEnabled) {
                scheduler.schedule(updated)
            } else {
                scheduler.cancel(updated)
            }
            // Log Event
            analyticsManager.logAlarmToggled(alarm.id, updated.isEnabled)
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            scheduler.cancel(alarm)
            repository.deleteAlarm(alarm)
            // Log Event
            analyticsManager.logAlarmDeleted(alarm.id)
        }
    }

    fun updateAlarmChallenge(alarm: Alarm, challengeType: ChallengeType) {
        viewModelScope.launch {
            val updated = alarm.copy(challengeType = challengeType)
            repository.updateAlarm(updated)
            if (updated.isEnabled) {
                scheduler.schedule(updated) // Re-schedule with new challenge
            }
        }
    }

    fun duplicateAlarm(alarm: Alarm) {
        viewModelScope.launch {
            val newAlarm = alarm.copy(
                id = UUID.randomUUID().toString(),
                isEnabled = alarm.isEnabled // Preserve original alarm's enabled state
            )
            repository.insertAlarm(newAlarm)
            if (newAlarm.isEnabled) {
                scheduler.schedule(newAlarm)
            }
            // Log Event
            analyticsManager.logEvent(
                com.aura.wake.data.analytics.AnalyticsManager.EVENT_ALARM_DUPLICATED,
                mapOf(com.aura.wake.data.analytics.AnalyticsManager.PARAM_ALARM_ID to newAlarm.id)
            )
        }
    }
}

