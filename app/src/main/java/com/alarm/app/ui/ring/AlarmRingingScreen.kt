package com.alarm.app.ui.ring

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.AccessAlarm // Added
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alarm.app.R
import com.alarm.app.data.alarm.AlarmService
import com.alarm.app.data.alarm.SnoozeReceiver
import com.alarm.app.data.model.ChallengeType
import androidx.compose.material3.IconButton // Added
import androidx.compose.material.icons.Icons // Added for convenience if needed, but specific is better
import androidx.compose.material.icons.filled.Close // Added
import java.util.Calendar

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

    // Function to stop alarm and navigate home (called AFTER challenge success)
    val finishAlarm: () -> Unit = {
        if (!isPreview) {
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
    val application = context.applicationContext as com.alarm.app.AlarmApplication
    val settingsRepository = remember { application.container.settingsRepository }
    
    // These should ideally be collected as state if they can change while ringing (unlikely)
    // but just reading them once is fine for now.
    val mathConfig = remember { settingsRepository.getMathMissionConfig() }
    val typingConfig = remember { settingsRepository.getTypingMissionConfig() }
    val qrConfig = remember { settingsRepository.getQrMissionConfig() }
    
    val overlayUri = remember { settingsRepository.getOverlayImageUri() }
    
    if (isChallengeActive && challengeType != ChallengeType.NONE && !isPreview) {
        // Show the specific challenge UI
        when (challengeType) {
            ChallengeType.MATH -> com.alarm.app.ui.ring.challenges.MathChallenge(
                difficulty = mathConfig.difficulty,
                problemCount = mathConfig.problemCount,
                onCompleted = onDismiss
            )
            ChallengeType.SHAKE -> com.alarm.app.ui.ring.challenges.ShakeChallenge(onCompleted = onDismiss)
            ChallengeType.TYPING -> com.alarm.app.ui.ring.challenges.TypingChallenge(
                sentences = typingConfig.sentences.takeIf { it.isNotEmpty() } ?: listOf("I am unstoppable"),
                onCompleted = onDismiss
            )
            ChallengeType.QR -> com.alarm.app.ui.ring.challenges.QRChallenge(
                targetContent = qrConfig.qrContent,
                onCompleted = onDismiss
            )
            else -> onDismiss() 
        }
    } else if (isChallengeActive && challengeType != ChallengeType.NONE && isPreview) {
         // Previewing the challenge - Use current configs too!
         Box(modifier = Modifier.fillMaxSize()) {
            when (challengeType) {
                ChallengeType.MATH -> com.alarm.app.ui.ring.challenges.MathChallenge(
                    difficulty = mathConfig.difficulty,
                    problemCount = mathConfig.problemCount,
                    onCompleted = onDismiss
                )
                ChallengeType.SHAKE -> com.alarm.app.ui.ring.challenges.ShakeChallenge(onCompleted = onDismiss)
                ChallengeType.TYPING -> com.alarm.app.ui.ring.challenges.TypingChallenge(
                    sentences = typingConfig.sentences.takeIf { it.isNotEmpty() } ?: listOf("I am unstoppable"),
                    onCompleted = onDismiss
                )
                ChallengeType.QR -> com.alarm.app.ui.ring.challenges.QRChallenge(
                    targetContent = qrConfig.qrContent,
                    onCompleted = onDismiss
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
        // Main Ringing UI (Moon Theme)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Full Screen Background Image if Overlay Set
            if (overlayUri != null) {
                 coil.compose.AsyncImage(
                    model = overlayUri,
                    contentDescription = "Custom Overlay",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Dark Overlay for readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.3f))

                // Big Time
                Text(
                    text = String.format("%02d:%02d", currentTime.get(Calendar.HOUR_OF_DAY), currentTime.get(Calendar.MINUTE)),
                    fontSize = 86.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-2).sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Moon Image with Snooze
                Box(
                    modifier = Modifier.size(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Moon Graphic Placeholder - Pure rounded Circle, ONLY show if NO overlay
                    if (overlayUri == null) {
                        Box(
                            modifier = Modifier
                                .size(280.dp)
                                .clip(CircleShape)
                                .background(Color.DarkGray)
                        ) {
                             Text("🌑", fontSize = 200.sp, modifier = Modifier.align(Alignment.Center))
                        }
                    } else {
                        // Invisible spacer to keep layout standard
                         Spacer(modifier = Modifier.size(280.dp))
                    }
                    
                    // Snooze Button Floating Over Moon (Bottom)
                    Surface(
                        onClick = onSnooze,
                        shape = RoundedCornerShape(percent = 50),
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                            .height(56.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        ) {
                            Text(
                                "3", 
                                color = Color.White, 
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .background(Color.Black, CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                            Spacer(modifier = Modifier.size(12.dp))
                            Text("Snooze", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
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
                        .height(72.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Dismiss", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            
            // Close Button for Preview (Main Screen)
            if (isPreview) {
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
        }
    }
}
