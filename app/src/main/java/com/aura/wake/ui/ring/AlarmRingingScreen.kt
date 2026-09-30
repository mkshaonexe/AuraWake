package com.aura.wake.ui.ring

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.ui.graphics.graphicsLayer
import com.aura.wake.ui.overlay.OverlayBackground
import com.aura.wake.ui.overlay.OverlayPresets
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aura.wake.R
import com.aura.wake.data.alarm.AlarmService
import com.aura.wake.data.alarm.SnoozeReceiver
import com.aura.wake.data.model.ChallengeType
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import java.util.Calendar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AlarmRingingScreen(
    navController: NavController,
    alarmId: String? = null,
    initialChallengeTypeStr: String? = null,
    startChallengeImmediately: Boolean = false,
    isPreview: Boolean = false
) {
    val context = LocalContext.current
    
    // Parse the challenge type
    val challengeType = try {
        if (initialChallengeTypeStr != null) ChallengeType.valueOf(initialChallengeTypeStr) else ChallengeType.NONE
    } catch (e: Exception) { ChallengeType.NONE }

    // Get repositories
    val application = context.applicationContext as com.aura.wake.AlarmApplication
    val wakeHistoryRepository = remember { application.container.wakeHistoryRepository }
    val alarmRepository = remember { application.container.alarmRepository }
    
    // Fetch alarm details if we have an ID
    var alarmHour by remember { mutableStateOf(0) }
    var alarmMinute by remember { mutableStateOf(0) }
    
    LaunchedEffect(alarmId) {
        if (alarmId != null) {
            try {
                val alarm = alarmRepository.getAlarmById(alarmId)
                alarm?.let {
                    alarmHour = it.hour
                    alarmMinute = it.minute
                }
            } catch (e: Exception) {
                // Alarm not found, continue without recording
            }
        }
    }

    // Function to stop alarm and navigate home (called AFTER challenge success)
    val finishAlarm: () -> Unit = {
        if (!isPreview) {
            // Record wake-up in history
            if (alarmId != null && alarmHour != 0) {
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    try {
                        wakeHistoryRepository.recordWakeUp(alarmHour, alarmMinute)
                    } catch (e: Exception) {
                        // Failed to record, but don't block alarm dismissal
                    }
                }
            }
            
            context.stopService(Intent(context, AlarmService::class.java))
            navController.navigate("home") { 
                popUpTo("ringing") { inclusive = true } 
            }
        } else {
            navController.popBackStack()
        }
    }
    
    // Function to snooze
    val snoozeAlarm: () -> Unit = {
        if (!isPreview) {
            if (alarmId != null) {
                val intent = Intent(context, SnoozeReceiver::class.java).apply {
                    action = "ACTION_SNOOZE"
                    putExtra("ALARM_ID", alarmId)
                }
                context.sendBroadcast(intent)
            } else {
                context.stopService(Intent(context, AlarmService::class.java))
            }
            navController.navigate("home") { 
                 popUpTo("ringing") { inclusive = true } 
            }
        } else {
             navController.popBackStack()
        }
    }

    // Listen for Alarm Stop Broadcast (syncs dismissal if Overlay finishes it)
    DisposableEffect(Unit) {
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(context: android.content.Context?, intent: Intent?) {
                if (intent?.action == "com.aura.wake.ACTION_ALARM_STOPPED") {
                    finishAlarm()
                }
            }
        }
        val filter = android.content.IntentFilter("com.aura.wake.ACTION_ALARM_STOPPED")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
             context.registerReceiver(receiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED)
        } else {
             context.registerReceiver(receiver, filter)
        }

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // Call the content wrapper
    AlarmRingingContent(
        challengeType = challengeType,
        startChallengeImmediately = startChallengeImmediately,
        isPreview = isPreview,
        onSnooze = snoozeAlarm,
        onDismiss = finishAlarm, // This is final dismiss or post-challenge
        onClosePreview = { navController.popBackStack() }
    )
}

