package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.RoutineTab
import com.example.ui.TuitionViewModel
import com.example.ui.components.ClassEditorDialog
import com.example.ui.components.HeroNextClassCard
import com.example.ui.components.InAppAlarmBanner
import com.example.ui.components.TeacherEditorDialog
import com.example.ui.theme.RoutineTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(
    viewModel: TuitionViewModel,
    modifier: Modifier = Modifier
) {
    val colors = RoutineTheme.colors
    val context = LocalContext.current

    val classes by viewModel.classes.collectAsState()
    val teachers by viewModel.teachers.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val nextClassInfo by viewModel.nextClassInfo.collectAsState()
    val activeAlarms by viewModel.activeAlarms.collectAsState()
    val remindersEnabled by viewModel.remindersEnabled.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    val editingClass by viewModel.editingClass.collectAsState()
    val isCreatingNewClass by viewModel.isCreatingNewClass.collectAsState()
    val editingTeacher by viewModel.editingTeacher.collectAsState()
    val isCreatingNewTeacher by viewModel.isCreatingNewTeacher.collectAsState()

    // Back handler: if on a secondary tab, return to Day tab
    BackHandler(enabled = selectedTab != RoutineTab.DAY) {
        viewModel.selectTab(RoutineTab.DAY)
    }

    val todayFormatted = remember {
        val sdf = SimpleDateFormat("EEEE, d MMMM", Locale.ENGLISH)
        sdf.format(Date())
    }

    Scaffold(
        containerColor = colors.bg,
        floatingActionButton = {
            if (selectedTab == RoutineTab.DAY || selectedTab == RoutineTab.WEEK) {
                FloatingActionButton(
                    onClick = { viewModel.openNewClass(selectedDay) },
                    containerColor = colors.accent,
                    contentColor = colors.accentInk,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_add_class")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Class",
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else if (selectedTab == RoutineTab.TEACHERS) {
                FloatingActionButton(
                    onClick = { viewModel.openNewTeacher() },
                    containerColor = colors.accent,
                    contentColor = colors.accentInk,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_add_teacher")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Teacher",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 720.dp) // Responsive boundary for tablets & large screens
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App Top Title & Eyebrow date
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = todayFormatted.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = colors.muted
                    )
                    Text(
                        text = "Tuition Routine",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = colors.ink
                    )
                }

                // In-App Alarm Ringing Banners
                InAppAlarmBanner(
                    alarms = activeAlarms,
                    onDismiss = { viewModel.dismissAlarm(it) },
                    onSnooze = { viewModel.snoozeAlarm(it) }
                )

                // Hero Next-Class Card on Top
                HeroNextClassCard(
                    nextClassInfo = nextClassInfo,
                    onPickTeacher = { classId, day ->
                        viewModel.selectDay(day)
                        val target = classes.firstOrNull { it.id == classId }
                        if (target != null) viewModel.openEditClass(target)
                    },
                    onAddAddress = { teacherId ->
                        val t = teachers.firstOrNull { it.id == teacherId }
                        if (t != null) viewModel.openEditTeacher(t)
                    },
                    onAddFirstClass = {
                        viewModel.openNewClass(selectedDay)
                    }
                )

                // Navigation Tabs (Day / Week / Teachers / Settings)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, colors.line, RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .padding(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TabButton(
                            title = "Day",
                            isSelected = selectedTab == RoutineTab.DAY,
                            onClick = { viewModel.selectTab(RoutineTab.DAY) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tab_day")
                        )
                        TabButton(
                            title = "Week",
                            isSelected = selectedTab == RoutineTab.WEEK,
                            onClick = { viewModel.selectTab(RoutineTab.WEEK) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tab_week")
                        )
                        TabButton(
                            title = "Teachers",
                            isSelected = selectedTab == RoutineTab.TEACHERS,
                            onClick = { viewModel.selectTab(RoutineTab.TEACHERS) },
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("tab_teachers")
                        )
                        TabButton(
                            title = "Settings",
                            isSelected = selectedTab == RoutineTab.SETTINGS,
                            onClick = { viewModel.selectTab(RoutineTab.SETTINGS) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tab_settings")
                        )
                    }
                }

                // Status Message Feedback
                AnimatedVisibility(
                    visible = !statusMessage.isNullOrBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.today)
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = statusMessage ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.accent
                        )
                    }
                }

                // Active View Content with smooth animated transition
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        if (targetState.ordinal > initialState.ordinal) {
                            (slideInHorizontally { width -> width / 3 } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> -width / 3 } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width / 3 } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> width / 3 } + fadeOut())
                        }
                    },
                    label = "tabContent"
                ) { tab ->
                    when (tab) {
                        RoutineTab.DAY -> {
                            DayViewScreen(
                                classes = classes,
                                teachers = teachers,
                                selectedDay = selectedDay,
                                onSelectDay = { viewModel.selectDay(it) },
                                onEditClass = { viewModel.openEditClass(it) },
                                onAddClass = { viewModel.openNewClass(it) }
                            )
                        }
                        RoutineTab.WEEK -> {
                            WeekViewScreen(
                                classes = classes,
                                onSelectDay = { day ->
                                    viewModel.selectDay(day)
                                    viewModel.selectTab(RoutineTab.DAY)
                                },
                                onEditClass = { viewModel.openEditClass(it) },
                                onAddClass = { viewModel.openNewClass(selectedDay) }
                            )
                        }
                        RoutineTab.TEACHERS -> {
                            TeachersViewScreen(
                                teachers = teachers,
                                classes = classes,
                                onAddTeacher = { viewModel.openNewTeacher() },
                                onEditTeacher = { viewModel.openEditTeacher(it) }
                            )
                        }
                        RoutineTab.SETTINGS -> {
                            SettingsScreen(
                                remindersEnabled = remindersEnabled,
                                onToggleReminders = { viewModel.toggleReminders() },
                                onTestAlarm = { viewModel.testAlarm(context) },
                                themeMode = themeMode,
                                onSetThemeMode = { viewModel.setThemeMode(it) },
                                onPreloadDemo = { viewModel.resetToDemoData() },
                                classes = classes,
                                teachers = teachers
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(72.dp)) // Padding for FAB
            }
        }
    }

    // Modal Class Editor
    if (editingClass != null) {
        val existingSubjects = classes.map { it.subject }.filter { it.isNotBlank() }
        ClassEditorDialog(
            initialClass = editingClass!!,
            isNew = isCreatingNewClass,
            teachers = teachers,
            allExistingSubjects = existingSubjects,
            onDismiss = { viewModel.closeClassEditor() },
            onSave = { viewModel.saveClass(it) },
            onDelete = { viewModel.deleteClass(it) }
        )
    }

    // Modal Teacher Editor
    if (editingTeacher != null) {
        TeacherEditorDialog(
            initialTeacher = editingTeacher!!,
            isNew = isCreatingNewTeacher,
            onDismiss = { viewModel.closeTeacherEditor() },
            onSave = { viewModel.saveTeacher(it) },
            onDelete = { viewModel.deleteTeacher(it) }
        )
    }
}

@Composable
private fun TabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RoutineTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (isSelected) colors.accent else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) colors.accentInk else colors.muted
        )
    }
}
