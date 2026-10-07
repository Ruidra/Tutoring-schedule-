package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Teacher
import com.example.data.model.TuitionClass
import com.example.ui.theme.RoutineTheme
import com.example.util.SubjectColorUtil
import com.example.util.TimeUtil

@Composable
fun ClassSlotCard(
    tuitionClass: TuitionClass,
    teacher: Teacher?,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RoutineTheme.colors
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    val startTimeFormatted = TimeUtil.formatTo12Hour(tuitionClass.start)
    val endTimeFormatted = TimeUtil.format12HourString(tuitionClass.end)
    val subjectColors = SubjectColorUtil.getColorsForSubject(tuitionClass.subject, isDark = isDark)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .padding(vertical = 12.dp, horizontal = 14.dp)
            .testTag("class_slot_${tuitionClass.id}"),
        verticalAlignment = Alignment.Top
    ) {
        // Left column: Time (76dp width)
        Column(
            modifier = Modifier.width(76.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = startTimeFormatted.timeDigits,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.ink,
                letterSpacing = (-0.5).sp,
                lineHeight = 22.sp
            )
            Text(
                text = startTimeFormatted.amPm,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.muted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "to $endTimeFormatted",
                fontSize = 11.sp,
                color = colors.muted,
                lineHeight = 14.sp
            )
        }

        // Vertical notebook ruling line
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(64.dp)
                .background(colors.line)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // Center column: Subject, teacher, note, reminder
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Subject Chip
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(subjectColors.background)
                    .padding(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(
                    text = tuitionClass.subject.ifEmpty { "Class" },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = subjectColors.text
                )
            }

            // Teacher Name
            if (teacher != null && teacher.name.isNotBlank()) {
                Text(
                    text = teacher.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.ink
                )

                if (teacher.address.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            try {
                                val mapIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("geo:0,0?q=" + Uri.encode(teacher.address))
                                )
                                context.startActivity(mapIntent)
                            } catch (_: Exception) {}
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Address",
                            tint = colors.muted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = teacher.address,
                            fontSize = 12.sp,
                            color = colors.muted,
                            maxLines = 1
                        )
                    }
                }

                if (teacher.phone.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            try {
                                val dialIntent = Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.parse("tel:${teacher.phone.trim()}")
                                )
                                context.startActivity(dialIntent)
                            } catch (_: Exception) {}
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call",
                            tint = colors.accent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = teacher.phone,
                            fontSize = 12.sp,
                            color = colors.accent,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                Text(
                    text = "No teacher assigned",
                    fontSize = 12.sp,
                    color = colors.muted,
                    fontStyle = FontStyle.Italic
                )
            }

            // Note
            if (!tuitionClass.note.isNullOrBlank()) {
                Text(
                    text = "“${tuitionClass.note}”",
                    fontSize = 12.sp,
                    color = colors.muted,
                    fontStyle = FontStyle.Italic
                )
            }

            // Reminder indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (tuitionClass.remindMinutes > 0) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Reminder set",
                        tint = colors.accent,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${tuitionClass.remindMinutes} min before",
                        fontSize = 11.sp,
                        color = colors.muted
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = "Reminder off",
                        tint = colors.muted,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "No reminder",
                        fontSize = 11.sp,
                        color = colors.muted
                    )
                }
            }
        }

        // Right column: Edit button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, colors.line, RoundedCornerShape(8.dp))
                .background(colors.surface)
                .clickable(onClick = onEdit)
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("edit_class_${tuitionClass.id}"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit class",
                    tint = colors.muted,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Edit",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.ink
                )
            }
        }
    }
}
