package com.alarm.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.alarm.app.AlarmApplication
import com.alarm.app.ui.alarm.AlarmViewModel

object AppViewModelProvider {
    val Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as AlarmApplication
            val repository = application.container.alarmRepository
            val scheduler = application.container.alarmScheduler
            
            return when {
                modelClass.isAssignableFrom(AlarmViewModel::class.java) -> 
                    AlarmViewModel(repository, scheduler) as T
                else -> throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

