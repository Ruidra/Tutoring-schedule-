package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayInfo
import com.example.data.model.Teacher
import com.example.data.model.TuitionClass
import com.example.ui.components.ClassSlotCard
import com.example.ui.components.DayChipsRow
import com.example.ui.theme.RoutineTheme
import java.util.Calendar

@Composable
fun DayViewScreen(
    classes: List<TuitionClass>,
    teachers: List<Teacher>,
    selectedDay: Int,
    onSelectDay: (Int) -> Unit,
    onEditClass: (TuitionClass) -> Unit,
    onAddClass: (day: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RoutineTheme.colors
    val todayDayOfWeek = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1).let {
        if (it < 0) 6 else it
    }

    val dayInfo = DayInfo.getDayInfo(selectedDay)
    val isToday = selectedDay == todayDayOfWeek

    val dayClasses = classes
        .filter { it.day == selectedDay }
        .sortedBy { it.start }

    val teacherMap = teachers.associateBy { it.id }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("day_view_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 7 Day Chips Row
        DayChipsRow(
            selectedDay = selectedDay,
            onSelectDay = onSelectDay
        )

        // Day Header: Full name + "Today" pill + Class count
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = dayInfo.fullName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.ink
                )

                if (isToday) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.today)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "TODAY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Text(
                text = when (dayClasses.size) {
                    0 -> "No classes"
                    1 -> "1 class"
                    else -> "${dayClasses.size} classes"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colors.muted
            )
        }

        // Ruled Notebook Class List
        if (dayClasses.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, colors.line, RoundedCornerShape(14.dp))
                    .background(colors.surface)
            ) {
                Column {
                    dayClasses.forEachIndexed { index, tuitionClass ->
                        ClassSlotCard(
                            tuitionClass = tuitionClass,
                            teacher = teacherMap[tuitionClass.teacherId],
                            onEdit = { onEditClass(tuitionClass) }
                        )

                        if (index < dayClasses.size - 1) {
                            HorizontalDivider(
                                color = colors.line,
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }
        } else {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .drawBehind {
                        val stroke = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 14f), 0f)
                        )
                        drawRoundRect(
                            color = colors.line,
                            cornerRadius = CornerRadius(14.dp.toPx()),
                            style = stroke
                        )
                    }
                    .padding(vertical = 32.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Nothing planned for ${dayInfo.fullName}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.muted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap below to add a class on this day.",
                        fontSize = 13.sp,
                        color = colors.muted.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Add class button (dashed border style)
        val lineColor = colors.accent
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .drawBehind {
                    val stroke = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                    )
                    drawRoundRect(
                        color = lineColor,
                        cornerRadius = CornerRadius(12.dp.toPx()),
                        style = stroke
                    )
                }
                .clickable { onAddClass(selectedDay) }
                .padding(vertical = 12.dp)
                .testTag("btn_add_class_for_day"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+ Add a class on ${dayInfo.fullName}",
                color = colors.accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
