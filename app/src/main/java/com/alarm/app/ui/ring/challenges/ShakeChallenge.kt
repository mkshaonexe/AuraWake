package com.alarm.app.ui.ring.challenges

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.sqrt

@Composable
fun ShakeChallenge(
    onCompleted: () -> Unit
) {
    val context = LocalContext.current
    val targetShakes = 30 // Example
    var currentShakes by remember { mutableStateOf(0) }
    
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        val listener = object : SensorEventListener {
            private var lastAcceleration = 0f
            private var currentAcceleration = SensorManager.GRAVITY_EARTH
            private var shakeThreshold = 12f // Sensitivity
            
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val x = it.values[0]
                    val y = it.values[1]
                    val z = it.values[2]
                    
                    val last = currentAcceleration
                    currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                    val delta = currentAcceleration - last
                    lastAcceleration = lastAcceleration * 0.9f + delta
                    
                    if (lastAcceleration > shakeThreshold) {
                         // Simple shake detection logic (debounce needed in real app)
                         // Here we assume high frequency update
                         // Let's use a simpler logic for demo or debounce locally
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        // Improved Shake Detection Logic Wrapper
        val shakeDetector = object : SensorEventListener {
            private var mAccel = 0.0f
            private var mAccelCurrent = SensorManager.GRAVITY_EARTH
            private var mAccelLast = SensorManager.GRAVITY_EARTH

            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                mAccelLast = mAccelCurrent
                mAccelCurrent = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val delta = mAccelCurrent - mAccelLast
                mAccel = mAccel * 0.9f + delta
                
                if (mAccel > 12) {
                    currentShakes++
                    if (currentShakes >= targetShakes) {
                        onCompleted()
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        
        sensorManager.registerListener(shakeDetector, accelerometer, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(shakeDetector)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), // Dark background
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            // Icon or Graphic
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Vibration, // Explicit import would be better but this works if available, otherwise Smartphone
                contentDescription = null,
                tint = Color(0xFF26C6DA),
                modifier = Modifier
                    .size(48.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Shake to wake!",
                fontSize = 28.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Box(contentAlignment = Alignment.Center) {
                // Background Track
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(220.dp),
                    color = Color(0xFF1C1C1E), // Dark gray track
                    trackColor = Color(0xFF1C1C1E),
                    strokeWidth = 12.dp
                )
                
                // Progress
                CircularProgressIndicator(
                    progress = { currentShakes.toFloat() / targetShakes },
                    modifier = Modifier.size(220.dp),
                    color = Color(0xFF26C6DA), // Teal
                    trackColor = Color.Transparent, // We use the background one for track
                    strokeWidth = 12.dp,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(currentShakes * 100 / targetShakes)}%",
                        fontSize = 56.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "$currentShakes / $targetShakes", 
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Keep shaking!",
                color = Color.Gray,
                fontSize = 18.sp
            )
        }
    }
}
