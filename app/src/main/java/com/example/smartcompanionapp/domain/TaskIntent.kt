package com.example.smartcompanionapp.domain

import com.example.smartcompanionapp.data.model.Task

// ─────────────────────────────────────────────
// INTENT HANDLING
// ─────────────────────────────────────────────
// Sealed class that represents every possible user action on the Task screen.
// Instead of calling ViewModel functions directly, the UI sends an Intent.
// Data flow: UI → Intent → ViewModel → UiState → UI
sealed class TaskIntent {

    data class AddTask(
        val title: String,
        val description: String,   // Uses Task.description
        val subject: String,        // Uses Task.subject
        val date: String,           // Scheduled date — ScheduleScreen filters by this
        val dueDate: String         // Deadline — shown on TaskCard
    ) : TaskIntent()

    data class UpdateTask(
        val original: Task,
        val title: String,
        val description: String,
        val subject: String,
        val date: String,
        val dueDate: String
    ) : TaskIntent()

    data class DeleteTask(val task: Task)
        : TaskIntent()
}