package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tuition_classes")
data class TuitionClass(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val day: Int, // 0 = Sun, 1 = Mon, 2 = Tue, 3 = Wed, 4 = Thu, 5 = Fri, 6 = Sat
    val start: String, // "16:00" in 24-hour format
    val end: String, // "17:00"
    val subject: String,
    val teacherId: String? = null,
    val note: String? = null,
    val remindMinutes: Int = 30 // 0 = off, 5, 10, 15, 30, 45, 60...
)
