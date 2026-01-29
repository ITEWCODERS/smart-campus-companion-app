package com.example.smartcompanionapp.data

object UserRepository {
    private val users = mutableListOf<User>()

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
