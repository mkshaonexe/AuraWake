package com.alarm.app.ui.ring

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alarm.app.data.alarm.AlarmService
import com.alarm.app.data.model.ChallengeType
import java.util.Calendar

@Composable
fun AlarmRingingScreen(
    navController: NavController,
    alarmId: String? = null
) {
    val context = LocalContext.current
    var challengeStep by remember { mutableStateOf<ChallengeType?>(null) }
    
    val currentTime = Calendar.getInstance()
    
    // Function to stop alarm and navigate home
    val dismissAlarm: () -> Unit = {
        // Stop the AlarmService (stops sound + vibration)
        context.stopService(Intent(context, AlarmService::class.java))
        navController.navigate("home") { 
            popUpTo("ringing") { inclusive = true } 
        }
    }
    
    if (challengeStep != null) {
        when (challengeStep) {
            ChallengeType.MATH -> com.alarm.app.ui.ring.challenges.MathChallenge(onCompleted = dismissAlarm)
            ChallengeType.SHAKE -> com.alarm.app.ui.ring.challenges.ShakeChallenge(onCompleted = dismissAlarm)
            ChallengeType.TYPING -> com.alarm.app.ui.ring.challenges.TypingChallenge(onCompleted = dismissAlarm)
            ChallengeType.QR -> com.alarm.app.ui.ring.challenges.QRChallenge(onCompleted = dismissAlarm)
            else -> dismissAlarm() // If NONE, just dismiss
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = String.format("%02d:%02d", currentTime.get(Calendar.HOUR_OF_DAY), currentTime.get(Calendar.MINUTE)),
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Button(
                onClick = {
                    val randomChallenge = listOf(ChallengeType.MATH, ChallengeType.SHAKE, ChallengeType.TYPING).random()
                    challengeStep = randomChallenge
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Text("DISMISS (Start Challenge)", fontSize = 24.sp)
            }
        }
    }
}

