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
        
        Log.d("AlarmService", "🔔 Service Command: $action for $alarmId, Challenge: $challengeType")

        when (action) {
            "ACTION_SNOOZE" -> {
                Log.d("AlarmService", "💤 Snooze clicked")
                stopSelf() 
                val snoozeIntent = Intent(this, com.alarm.app.data.alarm.SnoozeReceiver::class.java).apply {
                    this.action = "ACTION_SNOOZE"
                    putExtra("ALARM_ID", alarmId)
                }
                sendBroadcast(snoozeIntent)
                return START_NOT_STICKY
            }
            "ACTION_DISMISS" -> {
                Log.d("AlarmService", "❌ Dismiss clicked")
                val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra("ALARM_ID", alarmId)
                    putExtra("CHALLENGE_TYPE", challengeType)
                    putExtra("SHOW_ALARM_SCREEN", true)
                    putExtra("START_CHALLENGE", true) // Signal to start challenge immediately
                }
                startActivity(fullScreenIntent)
                return START_REDELIVER_INTENT
            }
        }
        
        Log.d("AlarmService", "🔔 Starting alarm service for: $alarmId")
        
        startForeground(NOTIFICATION_ID, createNotification(alarmId, challengeType))
        startRinging()
        startVibration()
        
        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("ALARM_ID", alarmId)
            putExtra("CHALLENGE_TYPE", challengeType)
            putExtra("SHOW_ALARM_SCREEN", true)
        }
        startActivity(fullScreenIntent)
        
        Log.d("AlarmService", "✅ MainActivity launched with alarm screen")

        return START_REDELIVER_INTENT
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d("AlarmService", "⚠️ App Task Removed - Restarting Alarm Service UI")
        
        // If the user swipes the app away, we want to bring the alarm screen back!
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            // Ideally we should persist the current alarm ID to SharedPreferences to restore it fully,
            // but if the Service is still running or restarting, `onStartCommand` might trigger.
            // Using logic to recreate the Activity.
             putExtra("SHOW_ALARM_SCREEN", true)
        }
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm notifications with sound"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun startRinging() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@AlarmService, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
            Log.d("AlarmService", "🔊 Alarm sound started")
        } catch (e: Exception) {
            Log.e("AlarmService", "❌ Failed to play alarm sound", e)
        }
    }

    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 500, 200, 500), 0)
        }
        Log.d("AlarmService", "📳 Vibration started")
    }

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
        
        // Dismiss Action 
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

        // Snooze Action 
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
            // Use setFullScreenIntent to launch the activity immediately if device is locked
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(R.drawable.ic_launcher_foreground, "Snooze (5m)", snoozePendingIntent) 
            .addAction(R.drawable.ic_launcher_foreground, "Dismiss", dismissPendingIntent) 
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

