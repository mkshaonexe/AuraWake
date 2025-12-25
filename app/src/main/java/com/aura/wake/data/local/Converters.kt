package com.aura.wake.data.local

import androidx.room.TypeConverter
import com.aura.wake.data.model.ChallengeType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromChallengeType(value: ChallengeType): String {
        return value.name
    }

    @TypeConverter
    fun toChallengeType(value: String): ChallengeType {
        return try {
            ChallengeType.valueOf(value)
        } catch (e: Exception) {
            ChallengeType.NONE
        }
    }

    @TypeConverter
    fun fromDaysOfWeek(value: Set<Int>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toDaysOfWeek(value: String): Set<Int> {
        val type = object : TypeToken<Set<Int>>() {}.type
        return Gson().fromJson(value, type) ?: emptySet()
    }

    @TypeConverter
    fun fromWakeStatus(value: com.aura.wake.data.model.WakeStatus): String {
        return value.name
    }

    @TypeConverter
    fun toWakeStatus(value: String): com.aura.wake.data.model.WakeStatus {
        return try {
            com.aura.wake.data.model.WakeStatus.valueOf(value)
        } catch (e: Exception) {
            com.aura.wake.data.model.WakeStatus.MISSED
        }
    }
}
