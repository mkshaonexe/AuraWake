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
            .background(Color.Black, RoundedCornerShape(16.dp)) // Pure black background for the card area if needed, or match parent
            .padding(vertical = 12.dp)
    ) {
        HeatmapGrid()
    }
}

@Composable
fun HeatmapGrid() {
    // Reference image shows roughly 7 rows (Mon-Sun) and about 20-22 columns
    val rows = 7
    val columns = 22

    Column(modifier = Modifier.fillMaxWidth()) {
        // Month Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp, bottom = 8.dp, end = 8.dp), // Align with grid
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val months = listOf("Dec", "Jan", "Feb", "Mar", "Apr")
            months.forEach { month ->
                Text(
                    text = month, 
                    color = Color.Gray, 
                    fontSize = 10.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Day Labels Column
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .height(96.dp) // Match height of grid roughly (8dp size + 4dp space) * 7
                    .padding(end = 8.dp)
            ) {
                Text("Mon", color = Color.Gray, fontSize = 10.sp)
                Text("Wed", color = Color.Gray, fontSize = 10.sp)
                Text("Fri", color = Color.Gray, fontSize = 10.sp)
            }

            // Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(rows) { rowIndex ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(columns) { colIndex ->
                            // Randomize active state to mimic the scattered look
                            // In a real app, this would be based on actual data
                            val isActive = remember { Random.nextFloat() > 0.6 } 
                            
                            // Specific color from reference (Teal/Cyan) and Dark Grey
                            val activeColor = Color(0xFF26C6DA) // Cyan/Teal
                            val inactiveColor = Color(0xFF1C1C1E) // Dark Grey matching card backgrounds

                            Box(
                                modifier = Modifier
                                    .size(10.dp) // Slightly larger dots
                                    .clip(RoundedCornerShape(3.dp)) // Soft square
                                    .background(if (isActive) activeColor else inactiveColor)
                            )
                        }
                    }
                }
            }
        }
    }
}
