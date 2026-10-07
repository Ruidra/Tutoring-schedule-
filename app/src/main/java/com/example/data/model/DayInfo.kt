package com.example.data.model

data class DayInfo(
    val dayOfWeek: Int, // 0 = Sun, 1 = Mon, 2 = Tue, 3 = Wed, 4 = Thu, 5 = Fri, 6 = Sat
    val shortName: String,
    val fullName: String,
    val banglaName: String
) {
    companion object {
        // Standard academic week starting Saturday (as in the tuition routine HTML)
        val ALL_DAYS = listOf(
            DayInfo(dayOfWeek = 6, shortName = "Sat", fullName = "Saturday", banglaName = "শনি"),
            DayInfo(dayOfWeek = 0, shortName = "Sun", fullName = "Sunday", banglaName = "রবি"),
            DayInfo(dayOfWeek = 1, shortName = "Mon", fullName = "Monday", banglaName = "সোম"),
            DayInfo(dayOfWeek = 2, shortName = "Tue", fullName = "Tuesday", banglaName = "মঙ্গল"),
            DayInfo(dayOfWeek = 3, shortName = "Wed", fullName = "Wednesday", banglaName = "বুধ"),
            DayInfo(dayOfWeek = 4, shortName = "Thu", fullName = "Thursday", banglaName = "বৃহ"),
            DayInfo(dayOfWeek = 5, shortName = "Fri", fullName = "Friday", banglaName = "শুক্র")
        )

        fun getDayInfo(dayOfWeek: Int): DayInfo {
            return ALL_DAYS.firstOrNull { it.dayOfWeek == dayOfWeek } ?: ALL_DAYS[0]
        }
    }
}
