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
    // Reference: Rows for hours 4, 5, 6, ... 12 (Total 9 rows)
    val rows = 9
    val columns = 22

    Column(modifier = Modifier.fillMaxWidth()) {
        // Month Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 28.dp, bottom = 8.dp, end = 8.dp),
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
            // Hour Labels Column (4 to 12)
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .height(126.dp) // Height for 9 rows: (10dp dot + 4dp space) * 9 approx
                    .padding(end = 8.dp)
            ) {
                // Showing 4, 6, 8, 10, 12 to avoid clutter, or all if they fit. 
                // User asked for 4..12. Let's show representative ones or all if space permits small font.
                // Given the visual density, let's list them all but maybe skipping some text if needed?
                // Visual reference shows clear labels. Let's try 4..12
                val hours = (4..12).map { it.toString() }
                hours.forEach { hour ->
                     Text(hour, color = Color.Gray, fontSize = 10.sp)
                }
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
                            val isActive = remember { Random.nextFloat() > 0.6 } 
                            val activeColor = Color(0xFF26C6DA) 
                            val inactiveColor = Color(0xFF1C1C1E)

                            Box(
                                modifier = Modifier
                                    .size(10.dp) 
                                    .clip(RoundedCornerShape(3.dp)) 
                                    .background(if (isActive) activeColor else inactiveColor)
                            )
                        }
                    }
                }
            }
        }
    }
}
