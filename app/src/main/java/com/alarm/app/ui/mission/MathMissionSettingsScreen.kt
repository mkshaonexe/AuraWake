package com.alarm.app.ui.mission

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.alarm.app.data.model.Difficulty
import com.alarm.app.ui.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathMissionSettingsScreen(
    navController: NavController,
    viewModel: MissionSettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var difficulty by remember { mutableStateOf(viewModel.mathDifficulty) }
    var problemCount by remember { mutableIntStateOf(viewModel.mathProblemCount) }

    LaunchedEffect(viewModel.mathDifficulty, viewModel.mathProblemCount) {
        difficulty = viewModel.mathDifficulty
        problemCount = viewModel.mathProblemCount
    }

    val previewProblem = remember(difficulty) {
        when (difficulty) {
            Difficulty.EASY -> "14 + 8 = ?"
            Difficulty.MEDIUM -> "45 - 12 + 8 = ?"
            Difficulty.HARD -> "12 x 8 + 5 = ?"
            Difficulty.VERY_HARD -> "(15 x 6) - 24 = ?"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Math Mission", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.saveMathSettings(difficulty, problemCount)
                        navController.popBackStack()
                    }) {
                        Text("Save", color = Color(0xFF6EC3F5), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Live Preview Card
            PreviewCard(problem = previewProblem)

            // Difficulty Section
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Difficulty Level", 
                    color = Color.Gray, 
                    fontSize = 14.sp, 
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp)
                )
                
                // Grid or Column of options? Column is cleaner for descriptions.
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DifficultyOption(
                        title = "Easy",
                        subtitle = "Simple arithmetic",
                        isSelected = difficulty == Difficulty.EASY,
                        onClick = { difficulty = Difficulty.EASY },
                        color = Color(0xFF4CAF50) // Green
                    )
                    DifficultyOption(
                        title = "Medium",
                        subtitle = "Mixed operations",
                        isSelected = difficulty == Difficulty.MEDIUM,
                        onClick = { difficulty = Difficulty.MEDIUM },
                        color = Color(0xFFFFCC00) // Yellow/Orange
                    )
                    DifficultyOption(
                        title = "Hard",
                        subtitle = "Multiplication & more",
                        isSelected = difficulty == Difficulty.HARD,
                        onClick = { difficulty = Difficulty.HARD },
                        color = Color(0xFFFF5252) // Red
                    )
                     DifficultyOption(
                        title = "Very Hard",
                        subtitle = "Complex equations",
                        isSelected = difficulty == Difficulty.VERY_HARD,
                        onClick = { difficulty = Difficulty.VERY_HARD },
                        color = Color(0xFF9C27B0) // Purple
                    )
                }
            }

            // Problem Count Section
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Problem Count", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(
                        "$problemCount Problems", 
                        color = Color.White, 
                        fontSize = 16.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Slider(
                    value = problemCount.toFloat(),
                    onValueChange = { problemCount = it.toInt() },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF6EC3F5),
                        activeTrackColor = Color(0xFF6EC3F5),
                        inactiveTrackColor = Color(0xFF2C2C2E)
                    ),
                    modifier = Modifier.height(20.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("1", color = Color.Gray, fontSize = 12.sp)
                    Text("10", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun PreviewCard(problem: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .border(1.dp, Color(0xFF2C2C2E), RoundedCornerShape(24.dp))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Calculate, 
                    contentDescription = null, 
                    tint = Color(0xFF6EC3F5),
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = problem,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "PREVIEW",
                    color = Color(0xFF6EC3F5),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
fun DifficultyOption(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "scale"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) color.copy(alpha = 0.15f) else Color(0xFF1C1C1E),
        label = "bgColor"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) color else Color.Transparent,
        label = "borderColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Disable default ripple for custom feel
                onClick = onClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(if(isSelected) color else Color.DarkGray)
        )
        
        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title, 
                color = if(isSelected) Color.White else Color.LightGray, 
                fontSize = 16.sp, 
                fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = subtitle, 
                color = if(isSelected) Color.White.copy(alpha=0.7f) else Color.Gray, 
                fontSize = 12.sp
            )
        }
    }
}
