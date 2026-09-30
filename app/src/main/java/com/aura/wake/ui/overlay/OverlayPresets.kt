package com.aura.wake.ui.overlay

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/**
 * Aesthetic Wallpaper & Theme Presets for AuraWake Alarm Overlay.
 * Designed to be minimalist, modern, and non-distracting.
 */
data class OverlayThemePreset(
    val id: String,
    val name: String,
    val subtitle: String,
    val gradientColors: List<Color>,
    val ambientColor: Color,
    val accentColor: Color
)

object OverlayPresets {
    const val ID_AURA = "preset:aura"
    const val ID_MIDNIGHT = "preset:midnight"
    const val ID_SUNRISE = "preset:sunrise"
    const val ID_NEON = "preset:neon"
    const val ID_SLATE = "preset:slate"

    val CosmicAura = OverlayThemePreset(
        id = ID_AURA,
        name = "Cosmic Aura",
        subtitle = "Luminous violet & deep indigo glow",
        gradientColors = listOf(
            Color(0xFF0F0B1E),
            Color(0xFF1B1135),
            Color(0xFF0D0A18),
            Color(0xFF05040A)
        ),
        ambientColor = Color(0xFF7C4DFF),
        accentColor = Color(0xFFB388FF)
    )

    val MidnightOled = OverlayThemePreset(
        id = ID_MIDNIGHT,
        name = "Midnight Dark",
        subtitle = "Pure stealth obsidian minimalism",
        gradientColors = listOf(
            Color(0xFF14171E),
            Color(0xFF0C0E13),
            Color(0xFF060709),
            Color(0xFF000000)
        ),
        ambientColor = Color(0xFF384358),
        accentColor = Color(0xFF90CAF9)
    )

    val GoldenDawn = OverlayThemePreset(
        id = ID_SUNRISE,
        name = "Golden Dawn",
        subtitle = "Warm morning amber & twilight",
        gradientColors = listOf(
            Color(0xFF2A1224),
            Color(0xFF3B1828),
            Color(0xFF1E0D1C),
            Color(0xFF0C050B)
        ),
        ambientColor = Color(0xFFFF8A65),
        accentColor = Color(0xFFFFB74D)
    )

    val CyberGlow = OverlayThemePreset(
        id = ID_NEON,
        name = "Cyber Glow",
        subtitle = "Deep teal & electric cyan",
        gradientColors = listOf(
            Color(0xFF061A24),
            Color(0xFF0B2533),
            Color(0xFF05121B),
            Color(0xFF02070A)
        ),
        ambientColor = Color(0xFF00E5FF),
        accentColor = Color(0xFF18FFFF)
    )

    val MinimalSlate = OverlayThemePreset(
        id = ID_SLATE,
        name = "Minimal Slate",
        subtitle = "Refined architectural graphite",
        gradientColors = listOf(
            Color(0xFF22252A),
            Color(0xFF181A1E),
            Color(0xFF101215),
            Color(0xFF0A0A0C)
        ),
        ambientColor = Color(0xFF5A6270),
        accentColor = Color(0xFFCFD8DC)
    )

    val ALL_PRESETS = listOf(
        CosmicAura,
        MidnightOled,
        GoldenDawn,
        CyberGlow,
        MinimalSlate
    )

    fun getPreset(id: String?): OverlayThemePreset {
        return when (id) {
            ID_MIDNIGHT -> MidnightOled
            ID_SUNRISE -> GoldenDawn
            ID_NEON -> CyberGlow
            ID_SLATE -> MinimalSlate
            else -> CosmicAura // Default to Cosmic Aura
        }
    }

    fun isPreset(uri: String?): Boolean {
        return uri == null || uri.startsWith("preset:")
    }
}

/**
 * Fullscreen or box background that dynamically renders presets with ambient breathing glow
 * or user custom photos with gradient scrims for optimal contrast and readability.
 */
@Composable
fun OverlayBackground(
    overlayUri: String?,
    modifier: Modifier = Modifier,
    animatePulse: Boolean = true
) {
    if (!OverlayPresets.isPreset(overlayUri) && overlayUri != null) {
        // User custom photo
        Box(modifier = modifier.fillMaxSize()) {
            AsyncImage(
                model = overlayUri,
                contentDescription = "Custom Alarm Wallpaper",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Premium cinematic vignette / scrim ensuring clock and button legibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.65f),
                                Color.Black.copy(alpha = 0.30f),
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )
        }
    } else {
        // Aesthetic Preset Wallpaper
        val preset = OverlayPresets.getPreset(overlayUri)

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(preset.gradientColors))
        ) {
            if (animatePulse) {
                val infiniteTransition = rememberInfiniteTransition(label = "aura_pulse")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.90f,
                    targetValue = 1.10f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.25f,
                    targetValue = 0.45f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(3800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )

                // Ambient luminous aura orb
                Canvas(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(340.dp)
                        .graphicsLayer(scaleX = scale, scaleY = scale)
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                preset.ambientColor.copy(alpha = alpha),
                                preset.ambientColor.copy(alpha = alpha * 0.45f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension / 1.7f
                        )
                    )
                }
            } else {
                // Static version for cards and thumbnails
                Canvas(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(340.dp)
                ) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                preset.ambientColor.copy(alpha = 0.35f),
                                preset.ambientColor.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension / 1.7f
                        )
                    )
                }
            }

            // Subtle top and bottom shade for text legibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.25f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.45f)
                            )
                        )
                    )
            )
        }
    }
}
