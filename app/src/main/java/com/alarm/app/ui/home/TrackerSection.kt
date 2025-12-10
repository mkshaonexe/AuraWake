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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
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
            .background(Color.Black) 
            .padding(vertical = 12.dp)
    ) {
        HeatmapGrid()
    }
}

@Composable
fun HeatmapGrid() {
    val rows = 9 // Hours 4..12
    
    // Data Generation (Dec to April)
    val monthsData = listOf(
        "Dec" to 31,
        "Jan" to 31,
        "Feb" to 28,
        "Mar" to 31,
        "Apr" to 30
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp) // Left padding
    ) {
        // Fixed Y-Axis (Hours)
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .height(130.dp) // Adjusted height
                .padding(top = 24.dp, end = 12.dp) // Top padding to align with grid below text, End padding gap
        ) {
            (4..12).forEach { hour ->
                Text(
                    text = hour.toString(), 
                    color = Color.Gray, 
                    fontSize = 10.sp
                )
            }
        }

        // Scrollable Grid (X-Axis)
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            monthsData.forEach { (monthName, days) ->
                items(days) { dayIndex ->
                    val day = dayIndex + 1
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Month Label (Only on 1st and maybe 15th for context, or just 1st as per image/request "dec... then next month")
                        // Image shows label centered-ish over the month? Or start? 
                        // Simplified: Show label on day 1 with some width or spacer.
                        // Or better: Just text if day == 1, else invisible text to keep height?
                        Box(modifier = Modifier.height(16.dp)) {
                            if (day == 1) {
                                Text(
                                    text = monthName,
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    modifier = Modifier.width(40.dp) // Ensure it doesn't wrap weirdly, overlays next cols visually 
                                )
                            }
                        }

                        // The vertical dots for this day
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(rows) { rowIndex ->
                                val isActive = remember(monthName, day, rowIndex) { 
                                    Random.nextFloat() > 0.7 
                                }
                                val activeColor = Color(0xFF26C6DA) // Teal
                                val inactiveColor = Color(0xFF1C1C1E) // Dark Grey

                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isActive) activeColor else inactiveColor)
                                )
                            }
                        }
                    }
                }
                // Spacer between months? 
                item {
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
        }
    }
}
