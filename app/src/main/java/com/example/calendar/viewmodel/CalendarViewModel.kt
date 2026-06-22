package com.example.calendar.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calendar.NepaliCalendarHelper
import com.example.calendar.NepaliDate
import com.example.calendar.data.CalendarDatabase
import com.example.calendar.data.CalendarNote
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val db = CalendarDatabase.getDatabase(application)
    private val dao = db.calendarNoteDao()

    // Real system default Nepali Date
    private val systemPost = NepaliCalendarHelper.adToBs(Date()) ?: NepaliDate(2083, 3, 7, 1)

    // Calendar Visual States
    private val _selectedDate = MutableStateFlow(systemPost)
    val selectedDate: StateFlow<NepaliDate> = _selectedDate.asStateFlow()

    var viewYear by mutableStateOf(systemPost.year)
        private set

    var viewMonth by mutableStateOf(systemPost.month) // 1-12
        private set

    // Selected Date Notes Flow
    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedDateNotes: StateFlow<List<CalendarNote>> = _selectedDate
        .flatMapLatest { date ->
            val dateStr = String.format(java.util.Locale.US, "%04d-%02d-%02d", date.year, date.month, date.day)
            dao.getNotesForDate(dateStr)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // All Saves List Flow
    val allNotes: StateFlow<List<CalendarNote>> = dao.getAllNotes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // AD to BS Converter States
    var convAdYear by mutableStateOf("2026")
    var convAdMonth by mutableStateOf("6")
    var convAdDay by mutableStateOf("21")
    var convAdResult by mutableStateOf<String?>(null)

    // BS to AD Converter States
    var convBsYear by mutableStateOf("2083")
    var convBsMonth by mutableStateOf("3")
    var convBsDay by mutableStateOf("7")
    var convBsResult by mutableStateOf<String?>(null)

    init {
        // Run initial standard conversion representations
        convertAdToBs()
        convertBsToAd()
    }

    fun selectDate(date: NepaliDate) {
        _selectedDate.value = date
    }

    fun navigateNextMonth() {
        if (viewMonth == 12) {
            if (viewYear < NepaliCalendarHelper.END_YEAR_BS) {
                viewYear++
                viewMonth = 1
            }
        } else {
            viewMonth++
        }
    }

    fun navigatePrevMonth() {
        if (viewMonth == 1) {
            if (viewYear > NepaliCalendarHelper.START_YEAR_BS) {
                viewYear--
                viewMonth = 12
            }
        } else {
            viewMonth--
        }
    }

    fun setViewMonthAndYear(month: Int, year: Int) {
        if (year in NepaliCalendarHelper.START_YEAR_BS..NepaliCalendarHelper.END_YEAR_BS) {
            viewYear = year
        }
        if (month in 1..12) {
            viewMonth = month
        }
    }

    // Database Actions
    fun saveNote(title: String, text: String, dateOverride: String? = null) {
        viewModelScope.launch {
            val dateStr = dateOverride ?: String.format(
                java.util.Locale.US,
                "%04d-%02d-%02d",
                _selectedDate.value.year,
                _selectedDate.value.month,
                _selectedDate.value.day
            )
            val note = CalendarNote(
                bsDateStr = dateStr,
                title = title,
                noteText = text
            )
            dao.insertNote(note)
        }
    }

    fun deleteNote(id: Int) {
        viewModelScope.launch {
            dao.deleteNoteById(id)
        }
    }

    // Conversion Computations
    fun convertAdToBs() {
        val y = convAdYear.toIntOrNull() ?: return
        val m = convAdMonth.toIntOrNull() ?: return
        val d = convAdDay.toIntOrNull() ?: return

        try {
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, y)
            cal.set(Calendar.MONTH, m - 1)
            cal.set(Calendar.DAY_OF_MONTH, d)
            cal.set(Calendar.HOUR_OF_DAY, 12) // Avoid timezone shifts

            val out = NepaliCalendarHelper.adToBs(cal.time)
            if (out != null) {
                convAdResult = "${out.year}-${out.month}-${out.day} (${out.monthNameEn} / ${out.monthNameNp} ${out.day})"
            } else {
                convAdResult = "Unsupported English range (Requires April 13, 2013+)"
            }
        } catch (e: Exception) {
            convAdResult = "Invalid Input Date Format"
        }
    }

    fun convertBsToAd() {
        val y = convBsYear.toIntOrNull() ?: return
        val m = convBsMonth.toIntOrNull() ?: return
        val d = convBsDay.toIntOrNull() ?: return

        try {
            val out = NepaliCalendarHelper.bsToAd(y, m, d)
            if (out != null) {
                val sdf = SimpleDateFormat("EEEE, MMMM dd, yyyy", java.util.Locale.US)
                convBsResult = sdf.format(out)
            } else {
                convBsResult = "Unsupported BS date range (Must be 2070 - 2095)"
            }
        } catch (e: Exception) {
            convBsResult = "Invalid Input BS Date"
        }
    }
}
