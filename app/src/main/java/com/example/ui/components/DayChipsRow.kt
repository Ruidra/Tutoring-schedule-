package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DayInfo
import com.example.ui.theme.RoutineTheme
import java.util.Calendar

@Composable
fun DayChipsRow(
    selectedDay: Int,
    onSelectDay: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = RoutineTheme.colors
    val todayDayOfWeek = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1).let {
        if (it < 0) 6 else it
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("day_chips_row"),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        DayInfo.ALL_DAYS.forEach { dayInfo ->
            val isSelected = dayInfo.dayOfWeek == selectedDay
            val isToday = dayInfo.dayOfWeek == todayDayOfWeek

            val chipBg by animateColorAsState(
                targetValue = if (isSelected) colors.ink else colors.surface,
                label = "chipBg"
            )
            val chipBorder by animateColorAsState(
                targetValue = if (isSelected) colors.ink else colors.line,
                label = "chipBorder"
            )
            val enTextColor by animateColorAsState(
                targetValue = if (isSelected) colors.bg else colors.ink,
                label = "enTextColor"
            )
            val bnTextColor by animateColorAsState(
                targetValue = if (isSelected) colors.bg.copy(alpha = 0.75f) else colors.muted,
                label = "bnTextColor"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, chipBorder, RoundedCornerShape(10.dp))
                    .background(chipBg)
                    .clickable { onSelectDay(dayInfo.dayOfWeek) }
                    .padding(vertical = 7.dp)
                    .testTag("day_chip_${dayInfo.shortName.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayInfo.shortName,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        color = enTextColor,
                        lineHeight = 16.sp
                    )
                    Text(
                        text = dayInfo.banglaName,
                        fontSize = 11.sp,
                        color = bnTextColor,
                        lineHeight = 14.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Small indicator dot for Today
                    if (isToday) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) colors.bg else colors.accent)
                        )
                    } else {
                        Spacer(modifier = Modifier.size(5.dp))
                    }
                }
            }
        }
    }
}
