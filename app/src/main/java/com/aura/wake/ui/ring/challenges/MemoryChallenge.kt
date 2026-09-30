package com.aura.wake.ui.ring.challenges

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
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
import com.aura.wake.data.model.Difficulty
import kotlinx.coroutines.delay

enum class MemoryPhase {
    SHOWING,
    INPUT,
    WRONG
}

@Composable
fun MemoryChallenge(
    difficulty: Difficulty = Difficulty.MEDIUM,
    questionCount: Int = 3,
    onCompleted: () -> Unit,
    onClose: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val vibrator = remember {
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun vibrateClick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (e: Exception) {}
    }

    fun vibrateError() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(200)
            }
        } catch (e: Exception) {}
    }

    // Grid config according to difficulty
    val gridSize = when (difficulty) {
        Difficulty.EASY -> 4
        Difficulty.MEDIUM -> 5
        Difficulty.HARD -> 6
        Difficulty.VERY_HARD -> 6
    }

    val patternLength = when (difficulty) {
        Difficulty.EASY -> 4
        Difficulty.MEDIUM -> 6
        Difficulty.HARD -> 8
        Difficulty.VERY_HARD -> 10
    }

    val showDurationMs = when (difficulty) {
        Difficulty.VERY_HARD -> 2000L
        else -> 2500L
    }

    var currentQuestion by remember { mutableIntStateOf(0) }
    var phase by remember { mutableStateOf(MemoryPhase.SHOWING) }
    var pattern by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var tappedIndices by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var wrongIndex by remember { mutableStateOf<Int?>(null) }

    // Generate new pattern whenever question changes
    LaunchedEffect(currentQuestion) {
        val totalTiles = gridSize * gridSize
        pattern = (0 until totalTiles).shuffled().take(patternLength).toSet()
        tappedIndices = emptySet()
        wrongIndex = null
        phase = MemoryPhase.SHOWING

        delay(showDurationMs)
        phase = MemoryPhase.INPUT
    }

    // Handle wrong retry
    LaunchedEffect(phase) {
        if (phase == MemoryPhase.WRONG) {
            delay(1200)
            wrongIndex = null
            tappedIndices = emptySet()
            phase = MemoryPhase.SHOWING
            delay(showDurationMs)
            phase = MemoryPhase.INPUT
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141416))
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onClose != null) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }

            // Question counter
            Text(
                text = "${currentQuestion + 1} / $questionCount",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Difficulty tag
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2C2C2E))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = when (difficulty) {
                        Difficulty.EASY -> "Easy"
                        Difficulty.MEDIUM -> "Normal"
                        Difficulty.HARD -> "Hard"
                        Difficulty.VERY_HARD -> "V. Hard"
                    },
                    color = Color(0xFFE5C158),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Phase Status & Instructions
        Text(
            text = when (phase) {
                MemoryPhase.SHOWING -> "Memorize the pattern"
                MemoryPhase.INPUT -> "Tap the tiles you memorized"
                MemoryPhase.WRONG -> "Wrong tile! Re-memorize"
            },
            color = when (phase) {
                MemoryPhase.SHOWING -> Color(0xFFE5C158)
                MemoryPhase.INPUT -> Color.White
                MemoryPhase.WRONG -> Color(0xFFFF3B30)
            },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when (phase) {
                MemoryPhase.SHOWING -> "Remember the highlighted tiles"
                MemoryPhase.INPUT -> "${tappedIndices.size} of ${pattern.size} found"
                MemoryPhase.WRONG -> "Pattern will replay in a moment..."
            },
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.weight(1f))

        // The Tile Grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E1E20))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (row in 0 until gridSize) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0 until gridSize) {
                            val index = row * gridSize + col
                            val isPatternTile = pattern.contains(index)
                            val isTapped = tappedIndices.contains(index)
                            val isWrongTile = wrongIndex == index

                            val targetTileColor = when {
                                isWrongTile -> Color(0xFFFF3B30)
                                phase == MemoryPhase.SHOWING && isPatternTile -> Color(0xFFE5C158)
                                phase == MemoryPhase.INPUT && isTapped -> Color(0xFFE5C158)
                                phase == MemoryPhase.WRONG && isPatternTile -> Color(0xFFE5C158).copy(alpha = 0.5f)
                                else -> Color(0xFF323236)
                            }

                            val animatedTileColor by animateColorAsState(
                                targetValue = targetTileColor,
                                animationSpec = tween(durationMillis = 200),
                                label = "tileColor"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(animatedTileColor)
                                    .clickable(enabled = phase == MemoryPhase.INPUT && !isTapped) {
                                        if (isPatternTile) {
                                            vibrateClick()
                                            val newTapped = tappedIndices + index
                                            tappedIndices = newTapped
                                            if (newTapped.size == pattern.size) {
                                                // Completed current question
                                                if (currentQuestion + 1 < questionCount) {
                                                    currentQuestion++
                                                } else {
                                                    onCompleted()
                                                }
                                            }
                                        } else {
                                            vibrateError()
                                            wrongIndex = index
                                            phase = MemoryPhase.WRONG
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom hint
        Text(
            text = "Alarm will dismiss once all questions are completed",
            color = Color.DarkGray,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
    }
}
