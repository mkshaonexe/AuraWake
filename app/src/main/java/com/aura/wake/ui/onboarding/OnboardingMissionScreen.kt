package com.aura.wake.ui.onboarding

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aura.wake.data.model.ChallengeType

@Composable
fun OnboardingMissionScreen(
    viewModel: OnboardingViewModel,
    onNext: () -> Unit
) {
    var selectedChallenge by remember { mutableStateOf(viewModel.selectedChallenge) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = "Choose a wake-up mission",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Mission List
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MissionItem(
                title = "Math",
                icon = Icons.Default.Edit, // Placeholder
                isSelected = selectedChallenge == ChallengeType.MATH,
                onClick = { selectedChallenge = ChallengeType.MATH }
            )
             MissionItem(
                title = "Memory",
                icon = Icons.Default.Apps,
                isSelected = selectedChallenge == ChallengeType.MEMORY,
                onClick = { selectedChallenge = ChallengeType.MEMORY }
            )
            MissionItem(
                title = "Typing",
                icon = Icons.Default.Edit,
                isSelected = selectedChallenge == ChallengeType.TYPING,
                onClick = { selectedChallenge = ChallengeType.TYPING }
            )
            MissionItem(
                title = "Shake",
                icon = Icons.Default.PhoneAndroid,
                isSelected = selectedChallenge == ChallengeType.SHAKE,
                onClick = { selectedChallenge = ChallengeType.SHAKE }
            )
            MissionItem(
                title = "Off",
                icon = Icons.Default.Close,
                isSelected = selectedChallenge == ChallengeType.NONE,
                onClick = { selectedChallenge = ChallengeType.NONE }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // No button here? Usually user selects and it goes next or confirm.
        // Let's add Confirm button.
        // Actually, in the design, clicking an item might select it, then Next.
        
         Button(
            onClick = {
                viewModel.updateChallenge(selectedChallenge)
                onNext()
            },
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30)),
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
fun MissionItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color(0xFF2C2C2E), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFF38383A), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = if(isSelected) Color(0xFFFF3B30) else Color(0xFF6EC3F5))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, color = Color.White, fontSize = 16.sp)
        }
    }
}
