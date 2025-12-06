package com.alarm.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class ChallengeType {
    NONE, MATH, SHAKE, QR, TYPING
}

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val daysOfWeek: Set<Int> = emptySet(), // 1=Sunday, 2=Monday, ... 7=Saturday
    val label: String? = null,
    val challengeType: ChallengeType = ChallengeType.NONE,
    val challengeConfig: String? = null // e.g., "DIFFICULT", "15_SHAKES", "QR_CODE_CONTENT"
)
