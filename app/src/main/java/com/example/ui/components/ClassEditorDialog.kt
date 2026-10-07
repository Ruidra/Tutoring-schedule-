package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DayInfo
import com.example.data.model.Teacher
import com.example.data.model.TuitionClass
import com.example.ui.theme.RoutineTheme
import com.example.util.TimeUtil

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClassEditorDialog(
    initialClass: TuitionClass,
    isNew: Boolean,
    teachers: List<Teacher>,
    allExistingSubjects: List<String>,
    onDismiss: () -> Unit,
    onSave: (TuitionClass) -> Unit,
    onDelete: (TuitionClass) -> Unit
) {
    val colors = RoutineTheme.colors

    var subject by remember { mutableStateOf(initialClass.subject) }
    var selectedDay by remember { mutableIntStateOf(initialClass.day) }
    var selectedTeacherId by remember { mutableStateOf(initialClass.teacherId) }
    var startHour by remember { mutableIntStateOf(TimeUtil.parseToMinutes(initialClass.start) / 60) }
    var startMinute by remember { mutableIntStateOf(TimeUtil.parseToMinutes(initialClass.start) % 60) }
    var endHour by remember { mutableIntStateOf(TimeUtil.parseToMinutes(initialClass.end) / 60) }
    var endMinute by remember { mutableIntStateOf(TimeUtil.parseToMinutes(initialClass.end) % 60) }
    var remindMinutes by remember { mutableIntStateOf(initialClass.remindMinutes) }
    var note by remember { mutableStateOf(initialClass.note ?: "") }

    var deleteConfirmArmed by remember { mutableStateOf(false) }

    var dayDropdownExpanded by remember { mutableStateOf(false) }
    var teacherDropdownExpanded by remember { mutableStateOf(false) }

    val commonSubjectSuggestions = listOf(
        "Physics 1st Paper",
        "Physics 2nd Paper",
        "Chemistry 1st Paper",
        "Chemistry 2nd Paper",
        "Higher Math",
        "Biology",
        "ICT",
        "English",
        "Bangla"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("class_editor_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isNew) "Add Class" else "Edit Class",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.muted
                        )
                    }
                }

                // Subject TextField
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "SUBJECT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = colors.muted
                    )
                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        placeholder = { Text("e.g. Physics 1st Paper", color = colors.muted) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_class_subject"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Quick subject suggestion chips
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        val suggestions = (commonSubjectSuggestions + allExistingSubjects)
                            .distinct()
                            .take(6)

                        suggestions.forEach { suggestion ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.field)
                                    .border(1.dp, colors.line, RoundedCornerShape(8.dp))
                                    .clickable { subject = suggestion }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = suggestion,
                                    fontSize = 11.sp,
                                    color = colors.ink,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Day Selector Dropdown
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "DAY OF WEEK",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = colors.muted
                    )
                    ExposedDropdownMenuBox(
                        expanded = dayDropdownExpanded,
                        onExpandedChange = { dayDropdownExpanded = !dayDropdownExpanded }
                    ) {
                        val currentDay = DayInfo.getDayInfo(selectedDay)
                        OutlinedTextField(
                            value = "${currentDay.fullName} (${currentDay.banglaName})",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("select_class_day"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = dayDropdownExpanded,
                            onDismissRequest = { dayDropdownExpanded = false }
                        ) {
                            DayInfo.ALL_DAYS.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text("${d.fullName} (${d.banglaName})") },
                                    onClick = {
                                        selectedDay = d.dayOfWeek
                                        dayDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Teacher Selector Dropdown
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "TEACHER",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = colors.muted
                    )
                    ExposedDropdownMenuBox(
                        expanded = teacherDropdownExpanded,
                        onExpandedChange = { teacherDropdownExpanded = !teacherDropdownExpanded }
                    ) {
                        val currentTeacher = teachers.firstOrNull { it.id == selectedTeacherId }
                        OutlinedTextField(
                            value = currentTeacher?.name ?: "No teacher assigned",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("select_class_teacher"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = teacherDropdownExpanded,
                            onDismissRequest = { teacherDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("None (No teacher)") },
                                onClick = {
                                    selectedTeacherId = null
                                    teacherDropdownExpanded = false
                                }
                            )
                            teachers.forEach { t ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(t.name, fontWeight = FontWeight.SemiBold)
                                            if (t.teaches.isNotBlank()) {
                                                Text(t.teaches, fontSize = 12.sp, color = colors.muted)
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedTeacherId = t.id
                                        teacherDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Start Time & End Time Pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Start Time
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "STARTS AT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.muted
                        )
                        TimePickerButton(
                            hour = startHour,
                            minute = startMinute,
                            onTimeSelected = { h, m ->
                                startHour = h
                                startMinute = m
                                // If end time is before start time, automatically advance end time by 1 hour
                                if (endHour * 60 + endMinute <= h * 60 + m) {
                                    endHour = (h + 1).coerceAtMost(23)
                                    endMinute = m
                                }
                            },
                            modifier = Modifier.testTag("time_picker_start")
                        )
                    }

                    // End Time
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "ENDS AT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.muted
                        )
                        TimePickerButton(
                            hour = endHour,
                            minute = endMinute,
                            onTimeSelected = { h, m ->
                                endHour = h
                                endMinute = m
                            },
                            modifier = Modifier.testTag("time_picker_end")
                        )
                    }
                }

                // Reminder Presets
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "REMIND BEFORE CLASS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = colors.muted
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0, 15, 30, 45, 60).forEach { mins ->
                            val isSelected = remindMinutes == mins
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) colors.accent else colors.field)
                                    .border(1.dp, if (isSelected) colors.accent else colors.line, RoundedCornerShape(8.dp))
                                    .clickable { remindMinutes = mins }
                                    .padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (mins == 0) "Off" else "${mins}m",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colors.accentInk else colors.ink
                                )
                            }
                        }
                    }
                }

                // Note
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "NOTE (OPTIONAL)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = colors.muted
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = { Text("e.g. bring lab notebook & calculator", color = colors.muted) },
                        maxLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_class_note"),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions: Done & Delete
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val startStr = String.format("%02d:%02d", startHour, startMinute)
                            val endStr = String.format("%02d:%02d", endHour, endMinute)
                            val updated = initialClass.copy(
                                subject = subject.ifBlank { "Class" },
                                day = selectedDay,
                                teacherId = selectedTeacherId,
                                start = startStr,
                                end = endStr,
                                remindMinutes = remindMinutes,
                                note = note.trim().ifEmpty { null }
                            )
                            onSave(updated)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accent,
                            contentColor = colors.accentInk
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("btn_save_class")
                    ) {
                        Text("Done / Save Class", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    if (!isNew) {
                        OutlinedButton(
                            onClick = {
                                if (deleteConfirmArmed) {
                                    onDelete(initialClass)
                                } else {
                                    deleteConfirmArmed = true
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colors.danger
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_delete_class")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (deleteConfirmArmed) "Tap again to delete class" else "Delete class",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TimePickerButton(
    hour: Int,
    minute: Int,
    onTimeSelected: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RoutineTheme.colors
    var showDialog by remember { mutableStateOf(false) }

    val formatted = TimeUtil.formatTo12Hour(String.format("%02d:%02d", hour, minute))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, colors.line, RoundedCornerShape(10.dp))
            .background(colors.field)
            .clickable { showDialog = true }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${formatted.timeDigits} ${formatted.amPm}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.ink
            )
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Select Time",
                tint = colors.muted,
                modifier = Modifier.size(16.dp)
            )
        }
    }

    if (showDialog) {
        QuickTimePickerDialog(
            initialHour = hour,
            initialMinute = minute,
            onDismiss = { showDialog = false },
            onConfirm = { h, m ->
                onTimeSelected(h, m)
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val colors = RoutineTheme.colors
    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Select Time",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.ink
                )

                // Hour selector (00..23)
                Text("Hour (24-hour)", fontSize = 12.sp, color = colors.muted, fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val commonHours = listOf(8, 9, 10, 11, 14, 15, 16, 17, 18, 19, 20)
                    commonHours.forEach { h ->
                        val isSelected = selectedHour == h
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) colors.accent else colors.field)
                                .clickable { selectedHour = h }
                                .padding(horizontal = 9.dp, vertical = 6.dp)
                        ) {
                            val h12 = if (h == 0) 12 else if (h > 12) h - 12 else h
                            val amPm = if (h >= 12) "PM" else "AM"
                            Text(
                                text = "$h12 $amPm",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) colors.accentInk else colors.ink
                            )
                        }
                    }
                }

                // Minute selector (00, 15, 30, 45)
                Text("Minute", fontSize = 12.sp, color = colors.muted, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0, 15, 30, 45).forEach { m ->
                        val isSelected = selectedMinute == m
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) colors.accent else colors.field)
                                .clickable { selectedMinute = m }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = String.format("%02d", m),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) colors.accentInk else colors.ink
                            )
                        }
                    }
                }

                // Display selection
                val previewTime = TimeUtil.formatTo12Hour(String.format("%02d:%02d", selectedHour, selectedMinute))
                Text(
                    text = "Selected: ${previewTime.timeDigits} ${previewTime.amPm}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.accent
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(selectedHour, selectedMinute) },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Confirm", color = colors.accentInk)
                    }
                }
            }
        }
    }
}
