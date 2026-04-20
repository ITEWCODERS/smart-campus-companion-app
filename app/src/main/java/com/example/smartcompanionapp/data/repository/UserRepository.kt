package com.example.smartcompanionapp.data.repository

import android.content.Context
import com.example.smartcompanionapp.data.model.User
import com.example.smartcompanionapp.data.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

object UserRepository {
    private var users = mutableListOf<User>()
    private const val PREFS_NAME = "user_prefs"
    private const val KEY_USERS = "users_list"
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users_legacy") // Legacy user storage in Firestore

    // Default hardcoded users
    private val defaultUsers = listOf(
        User("admin", "admin123", UserRole.ADMIN, "admin@campus.com", "1234567890"),
        User("user", "user123", UserRole.USER, "user@campus.com", "0987654321")
    )

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val usersJson = prefs.getString(KEY_USERS, null)

        users.clear()
        if (usersJson == null) {
            users.addAll(defaultUsers)
            saveToPrefs(context)
        } else {
            val jsonArray = JSONArray(usersJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                users.add(
                    User(
                        obj.getString("username"),
                        obj.getString("password"),
                        UserRole.valueOf(obj.getString("role")),
                        obj.optString("email", ""),
                        obj.optString("phoneNumber", "")
                    )
                )
            }
        }
    }

    private fun saveToPrefs(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray()
        users.forEach { user ->
            val obj = JSONObject()
            obj.put("username", user.username)
            obj.put("password", user.password)
            obj.put("role", user.role.name)
            obj.put("email", user.email)
            obj.put("phoneNumber", user.phoneNumber)
            jsonArray.put(obj)
            
            // Sync to Firestore
            usersCollection.document(user.username).set(user)
        }
        prefs.edit().putString(KEY_USERS, jsonArray.toString()).apply()
    }

    fun signUp(context: Context, user: User): Boolean {
        if (users.isEmpty()) init(context)
        if (users.any { it.username == user.username }) {
            return false
        }
        users.add(user)
        saveToPrefs(context)
        return true
    }

    fun login(context: Context, username: String, password: String): User? {
        if (users.isEmpty()) init(context)
        return users.find { it.username == username && it.password == password }
    }
    
    suspend fun syncFromFirestore(context: Context) {
        try {
            val snapshot = usersCollection.get().await()
            val remoteUsers = snapshot.toObjects(User::class.java)
            if (remoteUsers.isNotEmpty()) {
                users.clear()
                users.addAll(remoteUsers)
                saveToPrefs(context)
            }
        } catch (e: Exception) {
            // Handle error
        }
    }
}
