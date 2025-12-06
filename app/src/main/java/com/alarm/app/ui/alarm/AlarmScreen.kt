package com.alarm.app.ui.alarm

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alarm.app.data.model.ChallengeType
import com.alarm.app.ui.AppViewModelProvider
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    navController: NavController,
    alarmId: String? = null,
    viewModel: AlarmViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val currentTime = Calendar.getInstance()
    val timeState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = false
    )

    var selectedChallenge by remember { mutableStateOf(ChallengeType.NONE) }
    
    // TODO: If alarmId is not null, load the alarm data (this logic requires ViewModel to support loading single alarm state)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (alarmId == null) "Add Alarm" else "Edit Alarm") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.addAlarm(timeState.hour, timeState.minute, selectedChallenge)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimeInput(state = timeState)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Wake Up Method",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ChallengeType.values().forEach { challenge ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedChallenge = challenge }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (selectedChallenge == challenge),
                        onClick = { selectedChallenge = challenge }
                    )
                    Text(
                        text = challenge.name.replace("_", " "),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    // Logic to preview challenge?
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Preview Challenge")
            }
        }
    }
}
