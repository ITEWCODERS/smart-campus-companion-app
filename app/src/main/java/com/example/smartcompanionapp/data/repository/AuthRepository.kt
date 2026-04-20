package com.example.smartcompanionapp.data.repository

import com.example.smartcompanionapp.data.database.authentication.AuthDao
import com.example.smartcompanionapp.data.database.authentication.UserEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val authDao: AuthDao,
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")

    suspend fun insertUser(user: UserEntity): Long {
        val id = authDao.insertUser(user)
        // Sync to Firestore
        try {
            usersCollection.document(user.email).set(user).await()
        } catch (e: Exception) {
            // Handle error
        }
        return id
    }

    suspend fun login(email: String, password: String, role: String): UserEntity? {
        // Try local login first (as SSOT is local)
        var user = authDao.login(email, password, role)
        
        if (user == null) {
            // If not found locally, try to fetch from Firestore and cache locally
            try {
                val snapshot = usersCollection.document(email).get().await()
                user = snapshot.toObject(UserEntity::class.java)
                if (user != null && user.password == password && user.role == role) {
                    authDao.insertUser(user)
                } else {
                    user = null
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
        return user
    }

    suspend fun isEmailExists(email: String): Boolean {
        // Check local first
        if (authDao.isEmailExists(email)) return true
        
        // Check Firestore
        return try {
            val snapshot = usersCollection.document(email).get().await()
            snapshot.exists()
        } catch (e: Exception) {
            false
        }
    }
}
