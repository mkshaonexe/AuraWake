package com.alarm.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

@Composable
fun TrackerSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D0D0D), RoundedCornerShape(16.dp)) // Very dark background
            .padding(12.dp) // Reduced padding
    ) {
        HeatmapGrid()
    }
}

@Composable
fun HeatmapGrid() {
    // Mock data: 7 rows (days), ~20 columns to fill space better when small
    val rows = 7
    val columns = 20

    Column {
        // Month Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, bottom = 6.dp), // Reduced offset and padding
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Dec", color = Color.Gray, fontSize = 9.sp)
            Text("Jan", color = Color.Gray, fontSize = 9.sp)
            Text("Feb", color = Color.Gray, fontSize = 9.sp)
            Text("Mar", color = Color.Gray, fontSize = 9.sp)
            Text("Apr", color = Color.Gray, fontSize = 9.sp)
        }

        Row {
            // Day Labels Column
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .height(80.dp) // Significantly reduced height
                    .padding(end = 6.dp)
            ) {
                Text("Mon", color = Color.Gray, fontSize = 9.sp)
                Text("Wed", color = Color.Gray, fontSize = 9.sp)
                Text("Fri", color = Color.Gray, fontSize = 9.sp)
            }

            // Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp) // Tighter vertical spacing
            ) {
                repeat(rows) { 
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp) // Tighter horizontal spacing
                    ) {
                        repeat(columns) { 
                            // Randomize active state
                            val isActive = remember { Random.nextFloat() > 0.7 } 
                            val color = if (isActive) Color(0xFF26C6DA) else Color(0xFF1E1E1E) // Teal vs Dark Grey

                            Box(
                                modifier = Modifier
                                    .size(8.dp) // Smaller dots
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(color)
                            )
                        }
                    }
                }
            }
        }
    }
}
