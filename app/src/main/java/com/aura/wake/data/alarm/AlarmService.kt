package com.aura.wake.data.alarm

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
import com.aura.wake.MainActivity
import com.aura.wake.R

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
                val snoozeIntent = Intent(this, com.aura.wake.data.alarm.SnoozeReceiver::class.java).apply {
                    this.action = "ACTION_SNOOZE"
                    putExtra("ALARM_ID", alarmId)
                }
                sendBroadcast(snoozeIntent)
                return START_NOT_STICKY
            }
            "ACTION_DISMISS" -> {
                Log.d("AlarmService", "❌ Dismiss clicked")
                overlayHelper?.removeOverlay() // Ensure overlay is removed
                
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
                } else {
                    Log.d("AlarmService", "✅ No challenge, stopping service")
                    stopSelf()
                }
                return START_REDELIVER_INTENT
            }
        }
        
        Log.d("AlarmService", "🔔 Starting alarm service for: $alarmId")
        
        // 1. Start Foreground with Full Screen Intent Notification (ALWAYS - Base Layer)
        val notification = createNotification(alarmId, challengeType)
        startForeground(NOTIFICATION_ID, notification)
        
        intent?.let {
            val ringtoneUri = it.getStringExtra("RINGTONE_URI")
            startRinging(ringtoneUri)
        } ?: startRinging(null)
        startVibration()
        
        // 2. Hybrid Logic: If Overlay is permitted, Show Overlay (Strong Layer)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && android.provider.Settings.canDrawOverlays(this)) {
             Log.d("AlarmService", "🛡️ Hybrid Mode: Showing System Overlay")
             overlayHelper?.showOverlay(alarmId, challengeType)
        } else {
             Log.d("AlarmService", "ℹ️ Hybrid Mode: Overlay not granted, relying on Full Screen Intent")
        }
        
        // 3. Keep Activity Launch as backup/primary for non-overlay cases
        // We do this AFTER startForeground to maximize chances of success
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
            Log.d("AlarmService", "✅ MainActivity launch attempted")
        } catch (e: Exception) {
            Log.e("AlarmService", "ℹ️ MainActivity launch suppressed (normal in background), relying on Notification", e)
        }

        // Return START_STICKY to ensure service restarts if killed
        return START_STICKY 
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d("AlarmService", "⚠️ App Task Removed - Attempting to persist Alarm")
        
        // 1. If Overlay is permitted, it likely stays.
        // 2. We should try to restart the Activity.
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
             putExtra("SHOW_ALARM_SCREEN", true)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) { e.printStackTrace() }
        
        // 3. EXPERIMENTAL: Restart Service if it was killed?
        // Usually START_STICKY handles this, but onTaskRemoved is just a callback.
        // We don't stopSelf() here.
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

    private fun startRinging(specificRingtoneUri: String?) {
        try {
            // Set volume to maximum for alarm stream
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            maxAlarmVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxAlarmVolume, 0)
            Log.d("AlarmService", "🔊 Alarm volume set to max: $maxAlarmVolume")
            
            // Start volume enforcement - prevents user from lowering volume
            startVolumeEnforcement(audioManager)
            
            val settingsRepository = (applicationContext as com.aura.wake.AlarmApplication).container.settingsRepository
            val defaultRingtone = settingsRepository.getDefaultRingtoneUri()
            
            val targetUriString = specificRingtoneUri ?: defaultRingtone
            val targetUri = targetUriString?.let { android.net.Uri.parse(it) }

            val alarmUri = targetUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
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
        overlayHelper?.removeOverlay()
        stopVolumeEnforcement() 
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        Log.d("AlarmService", "🛑 Alarm service stopped")
    }
}

