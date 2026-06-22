package com.example.calendar.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.NoteAlt
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.calendar.NepaliCalendarHelper
import com.example.calendar.NepaliDate
import com.example.calendar.data.CalendarNote
import com.example.calendar.viewmodel.CalendarViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    val systemPost = NepaliCalendarHelper.adToBs(Date()) ?: NepaliDate(2083, 3, 7, 1)
    val formattedToday = "आज: ${systemPost.dayOfWeekNameNp}, ${systemPost.monthNameNp} ${systemPost.devanagariDay}, ${systemPost.devanagariYear}"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "नेपाली पात्रो (Nepali Calendar)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = formattedToday,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.setViewMonthAndYear(systemPost.month, systemPost.year)
                            viewModel.selectDate(systemPost)
                        },
                        modifier = Modifier.testTag("today_quick_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Jump to Today"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                tonalElevation = 8.dp
            ) {
                val tabs = listOf(
                    Triple("पात्रो (Calendar)", Icons.Default.CalendarMonth, "tab_calendar"),
                    Triple("रूपान्तरण (Convert)", Icons.Default.SwapHoriz, "tab_converter"),
                    Triple("चाडपर्व (Events)", Icons.Default.Festival, "tab_festivals"),
                    Triple("टिप्पणी (Notes)", Icons.Default.NoteAlt, "tab_notes")
                )

                tabs.forEachIndexed { index, (label, icon, tag) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 11.sp, overflow = TextOverflow.Ellipsis, maxLines = 1) },
                        modifier = Modifier.testTag(tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> CalendarTab(viewModel)
                1 -> ConverterTab(viewModel)
                2 -> FestivalsTab()
                3 -> NotesTab(viewModel)
            }
        }
    }
}

