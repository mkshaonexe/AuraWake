package com.alarm.app.data.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.alarm.app.MainActivity
import com.alarm.app.R

class AlarmService : Service() {
    
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    companion object {
        const val CHANNEL_ID = "ALARM_CHANNEL"
        const val NOTIFICATION_ID = 1001
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val alarmId = intent?.getStringExtra("ALARM_ID")
        val challengeType = intent?.getStringExtra("CHALLENGE_TYPE")
        
        Log.d("AlarmService", "🔔 Service Command: $action for $alarmId")

        when (action) {
            "ACTION_SNOOZE" -> {
                // Handle Snooze: Stop ringing, Schedule new alarm for +5 mins
                Log.d("AlarmService", "💤 Snooze clicked")
                stopSelf() // Stops service (sound/vibration)
                // TODO: Actual rescheduling should be triggered here or by the Receiver handling the action
                // For simplicity, we'll broadcast a "SNOOZE" intent to AlarmReceiver or handle it here if we inject Scheduler
                // Let's rely on the PendingIntent to trigger a Boardcast or Activity.
                // Actually, let's make the notification action trigger a BroadcastReceiver that handles the logic.
                // But to save time/complexity, let's just use a dedicated receiver or handle in Service if possible (but Service is stopping).
                // Better approach: Redirect to a BroadcastReceiver that reschedules.
                
                // For now, let's assume the action launches a Receiver. 
                // Wait, I will modify the Notification Action to point to a Receiver.
                return START_NOT_STICKY
            }
            "ACTION_DISMISS" -> {
                Log.d("AlarmService", "❌ Dismiss clicked")
                // Dismiss implies solving challenge. So this should open the Activity just like clicking the notification body.
                val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra("ALARM_ID", alarmId)
                    putExtra("CHALLENGE_TYPE", challengeType)
                    putExtra("SHOW_ALARM_SCREEN", true)
                }
                startActivity(fullScreenIntent)
                // Service continues until challenge solved
                return START_STICKY
            }
        }
        
        Log.d("AlarmService", "🔔 Starting alarm service for: $alarmId")
        
        // Start foreground immediately
        startForeground(NOTIFICATION_ID, createNotification(alarmId, challengeType))
        
        // Start ringing
        startRinging()
        
        // Start vibration
        startVibration()
        
        // Launch full screen alarm activity
        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("ALARM_ID", alarmId)
            putExtra("CHALLENGE_TYPE", challengeType)
            putExtra("SHOW_ALARM_SCREEN", true)
        }
        startActivity(fullScreenIntent)
        
        Log.d("AlarmService", "✅ MainActivity launched with alarm screen")

        return START_STICKY
    }
    
    // ... (createNotificationChannel, startRinging, startVibration remain same) ...

    private fun createNotification(alarmId: String?, challengeType: String?): Notification {
        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("ALARM_ID", alarmId)
            putExtra("CHALLENGE_TYPE", challengeType)
            putExtra("SHOW_ALARM_SCREEN", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Dismiss Action (Opens Activity to solve challenge)
        val dismissIntent = Intent(this, MainActivity::class.java).apply {
            action = "ACTION_DISMISS"
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("ALARM_ID", alarmId)
            putExtra("CHALLENGE_TYPE", challengeType)
            putExtra("SHOW_ALARM_SCREEN", true)
        }
        val dismissPendingIntent = PendingIntent.getActivity(
            this, 1, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze Action (Broadcast to SnoozeReceiver)
        val snoozeIntent = Intent(this, com.alarm.app.data.alarm.SnoozeReceiver::class.java).apply {
            action = "ACTION_SNOOZE"
            putExtra("ALARM_ID", alarmId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            this, 2, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⏰ Alarm Ringing!")
            .setContentText("Wake Up! Solve the challenge to dismiss!")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(R.drawable.ic_launcher_foreground, "Snooze (5m)", snoozePendingIntent) // TODO: Use proper icon
            .addAction(R.drawable.ic_launcher_foreground, "Dismiss", dismissPendingIntent) // TODO: Use proper icon
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        Log.d("AlarmService", "🛑 Alarm service stopped")
    }
}

