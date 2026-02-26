package com.example.smartcompanionapp.domain

import com.example.smartcompanionapp.data.model.Task

sealed interface TaskUiState {
    object Loading : TaskUiState
    data class Success(val tasks: List<Task>) : TaskUiState
    data class Error(val message: String) : TaskUiState
}
