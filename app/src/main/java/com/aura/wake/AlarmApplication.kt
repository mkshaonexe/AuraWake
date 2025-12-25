package com.aura.wake

import android.app.Application
import android.content.Context
import com.aura.wake.data.alarm.AlarmScheduler
import com.aura.wake.data.alarm.AndroidAlarmScheduler
import com.aura.wake.data.local.AlarmDatabase
import com.aura.wake.data.repository.AlarmRepository
import com.aura.wake.data.repository.OfflineAlarmRepository
import com.aura.wake.data.repository.SettingsRepository
import com.aura.wake.data.repository.SharedPreferencesSettingsRepository

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
    val settingsRepository: SettingsRepository
    val analyticsManager: com.aura.wake.data.analytics.AnalyticsManager
    val wakeHistoryRepository: com.aura.wake.data.repository.WakeHistoryRepository
}

class AppDefaultContainer(private val context: Context) : AppContainer {
    override val alarmRepository: AlarmRepository by lazy {
        OfflineAlarmRepository(AlarmDatabase.getDatabase(context).alarmDao())
    }
    override val alarmScheduler: AlarmScheduler by lazy {
        AndroidAlarmScheduler(context)
    }
    override val settingsRepository: SettingsRepository by lazy {
        com.aura.wake.data.repository.SharedPreferencesSettingsRepository(context)
    }
    override val analyticsManager: com.aura.wake.data.analytics.AnalyticsManager by lazy {
        com.aura.wake.data.analytics.AnalyticsManager(context)
    }
    override val wakeHistoryRepository: com.aura.wake.data.repository.WakeHistoryRepository by lazy {
        com.aura.wake.data.repository.WakeHistoryRepository(AlarmDatabase.getDatabase(context).wakeHistoryDao())
    }
}

