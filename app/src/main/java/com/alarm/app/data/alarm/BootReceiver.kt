package com.alarm.app.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.alarm.app.AlarmApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "📱 Device booted, rescheduling alarms...")
            
            val app = context.applicationContext as? AlarmApplication
            if (app != null) {
                val repository = app.container.alarmRepository
                val scheduler = app.container.alarmScheduler
                
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val alarms = repository.getAllAlarms().first()
                        alarms.filter { it.isEnabled }.forEach { alarm ->
                            scheduler.schedule(alarm)
                            Log.d("BootReceiver", "✅ Rescheduled alarm: ${alarm.id}")
                        }
                    } catch (e: Exception) {
                        Log.e("BootReceiver", "❌ Failed to reschedule alarms", e)
                    }
                }
            }
        }
    }
}
