package com.alarm.app.ui.profile

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.cos
import kotlin.math.sin

// Theme Colors
val DarkBackground = Color(0xFF0D0D15)
val CardBackground = Color(0xFF161622) // Slightly lighter for cards
val PrimaryPurple = Color(0xFFBB86FC)
val DeepPurple = Color(0xFF6200EE)
val AccentPink = Color(0xFFCF6679)
val TextWhite = Color.White
val TextSecondary = Color.Gray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Agent Stats", color = TextWhite, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Radar Chart (Stats)
            item {
                RadarStatCard()
            }

            // 2. Skill Points
            item {
               SkillPointsCard()
            }

            // 3. Bar Chart
            item {
                ActivityBarChartCard()
            }

            // 4. XP / Goal
            item {
                XPCard()
            }

            // 5. Contribution Graph (Full Width)
            item(span = { GridItemSpan(2) }) {
                ContributionGraphCard()
            }
        }
    }
}

@Composable
fun ProfileCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A35))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

// ... [Keep other composables as they are, assume they are unchanged if not targeted] ...

@Composable
fun ContributionGraphCard() {
    ProfileCard {
        // Use the shared component from Home
        com.alarm.app.ui.home.ContributionHeatmap()
    }
}

@Composable
fun SkillPointsCard() {
    ProfileCard(modifier = Modifier.height(220.dp)) {
        Text("Skill Points", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        val skills = listOf(
            "Writing" to 0.7f,
            "Financial" to 0.4f,
            "Learning" to 0.9f
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            skills.forEach { (name, progress) ->
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(), 
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(name, color = TextSecondary, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = PrimaryPurple,
                        trackColor = Color(0xFF2A2A35),
                        strokeCap = StrokeCap.Round,
                    )
                }
            }
        }
    }
}


@Composable
fun AvatarSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp)
        ) {
            // Decorative Rings
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2

                // Outer decorative ring
                drawCircle(
                    color = PrimaryPurple.copy(alpha = 0.3f),
                    radius = radius,
                    style = Stroke(width = 2.dp.toPx())
                )
                
                // Inner segmented ring simulation
                drawCircle(
                    color = DeepPurple,
                    radius = radius - 10.dp.toPx(),
                    style = Stroke(width = 4.dp.toPx())
                )
            }
            
            // Avatar Placeholder
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2C2C2E))
                    .border(2.dp, PrimaryPurple, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                 // In a real app, this would be an Image
                 Text("MK", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Shadow Agent", color = TextWhite, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("Level 42", color = PrimaryPurple, fontSize = 14.sp)
    }
}

@Composable
fun RadarStatCard() {
    ProfileCard(modifier = Modifier.height(220.dp)) {
        Text("Stats", color = TextSecondary, fontSize = 12.sp)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2 * 0.8f
                val stats = listOf(0.8f, 0.6f, 0.9f, 0.5f, 0.7f, 0.8f) // 6 stats
                val sides = 6
                
                // Draw Web
                for (i in 1..4) {
                    val r = radius * (i / 4f)
                    drawPath(
                        path = createPolygonPath(center, r, sides),
                        color = Color.Gray.copy(alpha = 0.3f),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Draw Data
                val dataPath = Path()
                stats.forEachIndexed { index, value ->
                    val angle = (2 * Math.PI / sides) * index - Math.PI / 2
                    val r = radius * value
                    val x = center.x + r * cos(angle).toFloat()
                    val y = center.y + r * sin(angle).toFloat()
                    
                    if (index == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
                }
                dataPath.close()
                
                drawPath(
                    path = dataPath,
                    color = PrimaryPurple.copy(alpha = 0.5f)
                )
                drawPath(
                    path = dataPath,
                    color = PrimaryPurple,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            
            // Labels (Simplified position)
            // In a real optimized view, we'd calculate exact label positions
        }
    }
}

fun createPolygonPath(center: Offset, radius: Float, sides: Int): Path {
    val path = Path()
    val angleStep = (2 * Math.PI / sides)
    
    for (i in 0 until sides) {
        val angle = angleStep * i - Math.PI / 2
        val x = center.x + radius * cos(angle).toFloat()
        val y = center.y + radius * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}



@Composable
fun ActivityBarChartCard() {
     ProfileCard(modifier = Modifier.height(200.dp)) { // Matched height roughly
        Text("Activity", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            val bars = listOf(0.4f, 0.8f, 0.5f, 0.9f)
            bars.forEach { value ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .height(100.dp * value) // Scale height
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(PrimaryPurple, DeepPurple)
                                )
                            )
                    )
                }
            }
        }
        Text(
            "Levels", 
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
            color = TextSecondary, 
            fontSize = 10.sp
        )
    }
}

@Composable
fun XPCard() {
    ProfileCard(modifier = Modifier.height(200.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("40% ", color = TextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("XP Goal", color = TextSecondary)
        }
        Spacer(modifier = Modifier.height(24.dp))
        
        // Custom segmented progress bar simulation
        Box(modifier = Modifier.fillMaxWidth().height(16.dp)) {
             Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2A2A35))
             )
             Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryPurple)
             )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        Text("695 / 2000", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}