@Composable
fun CalendarTab(viewModel: CalendarViewModel) {
    val selectedNepaliDate by viewModel.selectedDate.collectAsState()
    val notesList by viewModel.selectedDateNotes.collectAsState()

    // Days in current view month
    val viewYear = viewModel.viewYear
    val viewMonth = viewModel.viewMonth

    val monthDaysList = NepaliCalendarHelper.MONTH_DAYS_BS[viewYear] ?: listOf(30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30, 30)
    val daysInThisMonth = monthDaysList[viewMonth - 1]

    // Determine the Gregorian starting date of this month, to find what day of week (Sunday to Saturday) it starts on
    val startAdDate = NepaliCalendarHelper.bsToAd(viewYear, viewMonth, 1)
    val dayOfWeekStart = if (startAdDate != null) {
        val cal = Calendar.getInstance()
        cal.time = startAdDate
        cal.get(Calendar.DAY_OF_WEEK) // 1 (Sun) to 7 (Sat)
    } else {
        1
    }

    val monthNameNp = NepaliCalendarHelper.MONTHS_NP[viewMonth - 1]
    val monthNameEn = NepaliCalendarHelper.MONTHS_EN[viewMonth - 1]
    val yearDevanagari = NepaliCalendarHelper.toDevanagari(viewYear)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Month Selector Header Block
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigatePrevMonth() },
                        modifier = Modifier
                            .testTag("prev_month_btn")
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Previous Month",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$monthNameNp $yearDevanagari",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$monthNameEn $viewYear",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.navigateNextMonth() },
                        modifier = Modifier
                            .testTag("next_month_btn")
                            .size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next Month",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // 7 columns labels for Days of Week
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val shortDaysNp = listOf("आइत", "सोम", "मङ्गल", "बुध", "बिही", "शुक्र", "शनि")
                shortDaysNp.forEachIndexed { index, name ->
                    Text(
                        text = name,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (index == 6) Color.Red else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Calendar Grid Block
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // We will generate rows of days dynamically.
                    // Total slots to draw = daysInThisMonth + dayOfWeekStart - 1
                    val offset = dayOfWeekStart - 1
                    val totalSlots = daysInThisMonth + offset
                    val rows = (totalSlots + 6) / 7

                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (c in 0 until 7) {
                                val slotIndex = r * 7 + c
                                val dayNum = slotIndex - offset + 1

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (dayNum in 1..daysInThisMonth) {
                                        val dayOfWeek = c + 1
                                        val dateObj = NepaliDate(viewYear, viewMonth, dayNum, dayOfWeek)
                                        val isSelected = selectedNepaliDate.year == viewYear &&
                                                selectedNepaliDate.month == viewMonth &&
                                                selectedNepaliDate.day == dayNum

                                        val adDate = NepaliCalendarHelper.bsToAd(viewYear, viewMonth, dayNum)
                                        val isToday = if (adDate != null) {
                                            val cal1 = Calendar.getInstance()
                                            val cal2 = Calendar.getInstance()
                                            cal2.time = adDate
                                            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                                                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
                                        } else {
                                            false
                                        }

                                        val tithi = adDate?.let { NepaliCalendarHelper.getTithiForDate(it) } ?: "Pratipada"
                                        val (festival, isHoliday) = NepaliCalendarHelper.getFestivalAndHoliday(viewMonth, dayNum, tithi)

                                        val backgroundModifier = when {
                                            isSelected -> Modifier.background(
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            )
                                            isToday -> Modifier.border(
                                                2.dp,
                                                MaterialTheme.colorScheme.secondary,
                                                CircleShape
                                            )
                                            else -> Modifier
                                        }

                                        val textPrimaryColor = when {
                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                            isHoliday || dayOfWeek == 7 -> Color.Red
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }

                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .then(backgroundModifier)
                                                .clickable { viewModel.selectDate(dateObj) }
                                                .testTag("day_${dayNum}"),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = NepaliCalendarHelper.toDevanagari(dayNum),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textPrimaryColor
                                            )
                                            
                                            // English small day label
                                            adDate?.let {
                                                val cal = Calendar.getInstance()
                                                cal.time = it
                                                val engDay = cal.get(Calendar.DAY_OF_MONTH)
                                                Text(
                                                    text = engDay.toString(),
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else textPrimaryColor.copy(alpha = 0.6f)
                                                )
                                            }

                                            // Show indicator if there is a festival
                                            if (festival != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .background(if (isHoliday) Color.Red else Color.Blue, CircleShape)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Details Panel of the Selected Date
        item {
            val adDate = NepaliCalendarHelper.bsToAd(selectedNepaliDate.year, selectedNepaliDate.month, selectedNepaliDate.day)
            val tithi = adDate?.let { NepaliCalendarHelper.getTithiForDate(it) } ?: ""
            val (festival, isHoliday) = NepaliCalendarHelper.getFestivalAndHoliday(
                selectedNepaliDate.month,
                selectedNepaliDate.day,
                tithi
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedNepaliDate.monthNameNp} ${selectedNepaliDate.devanagariDay}, ${selectedNepaliDate.devanagariYear}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = selectedNepaliDate.dayOfWeekNameNp,
                            fontSize = 12.sp,
                            color = if (selectedNepaliDate.dayOfWeek == 7) Color.Red else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))

                    // English details row
                    adDate?.let { date ->
                        val sdf = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US)
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.CalendarToday, contentDescription = "English Date", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "English: ${sdf.format(date)}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Tithi Details row
                    if (tithi.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Brightness4, contentDescription = "Tithi", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "तिथि: $tithi",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Festival / Holiday Display
                    if (festival != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isHoliday) Color.Red.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                    ),
                                    RoundedCornerShape(8.dp)
                                )
                                .background(
                                    if (isHoliday) Color.Red.copy(alpha = 0.08f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Festival,
                                contentDescription = "Event icon",
                                tint = if (isHoliday) Color.Red else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = festival,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isHoliday) Color.Red else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Inline Reminder notes list for this date
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "टिप्पणी तथा अनुस्मारक (Events & Notes):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    InlineNotesSection(notesList, viewModel)
                }
            }
        }
    }
}

