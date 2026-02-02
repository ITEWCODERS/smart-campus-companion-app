package com.example.smartcompanionapp.model

data class Task( //FOR TASK SCREEN
    val title: String,
    val dueDate: String
)

data class Tasks( //FOR SCHEDULE SCREEN
    val title: String,
    val dueDate: String,
    val date: String
)