package com.alarm.app.data.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra("ALARM_ID")
        val challengeType = intent.getStringExtra("CHALLENGE_TYPE")
        
        Log.d("AlarmReceiver", "🔔 Alarm received: $alarmId, Challenge: $challengeType")

        // Acquire a wake lock to ensure device wakes up
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "AlarmApp:AlarmWakeLock"
        )
        wakeLock.acquire(10 * 60 * 1000L) // 10 minutes max

        try {
            val serviceIntent = Intent(context, AlarmService::class.java).apply {
                putExtra("ALARM_ID", alarmId)
                putExtra("CHALLENGE_TYPE", challengeType)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
            Log.d("AlarmReceiver", "✅ AlarmService started")
        } catch (e: Exception) {
            Log.e("AlarmReceiver", "❌ Failed to start AlarmService", e)
        } finally {
            // Release wake lock after a delay (service should acquire its own)
            wakeLock.release()
        }
    }
}

