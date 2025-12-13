package com.alarm.app.ui.onboarding

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarm.app.data.alarm.AlarmScheduler
import com.alarm.app.data.model.Alarm
import com.alarm.app.data.model.ChallengeType
import com.alarm.app.data.repository.AlarmRepository
import com.alarm.app.data.repository.SettingsRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class OnboardingViewModel(
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // Default to current time + 1 hour or 7:00 AM? User request image showed 07:00 AM.
    // Let's default to 7:00 AM.
    var selectedHour by mutableIntStateOf(7)
    var selectedMinute by mutableIntStateOf(0)
    
    // Default Sound
    var selectedSound by mutableStateOf("Default")
    var selectedSoundUri by mutableStateOf<String?>(null)
    
    // Default Mission
    var selectedChallenge by mutableStateOf(ChallengeType.NONE)

    fun updateTime(hour: Int, minute: Int) {
        selectedHour = hour
        selectedMinute = minute
    }

    fun updateSound(soundName: String, soundUri: String?) {
        selectedSound = soundName
        selectedSoundUri = soundUri
    }

    fun updateChallenge(challenge: ChallengeType) {
        selectedChallenge = challenge
    }

    fun completeOnboarding(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Save the selected ringtone as default
             if (selectedSoundUri != null) {
                settingsRepository.saveDefaultRingtoneUri(selectedSoundUri)
            }
            
            // Create the alarm
            val alarm = Alarm(
                hour = selectedHour,
                minute = selectedMinute,
                isEnabled = true,
                daysOfWeek = emptySet(), 
                challengeType = selectedChallenge,
                label = "Wake Up" 
            )
            
            alarmRepository.insertAlarm(alarm)
            alarmScheduler.schedule(alarm)
            
            // Mark first run complete
            settingsRepository.setFirstRunCompleted()
            
            onSuccess()
        }
    }
}
