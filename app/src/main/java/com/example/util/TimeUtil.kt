package com.example.util

import com.example.data.model.DayInfo
import com.example.data.model.Teacher
import com.example.data.model.TuitionClass
import java.util.Calendar

data class FormattedTime(
    val timeDigits: String, // e.g. "4:00"
    val amPm: String // "AM" or "PM"
)

data class NextClassInfo(
    val tuitionClass: TuitionClass,
    val teacher: Teacher?,
    val isHappeningNow: Boolean,
    val startsInOrEndsInText: String, // e.g. "in 25 min", "ends in 15 min"
    val dayLabel: String, // e.g. "Next class · Today", "Next class · Tomorrow", "Next class · Saturday"
    val startTimeFormatted: FormattedTime,
    val endTimeFormatted: FormattedTime
)

object TimeUtil {

    fun parseToMinutes(timeStr: String): Int {
        val parts = timeStr.trim().split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return h * 60 + m
    }

    fun minutesToTimeStr(minutes: Int): String {
        val bounded = minutes.coerceIn(0, 23 * 60 + 59)
        val h = bounded / 60
        val m = bounded % 60
        return String.format("%02d:%02d", h, m)
    }

    fun formatTo12Hour(timeStr: String): FormattedTime {
        val totalMinutes = parseToMinutes(timeStr)
        val h24 = totalMinutes / 60
        val m = totalMinutes % 60
        val isPm = h24 >= 12
        val h12 = when {
            h24 == 0 -> 12
            h24 > 12 -> h24 - 12
            else -> h24
        }
        val amPm = if (isPm) "PM" else "AM"
        return FormattedTime(
            timeDigits = String.format("%d:%02d", h12, m),
            amPm = amPm
        )
    }

    fun format12HourString(timeStr: String): String {
        val f = formatTo12Hour(timeStr)
        return "${f.timeDigits} ${f.amPm}"
    }

    fun formatDuration(ms: Long): String {
        val totalMinutes = (ms / 60000).coerceAtLeast(1)
        if (totalMinutes < 60) {
            return "$totalMinutes min"
        }
        val hours = totalMinutes / 60
        val remainingMinutes = totalMinutes % 60
        if (hours < 24) {
            return if (remainingMinutes > 0) "${hours}h ${remainingMinutes}m" else "${hours}h"
        }
        val days = hours / 24
        val remHours = hours % 24
        return if (remHours > 0) "${days}d ${remHours}h" else "${days}d"
    }

    /**
     * Finds the next scheduled or currently active class across the week.
     */
    fun findNextClass(
        classes: List<TuitionClass>,
        teachers: List<Teacher>,
        nowCalendar: Calendar = Calendar.getInstance()
    ): NextClassInfo? {
        if (classes.isEmpty()) return null

        val teacherMap = teachers.associateBy { it.id }
        val nowMillis = nowCalendar.timeInMillis
        val currentDayOfWeek = nowCalendar.get(Calendar.DAY_OF_WEEK) - 1 // Calendar.SUNDAY = 1 -> 0

        var bestCandidate: NextClassCandidate? = null

        // Search through the next 8 days
        for (dayOffset in 0..7) {
            val checkDayOfWeek = (currentDayOfWeek + dayOffset) % 7
            val classesOnDay = classes.filter { it.day == checkDayOfWeek }

            for (c in classesOnDay) {
                val startMin = parseToMinutes(c.start)
                val endMin = parseToMinutes(c.end)

                val classStartCal = (nowCalendar.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, dayOffset)
                    set(Calendar.HOUR_OF_DAY, startMin / 60)
                    set(Calendar.MINUTE, startMin % 60)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val classEndCal = (nowCalendar.clone() as Calendar).apply {
                    add(Calendar.DAY_OF_YEAR, dayOffset)
                    set(Calendar.HOUR_OF_DAY, endMin / 60)
                    set(Calendar.MINUTE, endMin % 60)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val startMillis = classStartCal.timeInMillis
                val endMillis = classEndCal.timeInMillis

                // If already ended in the past, skip
                if (endMillis <= nowMillis) continue

                if (bestCandidate == null || startMillis < bestCandidate.startMillis) {
                    bestCandidate = NextClassCandidate(
                        tuitionClass = c,
                        teacher = teacherMap[c.teacherId],
                        dayOffset = dayOffset,
                        startMillis = startMillis,
                        endMillis = endMillis
                    )
                }
            }
        }

        val cand = bestCandidate ?: return null
        val isHappeningNow = cand.startMillis <= nowMillis

        val startsInOrEndsInText = if (isHappeningNow) {
            "ends in " + formatDuration(cand.endMillis - nowMillis)
        } else {
            "in " + formatDuration(cand.startMillis - nowMillis)
        }

        val dayLabel = when {
            isHappeningNow -> "Happening now"
            cand.dayOffset == 0 -> "Next class · Today"
            cand.dayOffset == 1 -> "Next class · Tomorrow"
            else -> "Next class · " + DayInfo.getDayInfo(cand.tuitionClass.day).fullName
        }

        return NextClassInfo(
            tuitionClass = cand.tuitionClass,
            teacher = cand.teacher,
            isHappeningNow = isHappeningNow,
            startsInOrEndsInText = startsInOrEndsInText,
            dayLabel = dayLabel,
            startTimeFormatted = formatTo12Hour(cand.tuitionClass.start),
            endTimeFormatted = formatTo12Hour(cand.tuitionClass.end)
        )
    }

    private data class NextClassCandidate(
        val tuitionClass: TuitionClass,
        val teacher: Teacher?,
        val dayOffset: Int,
        val startMillis: Long,
        val endMillis: Long
    )
}
