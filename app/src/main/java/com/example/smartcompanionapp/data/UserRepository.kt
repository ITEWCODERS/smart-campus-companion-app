package com.example.smartcompanionapp.data

object UserRepository {
    private val users = mutableListOf<User>(
        User("admin", "admin123", UserRole.ADMIN, "admin@campus.com", "1234567890"),
        User("user", "user123", UserRole.USER, "user@campus.com", "0987654321")
    )

    fun signUp(user: User): Boolean {
        if (users.any { it.username == user.username }) {
            return false // User already exists
        }
        users.add(user)
        return true
    }

    fun login(username: String, password: String): User? {
        return users.find { it.username == username && it.password == password }
    }
}
