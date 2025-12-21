package com.aura.wake.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Reusing WheelPicker from AlarmScreen if accessible, otherwise duplicate or move to common.
// Assuming we duplicate for now to avoid refactoring AlarmScreen publicly significantly or if it's private.
// AlarmScreen WheelPicker was private? Let's check. Yes, it was inside AlarmScreen.kt and likely not public or easily shared without refactor.
// I will duplicate a simple version here for speed and independence.
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.draw.alpha
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.DisposableEffect
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.snapshotFlow

@Composable
fun OnboardingTimeScreen(
    viewModel: OnboardingViewModel,
    onNext: () -> Unit
) {
    // Local state for UI components
    // We synchronize with ViewModel on Next
    
    // De-couple from VM initial state for smooth UI, update VM on confirm
    var tempHour by remember { mutableIntStateOf(viewModel.selectedHour) }
    var tempMinute by remember { mutableIntStateOf(viewModel.selectedMinute) }
    
    // Calculation for AM/PM logic similar to AlarmScreen
    val isPm = tempHour >= 12
    val displayHour = if (tempHour == 0) 12 else if (tempHour > 12) tempHour - 12 else tempHour
    var amPmState by remember { mutableIntStateOf(if (isPm) 1 else 0) } // 0=AM, 1=PM

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "Set your alarm time",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Time Picker Row
        Box(
             modifier = Modifier.fillMaxWidth().height(200.dp),
             contentAlignment = Alignment.Center
        ) {
              // Highlight
             Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2C2C2E)))
                    
             Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                 // Hour
                OnboardingWheelPicker(
                    count = 12,
                    initialItem = if (displayHour == 12) 11 else displayHour - 1,
                    format = { (it + 1).toString().padStart(2, '0') },
                    onItemSelected = { idx ->
                         val h = idx + 1
                         if (amPmState == 0) { // AM
                             tempHour = if (h == 12) 0 else h
                         } else { // PM
                             tempHour = if (h == 12) 12 else h + 12
                         }
                    },
                    modifier = Modifier.width(70.dp)
                )
                
                Text(":", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                
                // Minute
                 OnboardingWheelPicker(
                    count = 60,
                    initialItem = tempMinute,
                    format = { it.toString().padStart(2, '0') },
                    onItemSelected = { tempMinute = it },
                    modifier = Modifier.width(70.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // AM/PM
                OnboardingWheelPicker(
                    count = 2,
                    initialItem = amPmState,
                    format = { if (it == 0) "AM" else "PM" },
                    onItemSelected = { idx ->
                        amPmState = idx
                        val currentH12 = if (tempHour == 0) 12 else if (tempHour > 12) tempHour - 12 else tempHour
                         if (idx == 0) { // AM
                             tempHour = if (currentH12 == 12) 0 else currentH12
                         } else { // PM
                             tempHour = if (currentH12 == 12) 12 else currentH12 + 12
                         }
                    },
                     modifier = Modifier.width(70.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = {
                viewModel.updateTime(tempHour, tempMinute)
                onNext()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Next", fontSize = 18.sp, color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingWheelPicker(
    count: Int,
    initialItem: Int,
    format: (Int) -> String,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = initialItem) { count }
    val context = LocalContext.current
    var lastPage by remember { mutableIntStateOf(initialItem) }
    var soundLoaded by remember { mutableStateOf(false) }

    // Initialize SoundPool
    val soundPool = remember {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build().apply {
                setOnLoadCompleteListener { _, _, status ->
                    if (status == 0) soundLoaded = true
                }
            }
    }
    
    val soundId = remember(soundPool) {
        soundPool.load(context, com.aura.wake.R.raw.tick, 1)
    }

    // Release SoundPool on dispose
    DisposableEffect(soundPool) {
        onDispose {
            soundPool.release()
        }
    }
    
    // Use snapshotFlow to reliably detect page changes
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page ->
                onItemSelected(page)
                if (page != lastPage) {
                    lastPage = page
                    
                    // Haptic Feedback
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    if (vibrator?.hasVibrator() == true) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(25)
                        }
                    }
                    
                    // Sound Effect
                    if (soundLoaded) {
                        soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
                    }
                }
            }
    }

    VerticalPager(
        state = pagerState,
        modifier = modifier.height(180.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 60.dp)
    ) { page ->
        val isSelected = page == pagerState.currentPage
        Box(
            modifier = Modifier.height(60.dp).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = format(page),
                fontSize = if (isSelected) 32.sp else 24.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.alpha(if (isSelected) 1f else 0.5f)
            )
        }
    }
}
