package com.example.smartcompanionapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcompanionapp.data.model.Task
import com.example.smartcompanionapp.data.repository.TaskRepository
import com.example.smartcompanionapp.domain.TaskIntent
import com.example.smartcompanionapp.domain.TaskUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository, private val userId: String) : ViewModel() {

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
            repository.getAllTasks(userId).collect { tasks ->
                _uiState.value = TaskUiState.Success(tasks)
            }
        }

        // Sync from Firestore to Room on init to keep local DB updated
        viewModelScope.launch {
            repository.refreshTasksFromFirestore(userId)
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
            repository.insertTask(
                Task(
                    userId      = userId,
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
            repository.updateTask(
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
            repository.deleteTask(intent.task)
        }
    }
}
