package com.aura.wake.ui.alarm

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
import androidx.compose.material.icons.filled.MusicNote
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aura.wake.data.model.ChallengeType
import com.aura.wake.ui.AppViewModelProvider
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

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.TextButton
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class)
@Composable
fun AlarmScreen(
    navController: NavController,
    alarmId: String? = null,
    viewModel: AlarmViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    // Battery optimization permission state
    var showBatteryPermissionDialog by remember { mutableStateOf(false) }
    var hasBatteryPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val pm = context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
                pm.isIgnoringBatteryOptimizations(context.packageName)
            } else {
                true
            }
        )
    }

    val currentTime = Calendar.getInstance()
    var selectedHour by remember { mutableStateOf(currentTime.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(currentTime.get(Calendar.MINUTE)) }
    
    // For 12h format logic (UI only, data still 24h)
    // We do NOT store isPm as a separate state we sync manually. 
    // Instead we rely on selectedHour to drive everything.
    // However, the AM/PM picker needs a state to bind to.
    val isPm = selectedHour >= 12
    val amPmState = remember { mutableStateOf(if (isPm) 1 else 0) } // 0=AM, 1=PM
    
    // Sync amPmState when selectedHour changes externally (e.g. loading from DB)
    LaunchedEffect(selectedHour) {
         amPmState.value = if (selectedHour >= 12) 1 else 0
    }

    var selectedChallenge by remember { mutableStateOf(ChallengeType.NONE) }
    var alarmName by remember { mutableStateOf("") }
    val selectedDays = remember { mutableStateListOf<Int>().apply { addAll(listOf(1,2,3,4,5,6,7)) } } // 1=Sun, ..., 7=Sat - Default to all days
    var isDaily by remember { mutableStateOf(true) } // Default to Daily mode
    
    // Hoist ringtone state here so it's accessible in LaunchedEffect
    var selectedRingtoneUri by remember { mutableStateOf<String?>(null) }
    var selectedRingtoneTitle by remember { mutableStateOf<String?>(null) }
    
    // Retrieve result from navigation (moved here to keep related logic close, but observing SavedStateHandle needs context)
    val navBackStackEntry = navController.currentBackStackEntry
    val savedStateHandle = navBackStackEntry?.savedStateHandle
    
    // Observe result
    LaunchedEffect(savedStateHandle) {
         savedStateHandle?.getLiveData<String>("selected_ringtone_uri")?.observe(navBackStackEntry) { uri ->
             selectedRingtoneUri = uri
         }
         savedStateHandle?.getLiveData<String>("selected_ringtone_title")?.observe(navBackStackEntry) { title ->
             selectedRingtoneTitle = title
         }
    }

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

    // Load existing alarm if editing
    LaunchedEffect(alarmId) {
        if (alarmId != null) {
            val alarm = viewModel.getAlarm(alarmId)
            if (alarm != null) {
                selectedHour = alarm.hour
                selectedMinute = alarm.minute
                alarmName = alarm.label ?: ""
                selectedChallenge = alarm.challengeType
                selectedRingtoneUri = alarm.ringtoneUri
                selectedRingtoneTitle = alarm.ringtoneTitle
                
                // No need to manually set isPm or amPmState here, the LaunchedEffect(selectedHour) above will handle it.
            }
        }
    }
    
    // Battery Permission Dialog
    if (showBatteryPermissionDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBatteryPermissionDialog = false },
            title = {
                Text(
                    "Battery Permission Required",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Text(
                    "Without this permission, your device OS may kill the alarm app for battery optimization and alarms won't ring reliably.\n\nPlease grant background running permission to ensure your alarms work properly.",
                    color = Color.Gray,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBatteryPermissionDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val intent = Intent().apply {
                                action = android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                data = Uri.parse("package:${context.packageName}")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val fallback = Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(fallback)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30))
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatteryPermissionDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF2C2C2E)
        )
    }

    Scaffold(
        containerColor = Color(0xFF1C1C1E), // Dark Background
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (alarmId == null) "New Alarm" else "Edit Alarm", 
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2C2C2E))
                    .padding(horizontal = 16.dp, vertical = 2.dp)
            ) {
                androidx.compose.material3.TextField(
                    value = alarmName,
                    onValueChange = { alarmName = it },
                    placeholder = { Text("Alarm Name", color = Color.Gray, fontSize = 14.sp) },
                    colors = androidx.compose.material3.TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Wheel Time Picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFF1C1C1E)),
                contentAlignment = Alignment.Center
            ) {
                // Background highlight
                Box(modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2C2C2E)))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Calculate display hour for 12h format
                    val displayHour = if (selectedHour == 0) 12 else if (selectedHour > 12) selectedHour - 12 else selectedHour

                    WheelPicker(
                        count = 12,
                        initialItem = if (displayHour == 12) 11 else displayHour - 1, 
                        format = { (it + 1).toString().padStart(2, '0') },
                        onItemSelected = { idx -> 
                             val h = idx + 1
                             if (amPmState.value == 0) { 
                                 selectedHour = if (h == 12) 0 else h
                             } else { 
                                 selectedHour = if (h == 12) 12 else h + 12
                             }
                        },
                        modifier = Modifier.width(60.dp)
                    )
                    
                    Text(":", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                    
                    WheelPicker(
                        count = 60,
                        initialItem = selectedMinute,
                        format = { it.toString().padStart(2, '0') },
                        onItemSelected = { selectedMinute = it },
                        modifier = Modifier.width(60.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(16.dp))

                     WheelPicker(
                        count = 2,
                        initialItem = amPmState.value,
                        format = { if (it == 0) "AM" else "PM" },
                        onItemSelected = { idx -> 
                            amPmState.value = idx 
                            val currentH12 = if (selectedHour == 0) 12 else if (selectedHour > 12) selectedHour - 12 else selectedHour
                             if (idx == 0) { 
                                 selectedHour = if (currentH12 == 12) 0 else currentH12
                             } else { 
                                 selectedHour = if (currentH12 == 12) 12 else currentH12 + 12
                             }
                        },
                        modifier = Modifier.width(50.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = ringInString,
                color = Color.Gray,
                fontSize = 16.sp
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
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val days = listOf("S", "M", "T", "W", "T", "F", "S")
                days.forEachIndexed { index, day ->
                    val dayNum = index + 1
                    val isSelected = selectedDays.contains(dayNum)
                    
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF2C2C2E))
                            .clickable {
                                if (isSelected) selectedDays.remove(dayNum) else selectedDays.add(dayNum)
                                isDaily = selectedDays.size == 7
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(day, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = if (isSelected) Color.White else Color.Gray)
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
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Wake-up mission", color = Color.White, fontSize = 14.sp)
                        Text("0/5", color = Color.Gray, fontSize = 12.sp)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val modifier = Modifier.weight(1f)
                        ChallengeOption(
                            type = ChallengeType.MATH, 
                            isSelected = selectedChallenge == ChallengeType.MATH,
                            onClick = { selectedChallenge = ChallengeType.MATH },
                            modifier = modifier
                        )
                        ChallengeOption(
                            type = ChallengeType.TYPING, 
                            isSelected = selectedChallenge == ChallengeType.TYPING,
                            onClick = { selectedChallenge = ChallengeType.TYPING },
                            modifier = modifier
                        )
                        ChallengeOption(
                            type = ChallengeType.SHAKE, 
                            isSelected = selectedChallenge == ChallengeType.SHAKE,
                            onClick = { selectedChallenge = ChallengeType.SHAKE },
                            modifier = modifier
                        )
                        ChallengeOption(
                            type = ChallengeType.QR, 
                            isSelected = selectedChallenge == ChallengeType.QR,
                            onClick = { 
                                if (cameraPermissionState.status.isGranted) {
                                    selectedChallenge = ChallengeType.QR 
                                } else {
                                    cameraPermissionState.launchPermissionRequest()
                                }
                            },
                            modifier = modifier
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Alarm Sound Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("pick_ringtone") }
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Alarm Sound", color = Color.White, fontSize = 16.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            selectedRingtoneTitle ?: "Default", 
                            color = Color.Gray, 
                            fontSize = 14.sp
                        )
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp).rotate(180f))
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Save Button & Preview Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                 // Preview Button
                Button(
                    onClick = {
                        navController.navigate(
                            "ringing?isPreview=true&challenge=${selectedChallenge.name}&startImmediate=false"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E)),
                    shape = RoundedCornerShape(24.dp), // More rounded
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp) // Smaller height
                ) {
                    Text("Preview", fontSize = 16.sp, color = Color.White)
                }

                // Save Button
                Button(
                    onClick = {
                        // Check battery permission before saving
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val pm = context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
                            hasBatteryPermission = pm.isIgnoringBatteryOptimizations(context.packageName)
                        }
                        
                        if (!hasBatteryPermission) {
                            showBatteryPermissionDialog = true
                        } else {
                            if (alarmId == null) {
                                viewModel.addAlarm(
                                    selectedHour, 
                                    selectedMinute, 
                                    selectedChallenge, 
                                    alarmName,
                                    selectedRingtoneUri,
                                    selectedRingtoneTitle
                                )
                            } else {
                                viewModel.updateAlarmDetails(
                                    alarmId,
                                    selectedHour,
                                    selectedMinute,
                                    selectedChallenge,
                                    alarmName,
                                    selectedRingtoneUri,
                                    selectedRingtoneTitle
                                )
                            }
                            navController.popBackStack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
                    shape = RoundedCornerShape(24.dp), // More rounded
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp) // Smaller height
                ) {
                    Text("Save", fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ChallengeOption(type: ChallengeType, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(50.dp) // Fixed height, width flexible via weight
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha=0.3f) else Color(0xFF1C1C1E))
            .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
         val label = when(type) {
             ChallengeType.MATH -> "Math"
             ChallengeType.TYPING -> "Type"
             ChallengeType.SHAKE -> "Shake"
             ChallengeType.QR -> "QR"
             else -> "None"
         }
         Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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

    // Scroll to initialItem if it changes (e.g. when alarm data is loaded)
    LaunchedEffect(initialItem) {
        if (pagerState.currentPage != initialItem) {
            pagerState.scrollToPage(initialItem)
        }
    }

    // visibleItemsCount = 3 means 1 selected, 1 above, 1 below roughly
    // We need to ensure the height effectively handles this.
    // If ItemHeight is 60dp, and we want 3 items visible, total height should be ~180dp or constrained by parent.
    // The parent Row/Box constrains it.
    
    val height = 120.dp
    val itemHeight = 40.dp
    
    VerticalPager(
        state = pagerState,
        modifier = modifier.height(height),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = (height - itemHeight) / 2)
    ) { page ->
        val isSelected = page == pagerState.currentPage
        
        Box(
            modifier = Modifier
                .height(itemHeight)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = format(page),
                fontSize = if (isSelected) 24.sp else 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.4f),
                modifier = Modifier.alpha(if (isSelected) 1f else 0.4f)
            )
        }
    }
}

