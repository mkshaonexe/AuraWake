package com.alarm.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
    val rows = 24 // 24 Hours
    val startHour = 4
    
    // Data Generation (Dec to April)
    val monthsData = listOf(
        "Dec" to 31,
        "Jan" to 31,
        "Feb" to 28,
        "Mar" to 31,
        "Apr" to 30
    )

    // Outer Container with FIXED height that allows Vertical Scrolling
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp) // Fixed height window
            .verticalScroll(androidx.compose.foundation.rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
        ) {
            // Fixed Y-Axis (Hours) - Scolls with the grid
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp), // Match grid spacing
                modifier = Modifier
                    .padding(top = 22.dp, end = 12.dp) // Align with grid start (approx text height offset)
            ) {
                repeat(rows) { i ->
                    val hour = (startHour + i) % 24
                    // Formatting to avoid 0 being 24? 0 is 0.
                    // Just showing numbers.
                    // Text needs to align with the dots. 10dp dot. Text usually ~14-16sp height.
                    // We might need to adjust alignment.
                    Box(modifier = Modifier.height(10.dp), contentAlignment = androidx.compose.ui.Alignment.CenterEnd) {
                         Text(
                            text = hour.toString(), 
                            color = Color.Gray, 
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Scrollable Grid (X-Axis) - Horizontally scrollable
            // Note: Horizontal Scroll inside Vertical Scroll implies diagonal scrolling capability or blocking.
            // Since LazyRow handles horizontal, parent Column handles vertical.
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
                            // Month Label
                            Box(modifier = Modifier.height(16.dp)) {
                                if (day == 1) {
                                    Text(
                                        text = monthName,
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        modifier = Modifier.width(40.dp)
                                    )
                                }
                            }

                            // The vertical dots for this day (24 rows)
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                repeat(rows) { rowIndex ->
                                    val isActive = remember(monthName, day, rowIndex) { 
                                        Random.nextFloat() > 0.85 // Sparser data for 24h
                                    }
                                    val activeColor = Color(0xFF26C6DA) 
                                    val inactiveColor = Color(0xFF1C1C1E)

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
                    item {
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                }
            }
        }
    }
}
