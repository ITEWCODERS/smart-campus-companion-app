package com.example.smartcompanionapp.model

data class Task( //FOR TASK SCREEN
    val title: String,
    val dueDate: String
)
data class Tasks(
    val id: Long = System.currentTimeMillis(), // Unique ID based on creation time
    val title: String,
    val description: String = "",
    val subject: String = "",
    val date: String,       // The scheduled/added date (dd/MM/yyyy) → used by ScheduleScreen to filter tasks by day
    val dueDate: String     // The deadline date (dd/MM/yyyy) → shown on TaskCard as "Due: ..."
)