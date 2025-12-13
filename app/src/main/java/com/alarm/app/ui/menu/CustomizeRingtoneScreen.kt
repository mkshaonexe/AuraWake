package com.alarm.app.ui.menu

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.alarm.app.ui.AppViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class RingtoneItem(
    val title: String,
    val uri: Uri,
    val isCustom: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizeRingtoneScreen(navController: NavController) {
    val context = LocalContext.current
    val settingsRepository = (context.applicationContext as com.alarm.app.AlarmApplication).container.settingsRepository
    
    // State
    var selectedRingtoneUri by remember { mutableStateOf<Uri?>(null) }
    var systemRingtones by remember { mutableStateOf<List<RingtoneItem>>(emptyList()) }
    var isPlaying by remember { mutableStateOf(false) }
    var playingUri by remember { mutableStateOf<Uri?>(null) }
    
    // MediaPlayer
    val mediaPlayer = remember { MediaPlayer() }
    
    // Load saved ringtone
    LaunchedEffect(Unit) {
        val savedUriStr = settingsRepository.getDefaultRingtoneUri()
        selectedRingtoneUri = savedUriStr?.let { Uri.parse(it) } ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        
        // Fetch System Ringtones
        withContext(Dispatchers.IO) {
            val ringtoneManager = RingtoneManager(context)
            ringtoneManager.setType(RingtoneManager.TYPE_ALARM)
            val cursor = ringtoneManager.cursor
            val list = mutableListOf<RingtoneItem>()
            
            while (cursor.moveToNext()) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uriStr = cursor.getString(RingtoneManager.URI_COLUMN_INDEX)
                val id = cursor.getString(RingtoneManager.ID_COLUMN_INDEX)
                val uri = Uri.parse("$uriStr/$id")
                list.add(RingtoneItem(title, uri))
            }
            systemRingtones = list
        }
    }
    
    // Helper to play ringtone
    fun playRingtone(uri: Uri) {
        try {
            if (isPlaying) {
                mediaPlayer.stop()
                mediaPlayer.reset()
                if (playingUri == uri) {
                    isPlaying = false
                    playingUri = null
                    return
                }
            }
            
            mediaPlayer.setDataSource(context, uri)
            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            mediaPlayer.prepare()
            mediaPlayer.start()
            isPlaying = true
            playingUri = uri
            
            mediaPlayer.setOnCompletionListener {
                isPlaying = false
                playingUri = null
                mediaPlayer.reset()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isPlaying = false
            playingUri = null
            mediaPlayer.reset()
        }
    }
    
    // Clean up
    DisposableEffect(Unit) {
        onDispose {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        }
    }
    
    // Custom File Picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Persist permission if needed (usually just saving string URI works fine for openable intent)
            // But for alarm, we might need persistent read permission.
             try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Ignore if not possible
            }
            
            selectedRingtoneUri = it
            settingsRepository.saveDefaultRingtoneUri(it.toString())
            playRingtone(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Customize Ringtone", 
                        fontWeight = FontWeight.Bold, 
                        color = Color.White
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Back", 
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Custom Button
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { launcher.launch("audio/*") }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C2C2E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.LightGray)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Select Custom Tone", color = Color.White, fontWeight = FontWeight.Medium)
                }
            }
            
            // System Ringtones List
            Text(
                "SYSTEM RINGTONES",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(systemRingtones) { ringtone ->
                    val isSelected = selectedRingtoneUri == ringtone.uri
                    val isPlayingThis = isPlaying && playingUri == ringtone.uri
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedRingtoneUri = ringtone.uri
                                settingsRepository.saveDefaultRingtoneUri(ringtone.uri.toString())
                                playRingtone(ringtone.uri)
                            }
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                         Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFF26A641) else Color(0xFF2C2C2E)),
                            contentAlignment = Alignment.Center
                        ) {
                             if (isPlayingThis) {
                                  Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White)
                             } else {
                                  Icon(if (isSelected) Icons.Default.MusicNote else Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                             }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Text(
                            ringtone.title, 
                            color = if (isSelected) Color(0xFF26A641) else Color.White, 
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF26A641))
                        }
                    }
                    Divider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(start = 72.dp))
                }
            }
        }
    }
}
