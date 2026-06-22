package com.example.calendar.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarNoteDao {
    @Query("SELECT * FROM calendar_notes WHERE bsDateStr = :dateStr ORDER BY timestamp DESC")
    fun getNotesForDate(dateStr: String): Flow<List<CalendarNote>>

    @Query("SELECT * FROM calendar_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<CalendarNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: CalendarNote)

    @Query("DELETE FROM calendar_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)
}
