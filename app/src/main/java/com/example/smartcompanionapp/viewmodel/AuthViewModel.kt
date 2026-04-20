package com.example.smartcompanionapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcompanionapp.data.repository.AuthRepository
import com.example.smartcompanionapp.data.database.authentication.AuthDatabase
import com.example.smartcompanionapp.data.database.authentication.UserEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AuthRepository
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        val authDao = AuthDatabase.getDatabase(application).authDao()
        val firestore = FirebaseFirestore.getInstance()
        repository = AuthRepository(authDao, firestore)
    }

    fun signUp(username: String, email: String, password: String, role: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            if (repository.isEmailExists(email)) {
                _authState.value = AuthState.Error("Email already exists")
                return@launch
            }
            
            val user = UserEntity(username = username, email = email, password = password, role = role)
            val result = repository.insertUser(user)
            if (result != -1L) {
                _authState.value = AuthState.Success(user)
            } else {
                _authState.value = AuthState.Error("Registration failed")
            }
        }
    }

    fun login(email: String, password: String, role: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val user = repository.login(email, password, role)
            if (user != null) {
                _authState.value = AuthState.Success(user)
            } else {
                _authState.value = AuthState.Error("Invalid email, password or role")
            }
        }
    }
    
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserEntity) : AuthState()
    data class Error(val message: String) : AuthState()
}
