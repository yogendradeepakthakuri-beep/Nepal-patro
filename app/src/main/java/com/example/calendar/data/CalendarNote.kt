package com.example.calendar.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_notes")
data class CalendarNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bsDateStr: String, // String format: "YYYY-MM-DD"
    val title: String,
    val noteText: String,
    val timestamp: Long = System.currentTimeMillis()
)
