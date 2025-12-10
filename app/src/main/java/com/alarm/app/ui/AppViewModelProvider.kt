package com.alarm.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.alarm.app.AlarmApplication
import com.alarm.app.ui.alarm.AlarmViewModel
import com.alarm.app.data.repository.SettingsRepository

object AppViewModelProvider {
    val Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as AlarmApplication
            val repository = application.container.alarmRepository
            val scheduler = application.container.alarmScheduler
            
            val settingsRepository = application.container.settingsRepository
            
            return when {
                modelClass.isAssignableFrom(AlarmViewModel::class.java) -> 
                    AlarmViewModel(repository, scheduler) as T
                modelClass.isAssignableFrom(com.alarm.app.ui.onboarding.OnboardingViewModel::class.java) ->
                    com.alarm.app.ui.onboarding.OnboardingViewModel(repository, scheduler, settingsRepository) as T
                else -> throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

