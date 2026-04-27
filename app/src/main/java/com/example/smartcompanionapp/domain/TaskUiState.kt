package com.example.smartcompanionapp.domain

import com.example.smartcompanionapp.data.model.Task

// ─────────────────────────────────────────────
// IMMUTABLE UI STATE
// ─────────────────────────────────────────────
// Sealed class for every possible UI state.
// IMMUTABLE — the ViewModel always emits a brand-new state object, never mutates the existing one.
sealed class TaskUiState {

    object Loading : TaskUiState()

    data class Success(val tasks: List<Task>) : TaskUiState()

    data class Error(val message: String) : TaskUiState()
}