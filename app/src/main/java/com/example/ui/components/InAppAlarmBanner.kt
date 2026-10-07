package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ActiveAlarmItem
import com.example.ui.theme.RoutineTheme
import com.example.util.TimeUtil

@Composable
fun InAppAlarmBanner(
    alarms: List<ActiveAlarmItem>,
    onDismiss: (key: String) -> Unit,
    onSnooze: (key: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RoutineTheme.colors

    val infiniteTransition = rememberInfiniteTransition(label = "alarmPulse")
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    AnimatedVisibility(
        visible = alarms.isNotEmpty(),
        enter = expandVertically(),
        exit = shrinkVertically(),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            alarms.forEach { alarm ->
                val fStart = TimeUtil.format12HourString(alarm.tuitionClass.start)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(scaleAnim)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.accent)
                        .border(2.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                        .testTag("alarm_banner_${alarm.key}")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = colors.accentInk,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "LOUD TUITION ALARM RINGING",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = colors.accentInk.copy(alpha = 0.95f)
                            )
                        }

                        Text(
                            text = "Time for ${alarm.tuitionClass.subject}!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accentInk
                        )

                        val details = buildString {
                            append("Starts at $fStart")
                            if (!alarm.teacher?.name.isNullOrBlank()) {
                                append(" with ${alarm.teacher?.name}")
                            }
                            if (!alarm.teacher?.address.isNullOrBlank()) {
                                append(" (${alarm.teacher?.address})")
                            }
                        }
                        Text(
                            text = details,
                            fontSize = 13.sp,
                            color = colors.accentInk.copy(alpha = 0.9f)
                        )

                        Text(
                            text = "ℹ️ If not stopped, alarm will sound again after a 5 min gap.",
                            fontSize = 11.sp,
                            color = colors.accentInk.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Primary "STOP ALARM" button (I'm awake!)
                            Button(
                                onClick = { onDismiss(alarm.key) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accentInk,
                                    contentColor = colors.accent
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(44.dp)
                                    .testTag("alarm_dismiss_${alarm.key}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Alarm,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "STOP ALARM",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            // Secondary "Snooze 5m" button
                            OutlinedButton(
                                onClick = { onSnooze(alarm.key) },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = colors.accentInk
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    colors.accentInk.copy(alpha = 0.7f)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("alarm_snooze_${alarm.key}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Snooze,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Snooze 5m",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
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
