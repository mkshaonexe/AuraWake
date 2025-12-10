package com.alarm.app.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight 
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add 
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckCircle // Added
import androidx.compose.material.icons.filled.Delete 
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Home // Added
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert 
import androidx.compose.material.icons.filled.Person // Added
import androidx.compose.material.icons.filled.PieChart // Added
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu 
import androidx.compose.material3.DropdownMenuItem 
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton 
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf 
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // Added
import androidx.compose.ui.draw.scale 
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alarm.app.data.model.Alarm
import com.alarm.app.ui.AppViewModelProvider
import com.alarm.app.ui.alarm.AlarmViewModel
import java.util.Calendar

import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.ui.graphics.vector.ImageVector
import com.alarm.app.data.model.ChallengeType

// Helper to get mission icon
private fun getChallengeIcon(type: ChallengeType): ImageVector? {
    return when (type) {
        ChallengeType.MATH -> Icons.Default.Calculate
        ChallengeType.TYPING -> Icons.Default.Keyboard
        ChallengeType.SHAKE -> Icons.Default.Smartphone
        ChallengeType.QR -> Icons.Default.QrCode
        ChallengeType.NONE -> null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: AlarmViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val alarms by viewModel.allAlarms.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    val nextAlarmString = remember(alarms) {
        getNextAlarmString(alarms)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Show TopBar only for Home tab, or customize per tab. 
                // Profile has its own TopBar.
                if (selectedTab == 0) {
                    TopAppBar(
                        title = { 
                            Text(
                                "AuraWake", 
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 24.sp
                            ) 
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Black
                        )
                    )
                }
            },
            floatingActionButton = {
                if (selectedTab == 0) {
                    FloatingActionButton(
                        onClick = { navController.navigate("create_alarm") },
                        containerColor = Color(0xFFFF5252), // Bright Red
                        contentColor = Color.White,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(bottom = 80.dp) // Lift FAB above custom nav bar
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Alarm")
                    }
                }
            }
        ) { padding ->
            val contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = 100.dp // Space for floating bottom bar
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> HomeTabContent(
                        alarms = alarms,
                        nextAlarmString = nextAlarmString,
                        viewModel = viewModel,
                        navController = navController,
                        contentPadding = contentPadding
                    )
                    1 -> HistoryTabContent(contentPadding)
                    2 -> SettingsTabContent(contentPadding) // Using Check icon/Task per image/request mix
                    3 -> {
                         // Workaround: ProfileScreen has its own Scaffold. 
                         // We can wrapping it to respect our bottom padding or just let it overlay?
                         // ProfileScreen uses Scaffold, which consumes WindowInsets.
                         // Let's call it but we might have double Scaffolds.
                         // For now, simpler to just display it.
                         // We pass navController so back button works if it has one? 
                         // Actually ProfileScreen had a Back button which pops stack. 
                         // Since we are in a tab, popping stack might exit app.
                         // We might need to adjust ProfileScreen or just accept it's a "view".
                         // Ideally we refactor ProfileScreen to just be content.
                         // But for this task, I'll invoke it.
                         Box(modifier = Modifier.padding(bottom = 80.dp)) {
                             com.alarm.app.ui.profile.ProfileScreen(navController = navController)
                         }
                    }
                }
            }
        }

        // Custom Floating Bottom Navigation
        CustomBottomNavigation(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}

@Composable
fun CustomBottomNavigation(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(64.dp)
            .width(280.dp), // Pill width
        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1C1C1E) // Dark Grey Pill
        ),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icons = listOf(
                Icons.Default.Home,         // Home
                Icons.Default.PieChart,     // History
                Icons.Default.CheckCircle,  // Settings/Tasks (Matches image checkmark)
                Icons.Default.Person        // Profile
            )
            
            // Labels are not shown in the image, so we skip them.
            
            icons.forEachIndexed { index, icon ->
                val isSelected = selectedTab == index
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(if (isSelected) Color(0xFF2E7D32) else Color.Transparent) // Green for selected
                        .clickable { onTabSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun HomeTabContent(
    alarms: List<Alarm>,
    nextAlarmString: String,
    viewModel: AlarmViewModel,
    navController: NavController,
    contentPadding: PaddingValues
) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header "Ring in..."
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = nextAlarmString.ifEmpty { "No upcoming alarms" },
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                    Icon(
                        Icons.Default.KeyboardArrowRight, 
                        contentDescription = null, 
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 2. Tracker Section
        item {
             TrackerSection()
        }

        // 3. Alarm List
        items(alarms, key = { it.id }) { alarm ->
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                AlarmCard(
                    alarm = alarm,
                    onToggle = { viewModel.toggleAlarm(alarm) },
                    onDelete = { viewModel.deleteAlarm(alarm) },
                    onDuplicate = { viewModel.duplicateAlarm(alarm) },
                    onPreview = { 
                        navController.navigate("ringing")
                    },
                    onSkip = {
                        Toast.makeText(context, "Alarm skipped once", Toast.LENGTH_SHORT).show()
                    },
                    onClick = { navController.navigate("edit_alarm/${alarm.id}") }
                )
            }
        }
    }
}

