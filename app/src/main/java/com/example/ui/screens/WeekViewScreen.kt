package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayInfo
import com.example.data.model.TuitionClass
import com.example.ui.theme.RoutineTheme
import com.example.util.SubjectColorUtil
import com.example.util.TimeUtil
import java.util.Calendar

@Composable
fun WeekViewScreen(
    classes: List<TuitionClass>,
    onSelectDay: (Int) -> Unit,
    onEditClass: (TuitionClass) -> Unit,
    onAddClass: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RoutineTheme.colors
    val isDark = isSystemInDarkTheme()
    val todayDayOfWeek = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1).let {
        if (it < 0) 6 else it
    }

    // Collect all unique start times across the week, sorted chronologically
    val allStartTimes = classes
        .map { it.start }
        .distinct()
        .sortedBy { TimeUtil.parseToMinutes(it) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("week_view_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "This Week",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.ink
            )
            Text(
                text = "Tap a day to open",
                fontSize = 13.sp,
                color = colors.muted
            )
        }

        if (classes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, colors.line, RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No classes in your routine yet",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.muted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Add classes to see your full weekly timetable.",
                        fontSize = 13.sp,
                        color = colors.muted.copy(alpha = 0.8f)
                    )
                }
            }
        } else {
            // Scrollable timetable matrix table
            val scrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, colors.line, RoundedCornerShape(14.dp))
                    .background(colors.surface)
                    .horizontalScroll(scrollState)
            ) {
                Column {
                    // Header Row: "Day" column + distinct start times
                    Row(
                        modifier = Modifier
                            .background(colors.field)
                            .border(width = 0.5.dp, color = colors.line)
                    ) {
                        // Day column header
                        Box(
                            modifier = Modifier
                                .width(82.dp)
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "DAY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.muted,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Time columns
                        allStartTimes.forEach { timeStr ->
                            val f = TimeUtil.formatTo12Hour(timeStr)
                            Box(
                                modifier = Modifier
                                    .width(120.dp)
                                    .border(width = 0.5.dp, color = colors.line)
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "${f.timeDigits} ${f.amPm}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.muted
                                )
                            }
                        }
                    }

                    // Rows for each of the 7 days
                    DayInfo.ALL_DAYS.forEach { dayInfo ->
                        val isToday = dayInfo.dayOfWeek == todayDayOfWeek
                        val rowBg = if (isToday) colors.today else colors.surface

                        Row(
                            modifier = Modifier
                                .background(rowBg)
                                .border(width = 0.5.dp, color = colors.line)
                        ) {
                            // Sticky Day Header cell
                            Box(
                                modifier = Modifier
                                    .width(82.dp)
                                    .border(width = 0.5.dp, color = colors.line)
                                    .clickable { onSelectDay(dayInfo.dayOfWeek) }
                                    .padding(horizontal = 10.dp, vertical = 12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = dayInfo.shortName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isToday) colors.accent else colors.ink
                                    )
                                    Text(
                                        text = dayInfo.banglaName,
                                        fontSize = 11.sp,
                                        color = colors.muted
                                    )
                                }
                            }

                            // Class cells for each start time
                            allStartTimes.forEach { timeStr ->
                                val classesInSlot = classes.filter {
                                    it.day == dayInfo.dayOfWeek && it.start == timeStr
                                }

                                Box(
                                    modifier = Modifier
                                        .width(120.dp)
                                        .border(width = 0.5.dp, color = colors.line)
                                        .padding(6.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (classesInSlot.isNotEmpty()) {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            classesInSlot.forEach { c ->
                                                val subjectColors = SubjectColorUtil.getColorsForSubject(
                                                    c.subject,
                                                    isDark = isDark
                                                )

                                                Box(
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(subjectColors.background)
                                                        .clickable { onEditClass(c) }
                                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                                ) {
                                                    Text(
                                                        text = c.subject.ifEmpty { "Class" },
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = subjectColors.text,
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "—",
                                            fontSize = 13.sp,
                                            color = colors.line,
                                            modifier = Modifier.padding(start = 4.dp)
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
