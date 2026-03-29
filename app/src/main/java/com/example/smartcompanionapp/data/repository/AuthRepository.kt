package com.example.smartcompanionapp.data.repository

import com.example.smartcompanionapp.data.database.authentication.AuthDao
import com.example.smartcompanionapp.data.database.authentication.UserEntity

class AuthRepository(private val authDao: AuthDao) {

    suspend fun insertUser(user: UserEntity): Long {
        return authDao.insertUser(user)
    }

    suspend fun login(email: String, password: String, role: String): UserEntity? {
        return authDao.login(email, password, role)
    }

    suspend fun isEmailExists(email: String): Boolean {
        return authDao.isEmailExists(email)
    }
}