package com.aura.wake.data.model

enum class Difficulty {
    EASY, MEDIUM, HARD, VERY_HARD
}

data class MathMissionConfig(
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val problemCount: Int = 3
)

data class TypingMissionConfig(
    val sentences: List<String> = listOf(
        "I am awake and ready to conquer the day.",
        "Success is not final, failure is not fatal.",
        "The only way to do great work is to love what you do."
    ),
    val wordCount: Int = 5 // Minimum words if we go by random text generation or selection
)

data class QrMissionConfig(
    val qrContent: String? = null,
    val qrLabel: String? = null
)

data class MemoryMissionConfig(
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val questionCount: Int = 3
)

// Wrapper for all mission configs if needed, or we just retrieve them individually via ChallengeType
