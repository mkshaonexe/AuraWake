package com.alarm.app.ui.alarm

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alarm.app.data.model.ChallengeType
import com.alarm.app.ui.AppViewModelProvider
import kotlinx.coroutines.launch
import java.util.Calendar
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.DisposableEffect
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.ui.platform.LocalContext
import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AlarmScreen(
    navController: NavController,
    alarmId: String? = null,
    viewModel: AlarmViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val currentTime = Calendar.getInstance()
    var selectedHour by remember { mutableStateOf(currentTime.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(currentTime.get(Calendar.MINUTE)) }
    
    // For 12h format logic (UI only, data still 24h)
    val isPm = selectedHour >= 12
    val displayHour = if (selectedHour == 0) 12 else if (selectedHour > 12) selectedHour - 12 else selectedHour
    val amPmState = remember { mutableStateOf(if (isPm) 1 else 0) } // 0=AM, 1=PM

    var selectedChallenge by remember { mutableStateOf(ChallengeType.NONE) }
    var alarmName by remember { mutableStateOf("") }
    val selectedDays = remember { mutableStateListOf<Int>().apply { addAll(listOf(1,2,3,4,5,6,7)) } } // 1=Sun, ..., 7=Sat - Default to all days
    var isDaily by remember { mutableStateOf(true) } // Default to Daily mode

    val ringInString by remember {
        derivedStateOf {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            if (target.before(now)) {
                target.add(Calendar.DAY_OF_YEAR, 1)
            }
            
            val diffMillis = target.timeInMillis - now.timeInMillis
            val diffMinutes = diffMillis / (1000 * 60)
            val hours = diffMinutes / 60
            val minutes = diffMinutes % 60
            
            "Ring in $hours hr. $minutes min"
        }
    }

    // Init Logic for scrolling to current time would be complex with Pager w/o `scrollToPage` initial
    // Keep internal hour/min state and update it when pager verification settles.

    Scaffold(
        containerColor = Color(0xFF1C1C1E), // Dark Background
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Wake-up alarm", 
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1C1C1E))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Name Input Field
            androidx.compose.material3.OutlinedTextField(
                value = alarmName,
                onValueChange = { alarmName = it },
                placeholder = { Text("Please fill in the alarm name", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.Yellow,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    Icon(
                        Icons.Default.Edit, 
                        contentDescription = null, 
                        tint = Color.Gray, 
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = androidx.compose.material3.TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedIndicatorColor = Color.Gray,
                    unfocusedIndicatorColor = Color.Gray.copy(alpha = 0.5f)
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
            
            // Wheel Time Picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF1C1C1E)),
                contentAlignment = Alignment.Center
            ) {
                // Background highlight
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
                    // Hour Pager
                    // Simplified: We assume user picks 24h format for simplicity or need complex 12h mapping
                    // Let's implement simple vertical list for now or just text inputs if pager is tricky without accompanist
                    // Using basic VerticalPager
                    WheelPicker(
                        count = 12,
                        initialItem = if (displayHour == 12) 11 else displayHour - 1, // 0-11 mapping for 1-12
                        format = { (it + 1).toString().padStart(2, '0') },
                        onItemSelected = { idx -> 
                             // Logic to update hour based on AM/PM
                             val h = idx + 1
                             if (amPmState.value == 0) { // AM
                                 selectedHour = if (h == 12) 0 else h
                             } else { // PM
                                 selectedHour = if (h == 12) 12 else h + 12
                             }
                        },
                        modifier = Modifier.width(80.dp)
                    )
                    
                    Text(":", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    
                    // Minute Pager
                    WheelPicker(
                        count = 60,
                        initialItem = selectedMinute,
                        format = { it.toString().padStart(2, '0') },
                        onItemSelected = { selectedMinute = it },
                        modifier = Modifier.width(80.dp)
                    )
                    
                    // AM/PM
                     WheelPicker(
                        count = 2,
                        initialItem = amPmState.value,
                        format = { if (it == 0) "AM" else "PM" },
                        onItemSelected = { idx -> 
                            amPmState.value = idx 
                            // Re-adjust hour
                            val currentH12 = if (selectedHour == 0) 12 else if (selectedHour > 12) selectedHour - 12 else selectedHour
                             if (idx == 0) { // AM
                                 selectedHour = if (currentH12 == 12) 0 else currentH12
                             } else { // PM
                                 selectedHour = if (currentH12 == 12) 12 else currentH12 + 12
                             }
                        },
                        modifier = Modifier.width(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = ringInString,
                color = Color.Gray,
                fontSize = 16.sp // Slightly larger for readability
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Daily Checkbox
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Daily", color = Color.Gray)
                Checkbox(
                    checked = isDaily,
                    onCheckedChange = { 
                        isDaily = it 
                        if (it) {
                            selectedDays.clear()
                            selectedDays.addAll(listOf(1,2,3,4,5,6,7))
                        } else {
                            selectedDays.clear()
                        }
                    },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
            }

            // Days Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val days = listOf("S", "M", "T", "W", "T", "F", "S")
                days.forEachIndexed { index, day ->
                    val dayNum = index + 1
                    val isSelected = selectedDays.contains(dayNum)
                    
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF2C2C2E))
                            .clickable {
                                if (isSelected) selectedDays.remove(dayNum) else selectedDays.add(dayNum)
                                isDaily = selectedDays.size == 7
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(day, color = if (isSelected) Color.White else MaterialTheme.colorScheme.primary)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Mission Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Wake-up mission", color = Color.White, fontSize = 16.sp)
                        Text("0/5", color = Color.Gray)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Challenge Buttons
                        ChallengeOption(
                            type = ChallengeType.MATH, 
                            isSelected = selectedChallenge == ChallengeType.MATH,
                            onClick = { selectedChallenge = ChallengeType.MATH }
                        )
                        ChallengeOption(
                            type = ChallengeType.TYPING, 
                            isSelected = selectedChallenge == ChallengeType.TYPING,
                            onClick = { selectedChallenge = ChallengeType.TYPING }
                        )
                        ChallengeOption(
                            type = ChallengeType.SHAKE, 
                            isSelected = selectedChallenge == ChallengeType.SHAKE,
                            onClick = { selectedChallenge = ChallengeType.SHAKE }
                        )
                         ChallengeOption(
                            type = ChallengeType.QR, 
                            isSelected = selectedChallenge == ChallengeType.QR,
                            onClick = { selectedChallenge = ChallengeType.QR }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Save Button & Preview Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                 // Preview Button
                Button(
                    onClick = {
                        // Navigate to preview
                        navController.navigate(
                            "ringing?isPreview=true&challenge=${selectedChallenge.name}&startImmediate=false"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text("Preview", fontSize = 18.sp, color = Color.White)
                }

                // Save Button
                Button(
                    onClick = {
                        viewModel.addAlarm(selectedHour, selectedMinute, selectedChallenge, alarmName)
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text("Save", fontSize = 18.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ChallengeOption(type: ChallengeType, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha=0.3f) else Color(0xFF1C1C1E))
            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
         // Using Text for abbreviation instead of Icons for now
         val label = when(type) {
             ChallengeType.MATH -> "Math"
             ChallengeType.TYPING -> "Type"
             ChallengeType.SHAKE -> "Shake"
             ChallengeType.QR -> "QR"
             else -> "None"
         }
         Text(label, color = Color.White, fontSize = 12.sp)
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    count: Int,
    initialItem: Int,
    format: (Int) -> String,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = initialItem) { count }
    val context = LocalContext.current
    var lastPage by remember { mutableStateOf(initialItem) }
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
        soundPool.load(context, com.alarm.app.R.raw.tick, 1)
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

    // visibleItemsCount = 3 means 1 selected, 1 above, 1 below roughly
    // We need to ensure the height effectively handles this.
    // If ItemHeight is 60dp, and we want 3 items visible, total height should be ~180dp or constrained by parent.
    // The parent Row/Box constrains it.
    
    VerticalPager(
        state = pagerState,
        modifier = modifier.height(180.dp), // 3 * 60dp
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 60.dp) // padding to allow first/last item in center
    ) { page ->
        val isSelected = page == pagerState.currentPage
        
        // Snap effect happens automatically with Pager
        
        Box(
            modifier = Modifier
                .height(60.dp) // Match the highlight box height
                .fillMaxWidth(),
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

