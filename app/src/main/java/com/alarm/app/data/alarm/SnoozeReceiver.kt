package com.alarm.app.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.alarm.app.AlarmApplication
import com.alarm.app.data.model.Alarm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class SnoozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "ACTION_SNOOZE") {
            val alarmId = intent.getStringExtra("ALARM_ID") ?: return
            
            // Stop service first
            context.stopService(Intent(context, AlarmService::class.java))
            
            val app = context.applicationContext as? AlarmApplication ?: return
            val scheduler = app.container.alarmScheduler
            val repository = app.container.alarmRepository
            
            CoroutineScope(Dispatchers.IO).launch {
                val alarm = repository.getAlarm(alarmId)
                if (alarm != null) {
                    // Create a snoozed alarm instance (or just schedule it directly)
                    // For now, let's schedule a one-time alarm for 5 mins later
                    val snoozedTime = Calendar.getInstance().apply {
                        add(Calendar.MINUTE, 5)
                    }
                    
                    val snoozedAlarm = alarm.copy(
                        hour = snoozedTime.get(Calendar.HOUR_OF_DAY),
                        minute = snoozedTime.get(Calendar.MINUTE)
                    )
                    
                    scheduler.schedule(snoozedAlarm)
                }
            }
        }
    }
}
