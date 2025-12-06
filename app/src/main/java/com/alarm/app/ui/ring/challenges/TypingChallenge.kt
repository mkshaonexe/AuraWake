package com.alarm.app.ui.ring.challenges

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypingChallenge(
    onCompleted: () -> Unit
) {
    val phrases = listOf(
        "Live with joy",
        "Practice makes perfect",
        "Action speaks louder",
        "Time is money"
    )
    val targetPhrase = remember { phrases.random() }
    var userText by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
         // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* Handle back */ }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text("1 / 2", color = Color.Gray, fontSize = 16.sp)
             IconButton(onClick = { /* Toggle Mute */ }) {
                Icon(Icons.Default.VolumeOff, contentDescription = "Mute", tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Text Display Area
        // Logic: Show targetPhrase. Typed characters should match. 
        // We can color correct chars Green, incorrect Red, remaining Gray/White.
        // Or simplified as per screenshot: White text, maybe typed part highlighted? 
        // Let's implement simple "Current typed vs Target" diff visual.
        
        Text(
            text = buildAnnotatedString {
                // Typed part
                withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                    append(userText)
                }
                // Remaining part
                if (userText.length < targetPhrase.length) {
                    withStyle(style = SpanStyle(color = Color.Gray.copy(alpha=0.5f), fontWeight = FontWeight.Bold)) {
                        append(targetPhrase.substring(userText.length))
                    }
                }
            },
            fontSize = 32.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        // Cursor indicator (Blue drop in screenshot, simplified here as a blinker or nothing)
        
        Spacer(modifier = Modifier.height(32.dp))

        // Invisible input field to capture typing
        BasicTextField(
            value = userText,
            onValueChange = { newValue ->
                if (newValue.length <= targetPhrase.length) {
                    userText = newValue
                }
            },
            modifier = Modifier
                .size(1.dp)
                .focusRequester(focusRequester), // Attached correctly to modifier
            cursorBrush = SolidColor(Color.Transparent),
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.Transparent),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { 
                 if (userText.equals(targetPhrase, ignoreCase = true)) {
                    onCompleted()
                 }
            })
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Progress / Count
        Text(
            text = "${userText.length} / ${targetPhrase.length}",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        // Complete Button
        val isComplete = userText.equals(targetPhrase, ignoreCase = true)
        Button(
            onClick = {
                if (isComplete) {
                    onCompleted()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isComplete) MaterialTheme.colorScheme.primary else Color(0xFF2C2C2E),
                contentColor = if (isComplete) Color.White else Color.Gray
            )
        ) {
            Text("Complete", fontSize = 18.sp)
        }
        
        Spacer(modifier = Modifier.height(16.dp)) // Space for keyboard
    }
}