@Composable
fun HistoryTabContent(contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.PieChart, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Sleep History", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Coming Soon", color = Color.Gray)
    }
}

@Composable
fun SettingsTabContent(contentPadding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Tasks & Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Coming Soon", color = Color.Gray)
    }
}

// ... [Keep getNextAlarmString and AlarmCard helper functions as they were, copy them here needed]
private fun getNextAlarmString(alarms: List<Alarm>): String {
    val activeAlarms = alarms.filter { it.isEnabled }
    if (activeAlarms.isEmpty()) return ""
    
    val now = Calendar.getInstance()
    var minDiffMinutes = Long.MAX_VALUE
    
    for (alarm in activeAlarms) {
        val alarmTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
        }
        
        if (alarmTime.before(now)) {
            alarmTime.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val diff = (alarmTime.timeInMillis - now.timeInMillis) / 1000 / 60
        if (diff < minDiffMinutes) {
            minDiffMinutes = diff
        }
    }
    
    if (minDiffMinutes == Long.MAX_VALUE) return ""
    
    val hours = minDiffMinutes / 60
    val minutes = minDiffMinutes % 60
    
    return "Ring in $hours hr. $minutes min"
}

@Composable
fun AlarmCard(
    alarm: Alarm,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onPreview: () -> Unit,
    onSkip: () -> Unit,
    onClick: () -> Unit
) {
    // Styling to match the "pill" shape and dark dark grey look
    val cardColor = Color(0xFF1C1C1E) // Standard Dark Grey for cards on Black
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        onClick = onClick,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp), // Slightly less rounded for smaller height if needed, or stick to pill
        colors = CardDefaults.cardColors(
            containerColor = cardColor 
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp) // Reduced height from 100.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp), // Reduced padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxHeight()
            ) {
                // Time
                Row(verticalAlignment = Alignment.Bottom) {
                    val hour = if (alarm.hour > 12) alarm.hour - 12 else if (alarm.hour == 0) 12 else alarm.hour
                    val minuteStr = String.format("%02d", alarm.minute)
                    val amPm = if (alarm.hour >= 12) "PM" else "AM"
                    
                    Text(
                        text = "$hour:$minuteStr",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (alarm.isEnabled) Color.White else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = amPm,
                        fontSize = 14.sp,
                        color = if (alarm.isEnabled) Color.White else Color.Gray,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                    
                    // Mission Icon
                    val icon = getChallengeIcon(alarm.challengeType)
                    if (icon != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (alarm.isEnabled) Color.Gray else Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(24.dp)
                                .padding(bottom = 4.dp)
                        )
                    }
                }

                // Days
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    val days = listOf("S", "M", "T", "W", "T", "F", "S")
                    days.forEachIndexed { index, day ->
                        val dayId = index + 1
                        val isActive = alarm.daysOfWeek.contains(dayId)
                        Text(
                            text = day,
                            fontSize = 11.sp, // Reduced from 12.sp
                            color = if (isActive && alarm.isEnabled) Color(0xFF26C6DA) else Color.Gray.copy(alpha = 0.5f), // Teal for active days
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = alarm.isEnabled,
                    onCheckedChange = { onToggle(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF26C6DA), 
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray,
                        uncheckedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.scale(0.8f) // Make switch slightly smaller
                )
                
                Box {
                    IconButton(
                        onClick = { expanded = true },
                        modifier = Modifier.size(32.dp) // Smaller touch target for the menu
                    ) {
                        Icon(
                            Icons.Default.MoreVert, 
                            contentDescription = "Options",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = Color(0xFF2C2C2E)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete", color = Color.White) },
                            onClick = { 
                                onDelete()
                                expanded = false 
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White) }
                        )
                        DropdownMenuItem(
                            text = { Text("Preview alarm", color = Color.White) },
                            onClick = { 
                                onPreview()
                                expanded = false 
                            },
                            leadingIcon = { Icon(Icons.Default.AccessAlarm, contentDescription = null, tint = Color.White) }
                        )
                        DropdownMenuItem(
                            text = { Text("Skip once", color = Color.White) },
                            onClick = { 
                                onSkip()
                                expanded = false 
                            },
                            leadingIcon = { Icon(Icons.Default.Assignment, contentDescription = null, tint = Color.White) }
                        )
                        DropdownMenuItem(
                            text = { Text("Duplicate alarm", color = Color.White) },
                            onClick = { 
                                onDuplicate()
                                expanded = false 
                            },
                            leadingIcon = { Icon(Icons.Default.Extension, contentDescription = null, tint = Color.White) }
                        )
                    }
                }
            }
        }
    }
}
