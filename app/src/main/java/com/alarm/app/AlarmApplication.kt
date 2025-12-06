package com.alarm.app

import android.app.Application
import android.content.Context
import com.alarm.app.data.alarm.AlarmScheduler
import com.alarm.app.data.alarm.AndroidAlarmScheduler
import com.alarm.app.data.local.AlarmDatabase
import com.alarm.app.data.repository.AlarmRepository
import com.alarm.app.data.repository.OfflineAlarmRepository

class AlarmApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDefaultContainer(this)
    }
}

interface AppContainer {
    val alarmRepository: AlarmRepository
    val alarmScheduler: AlarmScheduler
}

class AppDefaultContainer(private val context: Context) : AppContainer {
    override val alarmRepository: AlarmRepository by lazy {
        OfflineAlarmRepository(AlarmDatabase.getDatabase(context).alarmDao())
    }
    override val alarmScheduler: AlarmScheduler by lazy {
        AndroidAlarmScheduler(context)
    }
}

