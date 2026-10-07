package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayInfo
import com.example.data.model.Teacher
import com.example.data.model.TuitionClass
import com.example.ui.theme.RoutineTheme

@Composable
fun TeachersViewScreen(
    teachers: List<Teacher>,
    classes: List<TuitionClass>,
    onAddTeacher: () -> Unit,
    onEditTeacher: (Teacher) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RoutineTheme.colors
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("teachers_view_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Teachers",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.ink
            )
            Text(
                text = "${teachers.size} teachers",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colors.muted
            )
        }

        Text(
            text = "Set each teacher's address once. It automatically links to every class they teach.",
            fontSize = 13.sp,
            color = colors.muted,
            lineHeight = 18.sp
        )

        if (teachers.isEmpty()) {
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
                        text = "No teachers added yet",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.muted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Add your private tutors, batch teachers, or coaching faculty.",
                        fontSize = 13.sp,
                        color = colors.muted.copy(alpha = 0.8f)
                    )
                }
            }
        } else {
            teachers.forEach { teacher ->
                val assignedClasses = classes.filter { it.teacherId == teacher.id }
                val teachingDays = DayInfo.ALL_DAYS
                    .filter { day -> assignedClasses.any { it.day == day.dayOfWeek } }
                    .joinToString(", ") { it.shortName }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, colors.line, RoundedCornerShape(14.dp))
                        .background(colors.surface)
                        .padding(16.dp)
                        .testTag("teacher_card_${teacher.id}")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Top row: Avatar + Name & Subject
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(colors.today),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = teacher.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.ink
                                )
                                if (teacher.teaches.isNotBlank()) {
                                    Text(
                                        text = teacher.teaches,
                                        fontSize = 13.sp,
                                        color = colors.muted
                                    )
                                }
                            }

                            // Edit button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, colors.line, RoundedCornerShape(8.dp))
                                    .clickable { onEditTeacher(teacher) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("btn_edit_teacher_${teacher.id}")
                            ) {
                                Text(
                                    text = "Edit",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.ink
                                )
                            }
                        }

                        // Address Row
                        if (teacher.address.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
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
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = teacher.address,
                                    fontSize = 13.sp,
                                    color = colors.muted,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy address",
                                    tint = colors.accent,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                            clipboard?.setPrimaryClip(ClipData.newPlainText("Teacher Address", teacher.address))
                                            Toast.makeText(context, "Address copied to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                )
                            }
                        }

                        // Phone Row with quick Call
                        if (teacher.phone.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
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
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = teacher.phone,
                                    fontSize = 13.sp,
                                    color = colors.accent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Class schedule summary
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.field)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (assignedClasses.isNotEmpty()) {
                                    "${assignedClasses.size} ${if (assignedClasses.size == 1) "class" else "classes"} a week: $teachingDays"
                                } else {
                                    "No classes assigned to this teacher yet"
                                },
                                fontSize = 12.sp,
                                color = colors.muted
                            )
                        }
                    }
                }
            }
        }

        // Add Teacher Button
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
                .clickable(onClick = onAddTeacher)
                .padding(vertical = 12.dp)
                .testTag("btn_add_teacher"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+ Add teacher",
                color = colors.accent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
