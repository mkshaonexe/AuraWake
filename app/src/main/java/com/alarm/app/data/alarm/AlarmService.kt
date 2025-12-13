package com.alarm.app.data.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
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
    
    // Volume enforcement - prevents user from lowering alarm volume
    private var volumeEnforcementHandler: android.os.Handler? = null
    private var volumeEnforcementRunnable: Runnable? = null
    private var maxAlarmVolume: Int = 0

    companion object {
        const val CHANNEL_ID = "ALARM_CHANNEL"
        const val NOTIFICATION_ID = 1001
        const val VOLUME_CHECK_INTERVAL_MS = 500L // Check every 500ms
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private var overlayHelper: AlarmOverlayHelper? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        overlayHelper = AlarmOverlayHelper(this)
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
                overlayHelper?.removeOverlay()
                
                // Check if there is a challenge
                val hasChallenge = challengeType != null && challengeType != "NONE"
                
                if (hasChallenge) {
                    Log.d("AlarmService", "🛡️ Challenge exists ($challengeType), forcing app open")
                    val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        putExtra("ALARM_ID", alarmId)
                        putExtra("CHALLENGE_TYPE", challengeType)
                        putExtra("SHOW_ALARM_SCREEN", true)
                        putExtra("START_CHALLENGE", true) // Signal to start challenge immediately
                    }
                    startActivity(fullScreenIntent)
                    // Do NOT stopSelf() here, let the UI handle it after challenge
                } else {
                    Log.d("AlarmService", "✅ No challenge, stopping service")
                    stopSelf()
                }
                return START_REDELIVER_INTENT
            }
        }
        
        Log.d("AlarmService", "🔔 Starting alarm service for: $alarmId")
        
        startForeground(NOTIFICATION_ID, createNotification(alarmId, challengeType))
        startRinging()
        startVibration()
        
        // Show Overlay if permission granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && android.provider.Settings.canDrawOverlays(this)) {
             Log.d("AlarmService", "🛡️ Showing System Overlay")
             overlayHelper?.showOverlay(alarmId, challengeType)
        } else {
             Log.w("AlarmService", "⚠️ Overlay permission missing - relying on Activity")
        }
        
        // Acquire wake lock to ensure screen turns on (critical for Android 10+)
        val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        val wakeLock = powerManager.newWakeLock(
            android.os.PowerManager.FULL_WAKE_LOCK or 
            android.os.PowerManager.ACQUIRE_CAUSES_WAKEUP or
            android.os.PowerManager.ON_AFTER_RELEASE,
            "AlarmApp:AlarmScreenWakeLock"
        )
        wakeLock.acquire(10 * 1000L) // Hold for 10 seconds to allow activity launch
        
        val fullScreenIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or 
                Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            )
            putExtra("ALARM_ID", alarmId)
            putExtra("CHALLENGE_TYPE", challengeType)
            putExtra("SHOW_ALARM_SCREEN", true)
        }
        
        try {
            startActivity(fullScreenIntent)
            Log.d("AlarmService", "✅ MainActivity launched with alarm screen")
        } catch (e: Exception) {
            Log.e("AlarmService", "❌ Failed to start MainActivity", e)
        } finally {
            // Release wake lock after a short delay
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (wakeLock.isHeld) wakeLock.release()
            }, 5000)
        }

        return START_NOT_STICKY // Changed from START_REDELIVER_INTENT to avoid loop if crash? 
        // User asked for persistence. STICKY is better? 
        // Actually START_STICKY restarts service with null intent if killed.
        // START_REDELIVER_INTENT restarts with original intent (preserving alarm ID).
        // Let's keep START_REDELIVER_INTENT as it is safest for alarm reliability.
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d("AlarmService", "⚠️ App Task Removed - Restarting Alarm Service UI")
        
        // Ensure overlay is shown if task removed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && android.provider.Settings.canDrawOverlays(this)) {
             // Overlay likely persists anyway, but let's be sure
             // Accessing intent extras in onTaskRemoved might be tricky if not stored.
             // Usually onTaskRemoved doesn't pass the original intent.
        }
        
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
            // Set volume to maximum for alarm stream
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            maxAlarmVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxAlarmVolume, 0)
            Log.d("AlarmService", "🔊 Alarm volume set to max: $maxAlarmVolume")
            
            // Start volume enforcement - prevents user from lowering volume
            startVolumeEnforcement(audioManager)
            
            val settingsRepository = (applicationContext as com.alarm.app.AlarmApplication).container.settingsRepository
            val customRingtone = settingsRepository.getDefaultRingtoneUri()?.let { android.net.Uri.parse(it) }

            val alarmUri = customRingtone ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
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
    
    private fun startVolumeEnforcement(audioManager: AudioManager) {
        volumeEnforcementHandler = android.os.Handler(android.os.Looper.getMainLooper())
        volumeEnforcementRunnable = object : Runnable {
            override fun run() {
                try {
                    val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
                    if (currentVolume < maxAlarmVolume) {
                        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxAlarmVolume, 0)
                        Log.d("AlarmService", "🔒 Volume enforced: $currentVolume -> $maxAlarmVolume")
                    }
                } catch (e: Exception) {
                    Log.e("AlarmService", "Volume enforcement error", e)
                }
                volumeEnforcementHandler?.postDelayed(this, VOLUME_CHECK_INTERVAL_MS)
            }
        }
        volumeEnforcementHandler?.post(volumeEnforcementRunnable!!)
        Log.d("AlarmService", "🔒 Volume enforcement started")
    }
    
    private fun stopVolumeEnforcement() {
        volumeEnforcementRunnable?.let { volumeEnforcementHandler?.removeCallbacks(it) }
        volumeEnforcementHandler = null
        volumeEnforcementRunnable = null
        Log.d("AlarmService", "🔓 Volume enforcement stopped")
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
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or 
                Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            )
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

        // Notification WITHOUT Snooze/Dismiss buttons - forces user to open the full-screen UI
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("⏰ Alarm Ringing!")
            .setContentText("Tap to open alarm screen")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(pendingIntent, true) // Opens alarm screen on lock screen
            .setContentIntent(pendingIntent) // Opens alarm screen when notification is tapped
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        overlayHelper?.removeOverlay() // Ensure overlay is removed
        stopVolumeEnforcement() 
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        Log.d("AlarmService", "🛑 Alarm service stopped")
    }
}

