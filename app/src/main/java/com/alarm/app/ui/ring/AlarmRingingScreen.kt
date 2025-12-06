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
import java.util.Calendar

@Composable
fun AlarmRingingScreen(
    navController: NavController,
    alarmId: String? = null,
    initialChallengeTypeStr: String? = null,
    startChallengeImmediately: Boolean = false
) {
    val context = LocalContext.current
    var isChallengeActive by remember { mutableStateOf(startChallengeImmediately) }
    
    // Block Back Button to prevent accidental closing
    androidx.activity.compose.BackHandler(enabled = true) {
        // Do nothing. User must Dismiss or Snooze.
    }
    
    // Parse the challenge type
    val challengeType = try {
        if (initialChallengeTypeStr != null) ChallengeType.valueOf(initialChallengeTypeStr) else ChallengeType.NONE
    } catch (e: Exception) { ChallengeType.NONE }
    
    val currentTime = Calendar.getInstance()
    
    // Function to stop alarm and navigate home (called AFTER challenge success)
    val finishAlarm: () -> Unit = {
        context.stopService(Intent(context, AlarmService::class.java))
        navController.navigate("home") { 
            popUpTo("ringing") { inclusive = true } 
        }
    }
    
    // Function to snooze
    val snoozeAlarm: () -> Unit = {
        if (alarmId != null) {
            val intent = Intent(context, SnoozeReceiver::class.java).apply {
                action = "ACTION_SNOOZE"
                putExtra("ALARM_ID", alarmId)
            }
            context.sendBroadcast(intent)
        } else {
            // For preview/testing without ID -> just stop service
            context.stopService(Intent(context, AlarmService::class.java))
        }
        navController.navigate("home") { 
             popUpTo("ringing") { inclusive = true } 
        }
    }
    
    if (isChallengeActive && challengeType != ChallengeType.NONE) {
        // Show the specific challenge UI
        when (challengeType) {
            ChallengeType.MATH -> com.alarm.app.ui.ring.challenges.MathChallenge(onCompleted = finishAlarm)
            ChallengeType.SHAKE -> com.alarm.app.ui.ring.challenges.ShakeChallenge(onCompleted = finishAlarm)
            ChallengeType.TYPING -> com.alarm.app.ui.ring.challenges.TypingChallenge(onCompleted = finishAlarm)
            ChallengeType.QR -> com.alarm.app.ui.ring.challenges.QRChallenge(onCompleted = finishAlarm)
            else -> finishAlarm() 
        }
    } else {
        // Main Ringing UI (Moon Theme)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // "Notification-like" Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1C1C1E)) // Notification gray bg
                        .padding(16.dp)
                ) {
                   Column {
                       Row(
                           verticalAlignment = Alignment.CenterVertically
                       ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.AccessAlarm, // Or app icon
                                contentDescription = "App Icon",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Alarmy • Just now", color = Color.Gray, fontSize = 12.sp)
                       }
                       Spacer(modifier = Modifier.height(8.dp))
                       Text("Alarmy", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                       Text("Tap to dismiss ⏰ ⏰ ⏰ ⏰ ⏰", color = Color.Gray, fontSize = 12.sp)
                   }
                }
                
                Spacer(modifier = Modifier.weight(0.1f))

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
                    // Moon Graphic Placeholder - Pure rounded Circle
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .clip(CircleShape)
                            .background(Color.DarkGray)
                    ) {
                         Text("🌑", fontSize = 200.sp, modifier = Modifier.align(Alignment.Center))
                    }
                    
                    // Snooze Button Floating Over Moon (Bottom)
                    Surface(
                        onClick = snoozeAlarm,
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
                            finishAlarm()
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
        }
    }
}
