package com.aura.wake.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*
import kotlin.random.Random

// Constants for pixel-perfect alignment
private val SQUARE_SIZE = 10.dp
private val GRID_SPACING = 4.dp
private val MONTH_LABEL_HEIGHT = 16.dp

@Composable
fun TrackerSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(vertical = 16.dp, horizontal = 16.dp)
    ) {
        ContributionHeatmap()
    }
}

@Composable
fun ContributionHeatmap() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val application = context.applicationContext as com.aura.wake.AlarmApplication
    val wakeHistoryRepository = remember { application.container.wakeHistoryRepository }
    
    // Fetch wake history data
    var weeksData by remember { mutableStateOf<List<WeekData>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        try {
            val history = wakeHistoryRepository.getHistoryForWeeks(26)
            weeksData = generateHeatmapDataFromHistory(history, wakeHistoryRepository)
        } catch (e: Exception) {
            // If error, show empty state
            weeksData = generateHeatmapData(26)
        }
    }
    
    // 2. Layout
    Row(modifier = Modifier.fillMaxWidth()) {
        // Left Column: Day Labels (Mon, Wed, Fri)
        // We add top padding to align with the Grid (skipping Month Label height)
        Column(modifier = Modifier.padding(top = MONTH_LABEL_HEIGHT + GRID_SPACING)) {
            DayLabels()
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right Column: Month Labels + Grid
        Column(modifier = Modifier.weight(1f)) {
            MonthLabels(weeksData)
            Spacer(modifier = Modifier.height(GRID_SPACING))
            HeatmapGrid(weeksData)
        }
    }
}

