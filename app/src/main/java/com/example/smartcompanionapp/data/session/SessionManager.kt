package com.example.smartcompanionapp.data.session

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USERNAME = "username"
        private const val KEY_ROLE = "role"
        private const val KEY_DARK_MODE = "dark_mode"
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

    fun saveSession(username: String, role: String) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USERNAME, username)
            putString(KEY_ROLE, role)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getUsername(): String? {
        return prefs.getString(KEY_USERNAME, null)
    }

    fun getRole(): String? {
        return prefs.getString(KEY_ROLE, null)
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
        // No need to manually update _isDarkMode.value here as the listener will handle it.
    }

    fun isDarkMode(): Boolean? {
        return if (prefs.contains(KEY_DARK_MODE)) {
            prefs.getBoolean(KEY_DARK_MODE, false)
        } else {
            null
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
        // Listener will trigger and set _isDarkMode to null since KEY_DARK_MODE is removed
    }
}