package com.example.smartcompanionapp.data.model

enum class UserRole {
    ADMIN, USER
}

data class User(
    val username: String,
    val password: String,
    val role: UserRole,
    val email: String = "",
    val phoneNumber: String = ""
)