@Composable
fun DayLabels() {
    Column(
        verticalArrangement = Arrangement.spacedBy(GRID_SPACING)
    ) {
        // 7 Rows (Sun, Mon, Tue, Wed, Thu, Fri, Sat)
        // Labels on indices: 1 (Mon), 3 (Wed), 5 (Fri)
        repeat(7) { index ->
            Box(
                modifier = Modifier.height(SQUARE_SIZE),
                contentAlignment = Alignment.CenterEnd // Align text to the right, next to the grid
            ) {
                if (index == 1 || index == 3 || index == 5) {
                    val label = when(index) {
                        1 -> "Mon"
                        3 -> "Wed"
                        5 -> "Fri"
                        else -> ""
                    }
                    Text(
                        text = label, 
                        color = Color(0xFFC9D1D9), // Brighter GitHub-like text color
                        fontSize = 9.sp, // Slightly smaller to fit 10dp height
                        lineHeight = 10.sp, // Ensure line height matches box
                        softWrap = false,
                        modifier = Modifier
                            .requiredHeight(12.dp) // Force height to allow visual overflow if needed
                            .wrapContentWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun MonthLabels(weeks: List<WeekData>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(GRID_SPACING)
    ) {
        weeks.forEach { week ->
            Box(
                modifier = Modifier.width(SQUARE_SIZE)
            ) {
                if (week.isNewMonth || (weeks.indexOf(week) == 0 && week.monthName.isNotEmpty())) {
                    // Allow text to overflow the 10dp box
                    Text(
                        text = week.monthName,
                        color = Color(0xFFC9D1D9), // Brighter GitHub-like text color
                        fontSize = 10.sp,
                        softWrap = false,
                        modifier = Modifier.requiredWidth(40.dp) // Ensure it draws fully
                    )
                }
            }
        }
    }
}

@Composable
fun HeatmapGrid(weeks: List<WeekData>) {
    // Use Row instead of LazyRow to ensure strict alignment with MonthLabels in this fixed window
    Row(
        horizontalArrangement = Arrangement.spacedBy(GRID_SPACING),
        modifier = Modifier.fillMaxWidth()
    ) {
        weeks.forEach { week ->
            Column(
                verticalArrangement = Arrangement.spacedBy(GRID_SPACING)
            ) {
                week.days.forEach { level ->
                    HeatmapSquare(level)
                }
            }
        }
    }
}

@Composable
fun HeatmapSquare(level: Int) { // level 0..4 (0=Empty, 4=Max)
    val color = when (level) {
        0 -> Color(0xFF161B22) // GitHub dark empty
        1 -> Color(0xFF0E4429)
        2 -> Color(0xFF006D32)
        3 -> Color(0xFF26A641)
        4 -> Color(0xFF39D353) // Max bright green
        else -> Color(0xFF161B22)
    }

    Box(
        modifier = Modifier
            .size(SQUARE_SIZE)
            .clip(RoundedCornerShape(2.dp))
            .background(color)
    )
}

// --- Data Models & Helpers ---

data class WeekData(
    val monthName: String,
    val isNewMonth: Boolean,
    val days: List<Int> // 7 ints representing activity level 0-4
)

fun generateHeatmapData(numWeeks: Int): List<WeekData> {
    val weeks = mutableListOf<WeekData>()
    val cal = Calendar.getInstance()
    
    // Start from current week and go back
    var lastMonth = -1

    repeat(numWeeks) {
        val days = mutableListOf<Int>()
        repeat(7) {
            // Empty data (level 0)
            days.add(0)
        }
        
        val currentMonth = cal.get(Calendar.MONTH)
        val monthName = if (currentMonth != lastMonth) getMonthName(currentMonth) else ""
        val isNewMonth = currentMonth != lastMonth
        
        weeks.add(WeekData(monthName, isNewMonth, days))
        
        lastMonth = currentMonth
        cal.add(Calendar.WEEK_OF_YEAR, -1) // Go backwards in time
    }
    return weeks
}

/**
 * Generate heatmap data from real wake history
 * Shows current month first (reverse chronological order)
 */
fun generateHeatmapDataFromHistory(
    history: List<com.aura.wake.data.model.AlarmWakeHistory>, 
    repository: com.aura.wake.data.repository.WakeHistoryRepository
): List<WeekData> {
    val weeks = mutableListOf<WeekData>()
    val cal = Calendar.getInstance()
    
    // Create a map of date to wake history for quick lookup
    val historyMap = history.associateBy { 
        val c = Calendar.getInstance()
        c.timeInMillis = it.date
        c.get(Calendar.DAY_OF_YEAR) to c.get(Calendar.YEAR)
    }
    
    var lastMonth = -1

    repeat(26) { weekIndex ->
        val days = mutableListOf<Int>()
        
        // For each day of the week (Sunday to Saturday)
        repeat(7) { dayIndex ->
            val tempCal = Calendar.getInstance()
            tempCal.timeInMillis = cal.timeInMillis
            tempCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY + dayIndex)
            
            val dayOfYear = tempCal.get(Calendar.DAY_OF_YEAR)
            val year = tempCal.get(Calendar.YEAR)
            
            // Check if we have wake history for this day
            val wakeHistory = historyMap[dayOfYear to year]
            val level = if (wakeHistory != null && wakeHistory.status == com.aura.wake.data.model.WakeStatus.COMPLETED) {
                // Calculate intensity based on alarm hour
                val wakeUpCal = Calendar.getInstance()
                wakeUpCal.timeInMillis = wakeHistory.wakeUpTime
                val wakeUpHour = wakeUpCal.get(Calendar.HOUR_OF_DAY)
                repository.calculateIntensityLevel(wakeUpHour)
            } else {
                0 // No wake-up or missed/skipped
            }
            days.add(level)
        }
        
        val currentMonth = cal.get(Calendar.MONTH)
        val monthName = if (currentMonth != lastMonth) getMonthName(currentMonth) else ""
        val isNewMonth = currentMonth != lastMonth
        
        weeks.add(WeekData(monthName, isNewMonth, days))
        
        lastMonth = currentMonth
        cal.add(Calendar.WEEK_OF_YEAR, -1) // Go backwards in time
    }
    
    return weeks
}

fun getMonthName(month: Int): String {
    return when(month) {
        Calendar.JANUARY -> "Jan"
        Calendar.FEBRUARY -> "Feb"
        Calendar.MARCH -> "Mar"
        Calendar.APRIL -> "Apr"
        Calendar.MAY -> "May"
        Calendar.JUNE -> "Jun"
        Calendar.JULY -> "Jul"
        Calendar.AUGUST -> "Aug"
        Calendar.SEPTEMBER -> "Sep"
        Calendar.OCTOBER -> "Oct"
        Calendar.NOVEMBER -> "Nov"
        Calendar.DECEMBER -> "Dec"
        else -> ""
    }
}
