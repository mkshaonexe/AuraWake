package com.alarm.app.data.repository

import com.alarm.app.data.local.AlarmDao
import com.alarm.app.data.model.Alarm
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun getAllAlarms(): Flow<List<Alarm>>
    suspend fun getAlarmById(id: String): Alarm?
    suspend fun getAlarm(id: String): Alarm? = getAlarmById(id)
    suspend fun insertAlarm(alarm: Alarm)
    suspend fun updateAlarm(alarm: Alarm)
    suspend fun deleteAlarm(alarm: Alarm)
}

class OfflineAlarmRepository(private val alarmDao: AlarmDao) : AlarmRepository {
    override fun getAllAlarms(): Flow<List<Alarm>> = alarmDao.getAllAlarms()
    override suspend fun getAlarmById(id: String): Alarm? = alarmDao.getAlarmById(id)
    override suspend fun insertAlarm(alarm: Alarm) = alarmDao.insertAlarm(alarm)
    override suspend fun updateAlarm(alarm: Alarm) = alarmDao.updateAlarm(alarm)
    override suspend fun deleteAlarm(alarm: Alarm) = alarmDao.deleteAlarm(alarm)
}
