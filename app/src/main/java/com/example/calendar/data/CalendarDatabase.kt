package com.example.calendar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CalendarNote::class], version = 1, exportSchema = false)
abstract class CalendarDatabase : RoomDatabase() {
    abstract fun calendarNoteDao(): CalendarNoteDao

    companion object {
        @Volatile
        private var INSTANCE: CalendarDatabase? = null

        fun getDatabase(context: Context): CalendarDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CalendarDatabase::class.java,
                    "calendar_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
