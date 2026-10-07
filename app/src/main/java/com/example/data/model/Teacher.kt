package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val teaches: String = "",
    val address: String = "",
    val phone: String = ""
)
