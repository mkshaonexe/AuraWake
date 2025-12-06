package com.alarm.app.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarm.app.data.alarm.AlarmScheduler
import com.alarm.app.data.model.Alarm
import com.alarm.app.data.model.ChallengeType
import com.alarm.app.data.repository.AlarmRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class AlarmViewModel(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler
) : ViewModel() {

    val allAlarms: StateFlow<List<Alarm>> = repository.getAllAlarms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addAlarm(hour: Int, minute: Int, challengeType: ChallengeType) {
        viewModelScope.launch {
            val alarm = Alarm(
                id = UUID.randomUUID().toString(),
                hour = hour,
                minute = minute,
                isEnabled = true,
                challengeType = challengeType
            )
            repository.insertAlarm(alarm)
            scheduler.schedule(alarm) // Schedule the alarm with AlarmManager
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
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            scheduler.cancel(alarm)
            repository.deleteAlarm(alarm)
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
}

