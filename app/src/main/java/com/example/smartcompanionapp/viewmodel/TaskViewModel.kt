package com.example.smartcompanionapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcompanionapp.data.TaskDao
import com.example.smartcompanionapp.data.model.Task
import com.example.smartcompanionapp.domain.TaskIntent
import com.example.smartcompanionapp.domain.TaskUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ✅ Now accepts TaskDao so tasks are saved to Room (persists across app restarts)
class TaskViewModel(private val dao: TaskDao) : ViewModel() {

    // ─────────────────────────────────────────────
    // IMMUTABLE UI STATE
    // ─────────────────────────────────────────────
    // _uiState is private/mutable — only the ViewModel writes to it.
    // uiState is the public read-only StateFlow the UI observes.
    private val _uiState = MutableStateFlow<TaskUiState>(TaskUiState.Loading)
    val uiState: StateFlow<TaskUiState> = _uiState

    init {
        // Load all tasks from Room when ViewModel is first created.
        // collect{} keeps listening — any DB change auto-updates the UI.
        viewModelScope.launch {
            dao.getAllTasks().collect { tasks ->
                _uiState.value = TaskUiState.Success(tasks)
            }
        }
    }

    // ─────────────────────────────────────────────
    // INTENT HANDLING
    // ─────────────────────────────────────────────
    // Single entry point for all UI actions.
    // UI fires an intent → ViewModel handles it → Room updates → StateFlow emits → UI recomposes
    fun processIntent(intent: TaskIntent) {
        when (intent) {
            is TaskIntent.AddTask    -> addTask(intent)
            is TaskIntent.UpdateTask -> updateTask(intent)
            is TaskIntent.DeleteTask -> deleteTask(intent)
        }
    }

    private fun addTask(intent: TaskIntent.AddTask) {
        viewModelScope.launch {
            dao.insertTask(
                Task(
                    title       = intent.title,
                    description = intent.description,
                    subject     = intent.subject,
                    date        = intent.date,
                    dueDate     = intent.dueDate
                )
            )
        }
    }

    private fun updateTask(intent: TaskIntent.UpdateTask) {
        viewModelScope.launch {
            dao.updateTask(
                intent.original.copy(
                    title       = intent.title,
                    description = intent.description,
                    subject     = intent.subject,
                    date        = intent.date,
                    dueDate     = intent.dueDate
                )
            )
        }
    }

    private fun deleteTask(intent: TaskIntent.DeleteTask) {
        viewModelScope.launch {
            dao.deleteTask(intent.task)
        }
    }
}