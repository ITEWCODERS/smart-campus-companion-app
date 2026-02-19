package com.example.smartcompanionapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcompanionapp.data.TaskDao
import com.example.smartcompanionapp.data.model.Task
import com.example.smartcompanionapp.domain.TaskIntent
import com.example.smartcompanionapp.domain.TaskUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class TaskViewModel(private val dao: TaskDao) : ViewModel() {

    private val _uiState = MutableStateFlow<TaskUiState>(TaskUiState.Loading)
    val uiState: StateFlow<TaskUiState> = _uiState

    init {
        dao.getAllTasks()
            .onEach { tasks ->
                _uiState.value = TaskUiState.Success(tasks)
            }
            .catch { exception ->
                _uiState.value = TaskUiState.Error(exception.message ?: "An unknown error occurred")
            }
            .launchIn(viewModelScope)
    }

    fun processIntent(intent: TaskIntent) {
        when (intent) {
            is TaskIntent.AddTask -> addTask(intent.title, intent.dueDate)
            is TaskIntent.UpdateTask -> updateTask(intent.task, intent.title, intent.dueDate)
            is TaskIntent.DeleteTask -> deleteTask(intent.task)
        }
    }

    private fun addTask(title: String, dueDate: String) {
        viewModelScope.launch {
            dao.insertTask(Task(title = title, dueDate = dueDate))
        }
    }

    private fun updateTask(task: Task, title: String, dueDate: String) {
        viewModelScope.launch {
            dao.updateTask(task.copy(title = title, dueDate = dueDate))
        }
    }

    private fun deleteTask(task: Task) {
        viewModelScope.launch {
            dao.deleteTask(task)
        }
    }
}