@Composable
fun AlarmRingingContent(
    challengeType: ChallengeType,
    startChallengeImmediately: Boolean,
    isPreview: Boolean,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit,
    onClosePreview: () -> Unit
) {
    var isChallengeActive by remember { mutableStateOf(startChallengeImmediately) }
    
    // Block Back Button to prevent accidental closing
    androidx.activity.compose.BackHandler(enabled = true) {
        // Do nothing. User must Dismiss or Snooze.
        // except in preview mode
        if (isPreview) {
            onClosePreview()
        }
    }
    
    val currentTime = Calendar.getInstance()
    
    
    // Fetch Global Mission Configs
    val context = LocalContext.current
    val application = context.applicationContext as com.aura.wake.AlarmApplication
    val settingsRepository = remember { application.container.settingsRepository }
    
    // These should ideally be collected as state if they can change while ringing (unlikely)
    // but just reading them once is fine for now.
    val mathConfig = remember { settingsRepository.getMathMissionConfig() }
    val typingConfig = remember { settingsRepository.getTypingMissionConfig() }
    val qrConfig = remember { settingsRepository.getQrMissionConfig() }
    val memoryConfig = remember { settingsRepository.getMemoryMissionConfig() }
    
    val overlayUri = remember { settingsRepository.getOverlayImageUri() }
    
    if (isChallengeActive && challengeType != ChallengeType.NONE && !isPreview) {
        // Show the specific challenge UI
        when (challengeType) {
            ChallengeType.MATH -> com.aura.wake.ui.ring.challenges.MathChallenge(
                difficulty = mathConfig.difficulty,
                problemCount = mathConfig.problemCount,
                onCompleted = onDismiss
            )
            ChallengeType.SHAKE -> com.aura.wake.ui.ring.challenges.ShakeChallenge(onCompleted = onDismiss)
            ChallengeType.TYPING -> com.aura.wake.ui.ring.challenges.TypingChallenge(
                sentences = typingConfig.sentences.takeIf { it.isNotEmpty() } ?: listOf("I am unstoppable"),
                onCompleted = onDismiss
            )
            ChallengeType.QR -> com.aura.wake.ui.ring.challenges.QRChallenge(
                targetContent = qrConfig.qrContent,
                onCompleted = onDismiss
            )
            ChallengeType.MEMORY -> com.aura.wake.ui.ring.challenges.MemoryChallenge(
                difficulty = memoryConfig.difficulty,
                questionCount = memoryConfig.questionCount,
                onCompleted = onDismiss
            )
            else -> onDismiss() 
        }
    } else if (isChallengeActive && challengeType != ChallengeType.NONE && isPreview) {
         // Previewing the challenge - Use current configs too!
         Box(modifier = Modifier.fillMaxSize()) {
            when (challengeType) {
                ChallengeType.MATH -> com.aura.wake.ui.ring.challenges.MathChallenge(
                    difficulty = mathConfig.difficulty,
                    problemCount = mathConfig.problemCount,
                    onCompleted = onDismiss
                )
                ChallengeType.SHAKE -> com.aura.wake.ui.ring.challenges.ShakeChallenge(onCompleted = onDismiss)
                ChallengeType.TYPING -> com.aura.wake.ui.ring.challenges.TypingChallenge(
                    sentences = typingConfig.sentences.takeIf { it.isNotEmpty() } ?: listOf("I am unstoppable"),
                    onCompleted = onDismiss
                )
                ChallengeType.QR -> com.aura.wake.ui.ring.challenges.QRChallenge(
                    targetContent = qrConfig.qrContent,
                    onCompleted = onDismiss
                )
                ChallengeType.MEMORY -> com.aura.wake.ui.ring.challenges.MemoryChallenge(
                    difficulty = memoryConfig.difficulty,
                    questionCount = memoryConfig.questionCount,
                    onCompleted = onDismiss,
                    onClose = onClosePreview
                )
                else -> onDismiss()
            }
            
            // Close Button Overlay for Preview
            IconButton(
                onClick = onClosePreview,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .padding(top = 24.dp) 
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Close,
                    contentDescription = "Close Preview",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
         }
    } else {
        // Main Ringing UI (Modern Aura Theme)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Full Screen Background (Presets or Custom Wallpaper with high contrast scrim)
            OverlayBackground(
                overlayUri = overlayUri,
                modifier = Modifier.fillMaxSize(),
                animatePulse = true
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.15f))

                // Date Display (e.g. WEDNESDAY, SEPTEMBER 30)
                val dateText = remember {
                    SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(currentTime.time)
                }
                Text(
                    text = dateText.uppercase(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Big Clean Digital Clock
                Text(
                    text = String.format("%02d:%02d", currentTime.get(Calendar.HOUR_OF_DAY), currentTime.get(Calendar.MINUTE)),
                    fontSize = 82.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-2).sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Status chip: "ALARM RINGING"
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier.height(28.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF5252))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "ALARM RINGING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(0.35f))

                // Center Graphic Area:
                // Minimal breathing Aura Ring if preset theme; clean unobstructed view if custom photo
                if (OverlayPresets.isPreset(overlayUri)) {
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse_ring")
                    val ringScale by infiniteTransition.animateFloat(
                        initialValue = 0.94f,
                        targetValue = 1.06f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2800, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "ringScale"
                    )

                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .graphicsLayer(scaleX = ringScale, scaleY = ringScale)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.5.dp, Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(115.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.size(170.dp))
                }

                Spacer(modifier = Modifier.weight(0.7f))

                // Snooze Button - Sleek Frosted Pill
                Surface(
                    onClick = onSnooze,
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.92f),
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(52.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Snooze,
                            contentDescription = "Snooze",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Snooze",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dismiss Button - Bottom Red Pill
                Button(
                    onClick = {
                        if (challengeType == ChallengeType.NONE) {
                            onDismiss()
                        } else {
                            isChallengeActive = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Text("Dismiss", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Close Button for Preview (Main Screen)
            if (isPreview) {
                 IconButton(
                    onClick = onClosePreview,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
                        contentDescription = "Close Preview",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
