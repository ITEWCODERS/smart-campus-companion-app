package com.example.smartcompanionapp.data.session

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_ROLE = "role"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_COURSE = "course"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_PROFILE_IMAGE = "profile_image_uri"
    }

    private val _isDarkMode = MutableStateFlow(isDarkMode())
    val isDarkModeFlow: StateFlow<Boolean?> = _isDarkMode

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == KEY_DARK_MODE) {
            _isDarkMode.value = isDarkMode()
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    fun saveSession(username: String, role: String, userId: String, email: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USERNAME, username)
            putString(KEY_ROLE, role)
            putString(KEY_USER_ID, userId)
            putString(KEY_EMAIL, email)
            apply()
        }
    }

    fun updateProfile(username: String, email: String, course: String) {
        prefs.edit().apply {
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
            putString(KEY_COURSE, course)
            apply()
        }
    }

    fun updateProfileImage(uri: String) {
        prefs.edit().putString(KEY_PROFILE_IMAGE, uri).apply()
    }

    fun getProfileImage(): String? {
        return prefs.getString(KEY_PROFILE_IMAGE, null)
    }

    fun isLoggedIn(): Boolean {
        return auth.currentUser != null || prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, auth.currentUser?.displayName)
    }

    fun getEmail(): String? {
        return prefs.getString(KEY_EMAIL, auth.currentUser?.email)
    }

    fun getRole(): String? {
        return prefs.getString(KEY_ROLE, "user")
    }

    fun getCourse(): String {
        return prefs.getString(KEY_COURSE, "Computer Science") ?: "Computer Science"
    }
    
    fun getUserId(): String? {
        return auth.currentUser?.uid ?: prefs.getString(KEY_USER_ID, null)
    }

    fun setDarkMode(enabled: Boolean?) {
        prefs.edit().apply {
            if (enabled == null) {
                remove(KEY_DARK_MODE)
            } else {
                putBoolean(KEY_DARK_MODE, enabled)
            }
            apply()
        }
    }

    fun isDarkMode(): Boolean? {
        return if (prefs.contains(KEY_DARK_MODE)) {
            prefs.getBoolean(KEY_DARK_MODE, false)
        } else {
            null
        }
    }

    fun logout() {
        auth.signOut()
        clearSession()
    }

    fun clearSession() {
        val darkMode = isDarkMode()
        prefs.edit().clear().apply()
        setDarkMode(darkMode)
    }
}