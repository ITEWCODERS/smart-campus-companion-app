package com.example.smartcompanionapp.data

enum class UserRole {
    ADMIN, USER
}

data class User(
    val username: String,
    val password: String,
    val role: UserRole
)
