package com.example.smartcompanionapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcompanionapp.data.repository.AuthRepository
import com.example.smartcompanionapp.data.database.authentication.AuthDatabase
import com.example.smartcompanionapp.data.database.authentication.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AuthRepository
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    init {
        val authDao = AuthDatabase.getDatabase(application).authDao()
        repository = AuthRepository(authDao, firestore)
    }

    fun signUp(username: String, email: String, password: String, role: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // 1. Create account in Firebase Authentication
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                
                if (firebaseUser != null) {
                    // 2. Save additional details (username, role) to Firestore/Local Room
                    val user = UserEntity(username = username, email = email, password = password, role = role)
                    repository.insertUser(user)
                    _authState.value = AuthState.Success(user)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Signup failed")
            }
        }
    }

    fun login(email: String, password: String, role: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                // 1. Authenticate with Firebase
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                
                if (firebaseUser != null) {
                    // 2. Fetch user details from repository (Firestore/Room)
                    val user = repository.login(email, password, role)
                    if (user != null) {
                        _authState.value = AuthState.Success(user)
                    } else {
                        // Fallback: If not in DB but authenticated, create basic entity
                        val newUser = UserEntity(username = firebaseUser.displayName ?: "User", email = email, password = password, role = role)
                        _authState.value = AuthState.Success(newUser)
                    }
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Invalid email or password")
            }
        }
    }

    fun signInWithGoogle(idToken: String, role: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user
                
                if (firebaseUser != null) {
                    val email = firebaseUser.email ?: ""
                    val username = firebaseUser.displayName ?: "Google User"
                    
                    var user = repository.login(email, "", role)
                    if (user == null) {
                        user = UserEntity(username = username, email = email, password = "", role = role)
                        repository.insertUser(user)
                    }
                    _authState.value = AuthState.Success(user!!)
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Google Sign-In failed: ${e.message}")
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
