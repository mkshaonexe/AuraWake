package com.aura.wake.ui.mission

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.aura.wake.data.model.Difficulty
import com.aura.wake.ui.AppViewModelProvider
import com.aura.wake.ui.ring.challenges.MemoryChallenge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryMissionSettingsScreen(
    navController: NavController,
    viewModel: MissionSettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var difficulty by remember { mutableStateOf(viewModel.memoryDifficulty) }
    var questionCount by remember { mutableIntStateOf(viewModel.memoryQuestionCount) }
    var showPreview by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.memoryDifficulty, viewModel.memoryQuestionCount) {
        difficulty = viewModel.memoryDifficulty
        questionCount = viewModel.memoryQuestionCount
    }

    fun updateDifficulty(newDiff: Difficulty) {
        difficulty = newDiff
        viewModel.saveMemorySettings(newDiff, questionCount)
    }

    fun updateQuestionCount(newCount: Int) {
        val clamped = newCount.coerceIn(1, 10)
        questionCount = clamped
        viewModel.saveMemorySettings(difficulty, clamped)
    }

    val flameCount = when (difficulty) {
        Difficulty.EASY -> 2
        Difficulty.MEDIUM -> 3
        Difficulty.HARD -> 4
        Difficulty.VERY_HARD -> 5
    }

    val difficultyDisplayName = when (difficulty) {
        Difficulty.EASY -> "Easy"
        Difficulty.MEDIUM -> "Normal"
        Difficulty.HARD -> "Hard"
        Difficulty.VERY_HARD -> "Very Hard"
    }

    // Grid config for example box
    val gridSize = when (difficulty) {
        Difficulty.EASY -> 4
        Difficulty.MEDIUM -> 5
        Difficulty.HARD -> 6
        Difficulty.VERY_HARD -> 6
    }

    val activeTilesCount = when (difficulty) {
        Difficulty.EASY -> 4
        Difficulty.MEDIUM -> 6
        Difficulty.HARD -> 8
        Difficulty.VERY_HARD -> 10
    }

    // Generate static preview pattern for the example card
    val examplePattern = remember(difficulty) {
        val total = gridSize * gridSize
        (0 until total).shuffled().take(activeTilesCount).toSet()
    }

    if (showPreview) {
        // Full interactive preview modal
        Box(modifier = Modifier.fillMaxSize()) {
            MemoryChallenge(
                difficulty = difficulty,
                questionCount = questionCount,
                onCompleted = { showPreview = false },
                onClose = { showPreview = false }
            )
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Memory",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            viewModel.saveMemorySettings(difficulty, questionCount)
                            navController.popBackStack()
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Navigate up",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        TextButton(onClick = { showPreview = true }) {
                            Text(
                                "PREVIEW",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
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
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Example Box with Flames on top
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Main Example Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 28.dp, bottom = 24.dp, start = 20.dp, end = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Example",
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Grid container
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    for (r in 0 until gridSize) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            for (c in 0 until gridSize) {
                                                val index = r * gridSize + c
                                                val isLit = examplePattern.contains(index)
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .fillMaxHeight()
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(
                                                            if (isLit) Color(0xFFE5C158) else Color(0xFF3A3A3C)
                                                        )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Floating Flame Capsule
                    Surface(
                        modifier = Modifier
                            .wrapContentSize(),
                        shape = RoundedCornerShape(50),
                        color = Color(0xFF252528),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 1..5) {
                                val isLit = i <= flameCount
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = if (isLit) Color(0xFFFF453A) else Color(0xFF48484A),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Number of Questions Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Number of Questions",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(0.6f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = { updateQuestionCount(questionCount - 1) },
                            shape = CircleShape,
                            color = Color(0xFF2C2C2E),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Remove,
                                    contentDescription = "Decrease",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Text(
                            text = questionCount.toString(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Surface(
                            onClick = { updateQuestionCount(questionCount + 1) },
                            shape = CircleShape,
                            color = Color(0xFF2C2C2E),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Increase",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Difficulty Level Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "Difficulty Level",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(0.7f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            val previous = when (difficulty) {
                                Difficulty.VERY_HARD -> Difficulty.HARD
                                Difficulty.HARD -> Difficulty.MEDIUM
                                Difficulty.MEDIUM -> Difficulty.EASY
                                Difficulty.EASY -> Difficulty.EASY
                            }
                            updateDifficulty(previous)
                        }) {
                            Icon(
                                Icons.Default.ArrowLeft,
                                contentDescription = "Previous difficulty",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = difficultyDisplayName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        IconButton(onClick = {
                            val next = when (difficulty) {
                                Difficulty.EASY -> Difficulty.MEDIUM
                                Difficulty.MEDIUM -> Difficulty.HARD
                                Difficulty.HARD -> Difficulty.VERY_HARD
                                Difficulty.VERY_HARD -> Difficulty.VERY_HARD
                            }
                            updateDifficulty(next)
                        }) {
                            Icon(
                                Icons.Default.ArrowRight,
                                contentDescription = "Next difficulty",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
