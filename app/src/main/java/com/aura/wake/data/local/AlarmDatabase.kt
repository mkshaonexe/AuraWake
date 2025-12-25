package com.aura.wake.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aura.wake.data.model.Alarm

@Database(entities = [Alarm::class, com.aura.wake.data.model.AlarmWakeHistory::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun wakeHistoryDao(): WakeHistoryDao

    companion object {
        @Volatile
        private var Instance: AlarmDatabase? = null

        fun getDatabase(context: Context): AlarmDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AlarmDatabase::class.java, "alarm_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
