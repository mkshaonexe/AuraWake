package com.aura.wake.ui.ring.challenges

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun MathChallenge(
    difficulty: com.aura.wake.data.model.Difficulty = com.aura.wake.data.model.Difficulty.EASY,
    problemCount: Int = 3,
    onCompleted: () -> Unit
) {
    var problemsSolved by remember { mutableStateOf(0) }
    
    // Generate new numbers when a problem is solved (key changes)
    val problemState = remember(problemsSolved) {
        generateProblem(difficulty)
    }
    
    val num1 = problemState.first
    val num2 = problemState.second
    val num3 = problemState.third // For hard/very hard
    val operation = problemState.fourth // +, -, *
    
    val correctAnswer = calculateAnswer(num1, num2, num3, operation, difficulty)

    var answer by remember { mutableStateOf("") }
    var shakeError by remember { mutableStateOf(false) }

    // If all solved, complete
    LaunchedEffect(problemsSolved) {
        if (problemsSolved >= problemCount) {
            onCompleted()
        }
    }

    if (problemsSolved < problemCount) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1C1C1E))
                .padding(16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Handle back if needed */ }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text("${problemsSolved + 1} / $problemCount", color = Color.Gray, fontSize = 16.sp)
                IconButton(onClick = { /* Toggle Mute */ }) {
                    Icon(Icons.Default.VolumeOff, contentDescription = "Mute", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Equation Display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formatProblem(num1, num2, num3, operation, difficulty),
                    fontSize = 40.sp, // Slightly smaller to fit "Hard" equations
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Answer Box
                Surface(
                    color = Color(0xFF2C2C2E),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(2.dp, if (shakeError) Color.Red else Color.White),
                    modifier = Modifier
                        .width(200.dp)
                        .height(60.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                         Text(
                            text = answer,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Custom Numpad
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val rows = listOf(
                    listOf("7", "8", "9"),
                    listOf("4", "5", "6"),
                    listOf("1", "2", "3")
                )

                rows.forEach { row ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        row.forEach { digit ->
                            NumPadButton(
                                text = digit,
                                modifier = Modifier.weight(1f).height(70.dp),
                                onClick = { 
                                    if (answer.length < 5) answer += digit 
                                    shakeError = false
                                }
                            )
                        }
                    }
                }
                
                // Bottom Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Backspace
                    Surface(
                        onClick = { if (answer.isNotEmpty()) answer = answer.dropLast(1) },
                        color = Color(0xFF2C2C2E),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(70.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Backspace, contentDescription = "Delete", tint = Color.White)
                        }
                    }
                    
                    // 0
                    NumPadButton(
                        text = "0",
                        modifier = Modifier.weight(1f).height(70.dp),
                        onClick = { 
                            if (answer.isNotEmpty() && answer.length < 5) answer += "0" 
                        }
                    )
                    
                    // Check (Submit)
                    Surface(
                        onClick = {
                            if (answer.toIntOrNull() == correctAnswer) {
                                problemsSolved++
                                answer = ""
                            } else {
                                shakeError = true
                                answer = ""
                            }
                        },
                        color = Color(0xFFFF3B30),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).height(70.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, contentDescription = "Submit", tint = Color.White)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
             Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("Solve to dismiss", color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

fun generateProblem(difficulty: com.aura.wake.data.model.Difficulty): Quad<Int, Int, Int, String> {
    return when (difficulty) {
        com.aura.wake.data.model.Difficulty.EASY -> {
            Quad(Random.nextInt(1, 20), Random.nextInt(1, 20), 0, "+")
        }
        com.aura.wake.data.model.Difficulty.MEDIUM -> {
             // A + B - C or simple A + B with larger numbers
             val op = if(Random.nextBoolean()) "+" else "-"
             val n1 = Random.nextInt(20, 100)
             val n2 = Random.nextInt(10, 50)
             
             // Ensure positive result for subtraction
             if (op == "-" && n2 > n1) {
                 Quad(n2, n1, 0, op) // Swap them: Large - Small
             } else {
                 Quad(n1, n2, 0, op)
             }
        }
        com.aura.wake.data.model.Difficulty.HARD -> {
             // A * B + C
             Quad(Random.nextInt(5, 15), Random.nextInt(5, 12), Random.nextInt(1, 20), "*+")
        }
        com.aura.wake.data.model.Difficulty.VERY_HARD -> {
             // (A * B) - C
             val n1 = Random.nextInt(10, 20)
             val n2 = Random.nextInt(5, 15)
             val product = n1 * n2
             // Ensure n3 is smaller than product to keep result positive
             val maxN3 = product - 1
             val n3 = Random.nextInt(10, if (maxN3 > 10) maxN3 else 11)
             
             Quad(n1, n2, n3, "*-")
        }
    }
}

fun calculateAnswer(n1: Int, n2: Int, n3: Int, op: String, diff: com.aura.wake.data.model.Difficulty): Int {
    return when (diff) {
        com.aura.wake.data.model.Difficulty.EASY -> n1 + n2
        com.aura.wake.data.model.Difficulty.MEDIUM -> if (op == "+") n1 + n2 else n1 - n2
        com.aura.wake.data.model.Difficulty.HARD -> (n1 * n2) + n3
        com.aura.wake.data.model.Difficulty.VERY_HARD -> (n1 * n2) - n3
    }
}

fun formatProblem(n1: Int, n2: Int, n3: Int, op: String, diff: com.aura.wake.data.model.Difficulty): String {
    return when (diff) {
        com.aura.wake.data.model.Difficulty.EASY -> "$n1 + $n2 ="
        com.aura.wake.data.model.Difficulty.MEDIUM -> if (op == "+") "$n1 + $n2 =" else "$n1 - $n2 ="
        com.aura.wake.data.model.Difficulty.HARD -> "$n1 x $n2 + $n3 ="
        com.aura.wake.data.model.Difficulty.VERY_HARD -> "( $n1 x $n2 ) - $n3 ="
    }
}

data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun NumPadButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color(0xFF2C2C2E),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
