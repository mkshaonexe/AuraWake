package com.alarm.app.data.local

import androidx.room.TypeConverter
import com.alarm.app.data.model.ChallengeType
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
}
