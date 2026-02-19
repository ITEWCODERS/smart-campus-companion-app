package com.example.smartcompanionapp.domain

import com.example.smartcompanionapp.data.model.Task

sealed interface TaskIntent {
    data class AddTask(val title: String, val dueDate: String) : TaskIntent
    data class UpdateTask(val task: Task, val title: String, val dueDate: String) : TaskIntent
    data class DeleteTask(val task: Task) : TaskIntent
}
