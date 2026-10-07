package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayInfo
import com.example.data.model.Teacher
import com.example.data.model.TuitionClass
import com.example.ui.theme.RoutineTheme
import com.example.util.TimeUtil

@Composable
fun SettingsScreen(
    remindersEnabled: Boolean,
    onToggleReminders: () -> Unit,
    onTestAlarm: () -> Unit,
    themeMode: String,
    onSetThemeMode: (String) -> Unit,
    onPreloadDemo: () -> Unit,
    classes: List<TuitionClass>,
    teachers: List<Teacher>,
    modifier: Modifier = Modifier
) {
    val colors = RoutineTheme.colors
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onTestAlarm()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section title
        Text(
            text = "Settings",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = colors.ink
        )

        // 1. Reminders & Alarms Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, colors.line, RoundedCornerShape(14.dp))
                .background(colors.surface)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.today),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Class Reminders",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.ink
                            )
                            Text(
                                text = if (remindersEnabled) "Active · Notifications will alert you" else "Turned off",
                                fontSize = 12.sp,
                                color = colors.muted
                            )
                        }
                    }

                    Switch(
                        checked = remindersEnabled,
                        onCheckedChange = { onToggleReminders() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.accentInk,
                            checkedTrackColor = colors.accent,
                            uncheckedThumbColor = colors.muted,
                            uncheckedTrackColor = colors.field
                        ),
                        modifier = Modifier.testTag("switch_class_reminders")
                    )
                }

                Text(
                    text = "Alarms ring according to each class's reminder time (e.g. 30 min before). Tap 'Test alarm' to verify sound and notification delivery.",
                    fontSize = 12.sp,
                    color = colors.muted,
                    lineHeight = 16.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onTestAlarm()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_test_alarm")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Test Alarm & Sound",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.accent
                        )
                    }
                }
            }
        }

        // 2. Theme / Appearance Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, colors.line, RoundedCornerShape(14.dp))
                .background(colors.surface)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Appearance & Theme",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.ink
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val options = listOf(
                        Triple("SYSTEM", "System", Icons.Default.Smartphone),
                        Triple("LIGHT", "Light", Icons.Default.LightMode),
                        Triple("DARK", "Dark", Icons.Default.DarkMode)
                    )

                    options.forEach { (mode, label, icon) ->
                        val isSelected = themeMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) colors.accent else colors.field)
                                .border(1.dp, if (isSelected) colors.accent else colors.line, RoundedCornerShape(10.dp))
                                .clickable { onSetThemeMode(mode) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) colors.accentInk else colors.muted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) colors.accentInk else colors.ink
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Routine Utilities & Export
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, colors.line, RoundedCornerShape(14.dp))
                .background(colors.surface)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Routine Management",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.ink
                )

                // Share / Export Routine
                OutlinedButton(
                    onClick = {
                        val teacherMap = teachers.associateBy { it.id }
                        val summaryText = buildString {
                            appendLine("📚 MY TUITION ROUTINE")
                            appendLine("====================")
                            DayInfo.ALL_DAYS.forEach { day ->
                                val dayClasses = classes.filter { it.day == day.dayOfWeek }.sortedBy { it.start }
                                if (dayClasses.isNotEmpty()) {
                                    appendLine("\n${day.fullName.uppercase()} (${day.banglaName}):")
                                    dayClasses.forEach { c ->
                                        val tName = teacherMap[c.teacherId]?.name ?: "No teacher"
                                        val tTime = "${TimeUtil.format12HourString(c.start)} - ${TimeUtil.format12HourString(c.end)}"
                                        appendLine(" • ${c.subject} ($tTime) - $tName")
                                    }
                                }
                            }
                        }
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Tuition Routine Schedule")
                            putExtra(Intent.EXTRA_TEXT, summaryText)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Routine"))
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_share_routine")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Share Routine as Text",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.accent
                    )
                }

                // Preload sample demo data
                OutlinedButton(
                    onClick = onPreloadDemo,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_preload_demo")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reset / Preload Demo Schedule",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.ink
                    )
                }
            }
        }

        // 4. About card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.field)
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = colors.muted,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "Tuition Routine",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink
                    )
                    Text(
                        text = "All data is securely stored locally on this device. You can access your full schedule offline anytime.",
                        fontSize = 12.sp,
                        color = colors.muted,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
