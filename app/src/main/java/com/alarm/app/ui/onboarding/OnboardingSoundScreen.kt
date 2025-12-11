package com.alarm.app.ui.onboarding

import android.media.RingtoneManager
import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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

data class RingtoneItem(val name: String, val uri: String)

@Composable
fun OnboardingSoundScreen(
    viewModel: OnboardingViewModel,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var ringtones by remember { mutableStateOf<List<RingtoneItem>>(emptyList()) }
    var selectedSound by remember { mutableStateOf(viewModel.selectedSound) }
    
    // MediaPlayer for preview
    val mediaPlayer = remember { MediaPlayer() }
    
    // Cleanup MediaPlayer when screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            try {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playPreview(uriString: String) {
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.reset()
            mediaPlayer.setDataSource(context, android.net.Uri.parse(uriString))
            mediaPlayer.setAudioAttributes(
                android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            mediaPlayer.prepare()
            mediaPlayer.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Fetch system alarm ringtones
    LaunchedEffect(Unit) {
        try {
            val ringtoneManager = RingtoneManager(context)
            ringtoneManager.setType(RingtoneManager.TYPE_ALARM)
            val cursor = ringtoneManager.cursor
            val ringtoneList = mutableListOf<RingtoneItem>()
            
            while (cursor.moveToNext()) {
                val title = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)
                val uri = ringtoneManager.getRingtoneUri(cursor.position).toString()
                ringtoneList.add(RingtoneItem(title, uri))
            }
            
            ringtones = ringtoneList
            
            // Set default selection if none selected
            if (selectedSound.isEmpty() && ringtoneList.isNotEmpty()) {
                selectedSound = ringtoneList[0].name
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback or empty list
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Alarm tone",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Ringtone List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFF2C2C2E), RoundedCornerShape(16.dp))
        ) {
            item {
                Text(
                    "Standard",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
            items(ringtones.size) { index ->
                val ringtone = ringtones[index]
                SoundItem(
                    name = ringtone.name,
                    isSelected = selectedSound == ringtone.name,
                    onSelect = { 
                        selectedSound = ringtone.name
                        playPreview(ringtone.uri)
                    }
                )
                if (index < ringtones.size - 1) {
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                // Stop playing when proceeding
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                viewModel.updateSound(selectedSound)
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

@Composable
fun SoundItem(name: String, isSelected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFFFF3B30),
                unselectedColor = Color.Gray
            )
        )
        Text(
            text = name,
            color = Color.White,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
