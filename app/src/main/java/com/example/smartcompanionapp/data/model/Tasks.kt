package com.example.smartcompanionapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks") // FOR TASK SCREEN
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val dueDate: String
)

@Entity(tableName = "schedule_tasks") // FOR SCHEDULE SCREEN
data class Tasks(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val dueDate: String,
    val date: String
)