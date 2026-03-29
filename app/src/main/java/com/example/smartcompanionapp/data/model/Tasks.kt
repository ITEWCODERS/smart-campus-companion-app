package com.example.smartcompanionapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey


// ✅ ONE unified Room entity used by BOTH TaskScreen and ScheduleScreen.
// - TaskScreen uses all fields
// - ScheduleScreen filters by `date`, displays title/subject/dueDate
// The old separate "Tasks" class in com.example.smartcompanionapp.model can be DELETED.
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val title: String,
    val description: String,  // Shown on TaskCard
    val subject: String,       // Shown on TaskCard and ScheduleCard
    val date: String,          // "dd/MM/yyyy" — ScheduleScreen filters by this
    val dueDate: String        // "dd/MM/yyyy" — shown as deadline on both screens
)