@Composable
fun InlineNotesSection(
    notes: List<CalendarNote>,
    viewModel: CalendarViewModel
) {
    var isAddingNote by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (notes.isEmpty()) {
            Text(
                text = "यस दिन कुनै पनि टिप्पणी सुरक्षित छैन।",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        } else {
            notes.forEach { note ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = note.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (note.noteText.isNotEmpty()) {
                            Text(
                                text = note.noteText,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.deleteNote(note.id) },
                        modifier = Modifier
                            .testTag("delete_note_${note.id}")
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Note",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        if (isAddingNote) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("शीर्षक (Title)", fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_title_input"),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                    )

                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("विवरण (Details)", fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_text_input"),
                        singleLine = false,
                        maxLines = 3,
                        textStyle = LocalTextStyle.current.copy(fontSize = 12.sp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                isAddingNote = false
                                noteTitle = ""
                                noteText = ""
                                focusManager.clearFocus()
                            }
                        ) {
                            Text("रद्द (Cancel)", fontSize = 11.sp)
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (noteTitle.trim().isNotEmpty()) {
                                    viewModel.saveNote(noteTitle, noteText)
                                    noteTitle = ""
                                    noteText = ""
                                    isAddingNote = false
                                    focusManager.clearFocus()
                                }
                            },
                            modifier = Modifier.testTag("save_note_btn"),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("सुरक्षित गर्नुहोस् (Save)", fontSize = 11.sp)
                        }
                    }
                }
            }
        } else {
            Button(
                onClick = { isAddingNote = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inline_add_note_btn"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = "Add Notes", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("टिप्पणी थप्नुहोस् (Add Note)", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun ConverterTab(viewModel: CalendarViewModel) {
    val focusManager = LocalFocusManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Option 1: AD to BS conversion card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "AD calendar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gregorian सन् (AD) बाट विक्रम संवत् (BS)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    HorizontalDivider(thickness = 0.5.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = viewModel.convAdYear,
                            onValueChange = { viewModel.convAdYear = it },
                            label = { Text("वर्ष (Year)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("conv_ad_year"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = viewModel.convAdMonth,
                            onValueChange = { viewModel.convAdMonth = it },
                            label = { Text("महिना (Month)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("conv_ad_month"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = viewModel.convAdDay,
                            onValueChange = { viewModel.convAdDay = it },
                            label = { Text("गते (Day)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("conv_ad_day"),
                            singleLine = true
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.convertAdToBs()
                            focusManager.clearFocus()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("convert_ad_to_bs_btn")
                    ) {
                        Icon(Icons.Default.CompareArrows, contentDescription = "Convert")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("रूपान्तरण गर्नुहोस् (Convert AD to BS)")
                    }

                    // Answer panel
                    viewModel.convAdResult?.let { result ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "परिणाम (Nepali Date BS):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = result,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // Option 2: BS to AD conversion card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = "BS calendar",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "विक्रम संवत् (BS) बाट Gregorian सन् (AD)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    HorizontalDivider(thickness = 0.5.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = viewModel.convBsYear,
                            onValueChange = { viewModel.convBsYear = it },
                            label = { Text("वर्ष (Year)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("conv_bs_year"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = viewModel.convBsMonth,
                            onValueChange = { viewModel.convBsMonth = it },
                            label = { Text("महिना (Month)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("conv_bs_month"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = viewModel.convBsDay,
                            onValueChange = { viewModel.convBsDay = it },
                            label = { Text("गते (Day)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("conv_bs_day"),
                            singleLine = true
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.convertBsToAd()
                            focusManager.clearFocus()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("convert_bs_to_ad_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.CompareArrows, contentDescription = "Convert")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("रूपान्तरण गर्नुहोस् (Convert BS to AD)")
                    }

                    // Answer panel
                    viewModel.convBsResult?.let { result ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "परिणाम (Gregorian Date AD):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = result,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FestivalsTab() {
    var searchQuery by remember { mutableStateOf("") }

    // Hardcode a static elegant list of major festivals with standard BS months
    val allFestivals = remember {
        listOf(
            FestivalItem("नयाँ वर्ष (Nepali New Year)", "Baisakh 1", "Baishakh", true),
            FestivalItem("लोकतन्त्र दिवस (Loktantra Diwas)", "Baisakh 11", "Baishakh", false),
            FestivalItem("बुद्ध जयन्ती (Buddha Jayanti)", "Baisakh Purnima (लुनार)", "Baishakh", true),
            FestivalItem("गणतन्त्र दिवस (Ganatantra Diwas)", "Jestha 15", "Jestha", true),
            FestivalItem("धान दिवस / दही चिउरा खाने दिन (National Paddy Day)", "Ashadh 15", "Ashadh", false),
            FestivalItem("खीर खाने दिन (Kheer Khane Din)", "Shrawan 15", "Shrawan", false),
            FestivalItem("जनै पूर्णिमा / रक्षाबन्धन (Janai Purnima)", "Shrawon Purnima (लुनार)", "Shrawan", true),
            FestivalItem("हरितालिका तीज (Teej Festival)", "Bhadra Shukla Tritiya", "Bhadra", true),
            FestivalItem("ऋषि पञ्चमी (Rishi Panchami)", "Bhadra Shukla Panchami", "Bhadra", false),
            FestivalItem("श्रीकृष्ण जन्माष्टमी (Janmashtami)", "Bhadra Krishna Ashtami", "Bhadra", true),
            FestivalItem("गाईजात्रा (Gai Jatra)", "Bhadra Krishna Pratipada", "Bhadra", false),
            FestivalItem("संविधान दिवस (Constitution Day)", "Ashwin 3", "Ashwin", true),
            FestivalItem("बडा दशैँ घटस्थापना (Dashain Ghatasthapana)", "Ashwin Shukla Pratipada", "Ashwin", true),
            FestivalItem("महा फूलपाती (Dashain Fulpati)", "Ashwin Shukla Saptami", "Ashwin", true),
            FestivalItem("महा अष्टमी (Maha Ashtami)", "Ashwin Shukla Ashtami", "Ashwin", true),
            FestivalItem("महा नवमी (Maha Navami)", "Ashwin Shukla Navami", "Ashwin", true),
            FestivalItem("विजया दशमी (Bijaya Dashami)", "Ashwin Shukla Dashami", "Ashwin", true),
            FestivalItem("कोजाग्रत पूर्णिमा (Kojagrat Purnima)", "Ashwin Shukla Purnima", "Ashwin", true),
            FestivalItem("काग तिहार (Kag Tihar)", "Kartik Krishna Trayodashi", "Kartik", false),
            FestivalItem("कुकुर तिहार (Kukur Tihar)", "Kartik Krishna Chaturdashi", "Kartik", false),
            FestivalItem("लक्ष्मी पूजा (Laxmi puja)", "Kartik Krishna Aunshi", "Kartik", true),
            FestivalItem("म्हा पूजा / गोवर्धन पूजा (Mha Puja)", "Kartik Shukla Pratipada", "Kartik", true),
            FestivalItem("भाइटीका / तिहार विशेष (Bhai Tika)", "Kartik Shukla Dwitiya", "Kartik", true),
            FestivalItem("छठ पर्व (Chhath Parva)", "Kartik Shukla Shasthi", "Kartik", true),
            FestivalItem("योमरी पुन्ही / उधौली पर्व (Yomari Punhi)", "Mangsir Purnima", "Mangsir", false),
            FestivalItem("पृथ्वी जयन्ती (Prithvi Jayanti)", "Poush 27", "Poush", true),
            FestivalItem("तमु ल्होसार (Tamu Lhosar)", "Poush 15", "Poush", true),
            FestivalItem("माघे संक्रान्ति (Maghe Sankranti)", "Magh 1", "Magh", true),
            FestivalItem("सोनाम ल्होसार (Sonam Lhosar)", "Magh Shukla Pratipada", "Magh", true),
            FestivalItem("महा शिवरात्रि (Maha Shivaratri)", "Falgun Krishna Chaturdashi", "Falgun", true),
            FestivalItem("फागु पूर्णिमा / होली पर्यन्त (Holi Festival)", "Falgun Purnima", "Falgun", true),
            FestivalItem("नारी दिवस (Women's Day)", "Falgun 24 / March 8", "Falgun", true),
            FestivalItem("घोडेजात्रा (Ghode Jatra)", "Chaitra Krishna Aunshi", "Chaitra", false),
            FestivalItem("चैते दशैँ (Chaite Dashain)", "Chaitra Shukla Ashtami", "Chaitra", false),
            FestivalItem("राम नवमी (Ram Navami)", "Chaitra Shukla Navami", "Chaitra", true)
        )
    }

    val filteredFestivals = allFestivals.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.month.contains(searchQuery, ignoreCase = true) ||
                it.nepaliDate.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("चाडपर्व वा महिना खोज्नुहोस् (Search festivals)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("festivals_search_bar"),
            singleLine = true,
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            }
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredFestivals.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Event,
                            contentDescription = "Not Found",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "कुनै पनि चाडपर्वहरू फेला परेन।",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            } else {
                items(filteredFestivals) { festival ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = festival.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "तिथि/गते: ${festival.nepaliDate}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }

                            if (festival.isHoliday) {
                                BadgeHoliday(text = "बिदा (Holiday)")
                            } else {
                                BadgeHoliday(text = "पर्व (Festival)", isHoliday = false)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeHoliday(text: String, isHoliday: Boolean = true) {
    Surface(
        color = if (isHoliday) Color.Red.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            1.dp,
            if (isHoliday) Color.Red.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHoliday) Color.Red else MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

data class FestivalItem(
    val name: String,
    val nepaliDate: String,
    val month: String,
    val isHoliday: Boolean
)

@Composable
fun NotesTab(viewModel: CalendarViewModel) {
    val allSavedNotes by viewModel.allNotes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "सुरक्षित टिप्पणीहरू (Saved Notes & Events):",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (allSavedNotes.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NoteAlt,
                            contentDescription = "Empty Notes",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "तपाईंले कुनै पनि अनुस्मारक थप्नु भएको छैन।",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(allSavedNotes) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = note.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (note.noteText.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = note.noteText,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "BS मिति: ${note.bsDateStr}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteNote(note.id) },
                                modifier = Modifier.testTag("delete_saved_${note.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete event",
                                    tint = Color.Red.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
