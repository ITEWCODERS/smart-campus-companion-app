package com.example.smartcompanionapp.model

data class Task(
    val title: String,
    val dueDate: String,

    // Added a day for a task (Ex: Monday, tuesday and so on)
    val day: String

)