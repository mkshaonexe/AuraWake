package com.alarm.app.data.alarm

import com.alarm.app.data.model.Alarm

interface AlarmScheduler {
    fun schedule(alarm: Alarm)
    fun cancel(alarm: Alarm)
}
