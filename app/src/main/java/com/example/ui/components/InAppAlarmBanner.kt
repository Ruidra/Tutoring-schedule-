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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
        initialValue = 0.99f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
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
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            alarms.forEach { alarm ->
                val fStart = TimeUtil.format12HourString(alarm.tuitionClass.start)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(scaleAnim)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.accent)
                        .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                        .testTag("alarm_banner_${alarm.key}")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "🔔 CLASS REMINDER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = colors.accentInk.copy(alpha = 0.85f)
                        )

                        Text(
                            text = "${alarm.tuitionClass.subject} is starting soon!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accentInk
                        )

                        val details = buildString {
                            append("Starts at $fStart")
                            if (!alarm.teacher?.name.isNullOrBlank()) {
                                append(" · ${alarm.teacher?.name}")
                            }
                            if (!alarm.teacher?.address.isNullOrBlank()) {
                                append(" · ${alarm.teacher?.address}")
                            }
                        }
                        Text(
                            text = details,
                            fontSize = 13.sp,
                            color = colors.accentInk.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // "Got it" button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.accentInk)
                                    .clickable { onDismiss(alarm.key) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                                    .testTag("alarm_dismiss_${alarm.key}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Got it",
                                    color = colors.accent,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // "Remind in 5 min" button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, colors.accentInk.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .clickable { onSnooze(alarm.key) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("alarm_snooze_${alarm.key}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Remind in 5 min",
                                    color = colors.accentInk,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
