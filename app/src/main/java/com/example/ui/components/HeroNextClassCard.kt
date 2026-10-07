package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RoutineTheme
import com.example.util.NextClassInfo
import com.example.util.SubjectColorUtil

@Composable
fun HeroNextClassCard(
    nextClassInfo: NextClassInfo?,
    onPickTeacher: (classId: String, day: Int) -> Unit,
    onAddAddress: (teacherId: String) -> Unit,
    onAddFirstClass: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RoutineTheme.colors
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.heroBg)
            .padding(20.dp)
            .testTag("hero_next_class_card")
    ) {
        if (nextClassInfo == null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "NEXT CLASS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = colors.heroMuted
                )
                Text(
                    text = "No upcoming classes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.heroFg
                )
                Text(
                    text = "Add your tuition classes for the week and your countdown will appear here.",
                    fontSize = 14.sp,
                    color = colors.heroMuted,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable(onClick = onAddFirstClass)
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("hero_add_class_button")
                ) {
                    Text(
                        text = "+ Add a class now",
                        color = colors.heroFg,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Top row: Eyebrow + countdown pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = nextClassInfo.dayLabel.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = colors.heroMuted
                    )

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        AnimatedContent(
                            targetState = nextClassInfo.startsInOrEndsInText,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "startsIn"
                        ) { targetText ->
                            Text(
                                text = targetText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.heroFg
                            )
                        }
                    }
                }

                // Time digits: 40sp Display
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = nextClassInfo.startTimeFormatted.timeDigits,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = colors.heroFg
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = nextClassInfo.startTimeFormatted.amPm,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.heroMuted,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "→ ${nextClassInfo.endTimeFormatted.timeDigits} ${nextClassInfo.endTimeFormatted.amPm}",
                        fontSize = 14.sp,
                        color = colors.heroMuted,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                // Subject Tag
                val subjectColors = SubjectColorUtil.getColorsForSubject(
                    nextClassInfo.tuitionClass.subject,
                    isDark = isDark
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(subjectColors.background)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = nextClassInfo.tuitionClass.subject.ifEmpty { "Class" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = subjectColors.text
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Teacher info
                if (nextClassInfo.teacher != null) {
                    Text(
                        text = nextClassInfo.teacher.name.ifEmpty { "Teacher" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.heroFg
                    )

                    if (nextClassInfo.teacher.address.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                try {
                                    val mapIntent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("geo:0,0?q=" + Uri.encode(nextClassInfo.teacher.address))
                                    )
                                    context.startActivity(mapIntent)
                                } catch (_: Exception) {}
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Address",
                                tint = colors.heroMuted,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = nextClassInfo.teacher.address,
                                fontSize = 13.sp,
                                color = colors.heroMuted
                            )
                        }
                    } else {
                        Text(
                            text = "+ Add teacher's address",
                            fontSize = 13.sp,
                            color = colors.accent,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable {
                                onAddAddress(nextClassInfo.teacher.id)
                            }
                        )
                    }

                    if (nextClassInfo.teacher.phone.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .clickable {
                                    try {
                                        val dialIntent = Intent(
                                            Intent.ACTION_DIAL,
                                            Uri.parse("tel:${nextClassInfo.teacher.phone.trim()}")
                                        )
                                        context.startActivity(dialIntent)
                                    } catch (_: Exception) {}
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call",
                                tint = colors.accent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Call " + nextClassInfo.teacher.phone,
                                fontSize = 13.sp,
                                color = colors.heroFg,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No teacher assigned · Tap to select",
                        fontSize = 13.sp,
                        color = colors.heroMuted,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            onPickTeacher(nextClassInfo.tuitionClass.id, nextClassInfo.tuitionClass.day)
                        }
                    )
                }

                // Note if present
                if (!nextClassInfo.tuitionClass.note.isNullOrBlank()) {
                    Text(
                        text = "Note: ${nextClassInfo.tuitionClass.note}",
                        fontSize = 13.sp,
                        color = colors.heroMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}
