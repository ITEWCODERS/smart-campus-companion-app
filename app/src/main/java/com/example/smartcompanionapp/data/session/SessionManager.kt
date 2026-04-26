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
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
    }

    private val _isDarkMode = MutableStateFlow(isDarkMode())
    val isDarkModeFlow: StateFlow<Boolean?> = _isDarkMode

    private val _isNotificationsEnabled = MutableStateFlow(isNotificationsEnabled())
    val isNotificationsEnabledFlow: StateFlow<Boolean> = _isNotificationsEnabled

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
            KEY_DARK_MODE -> _isDarkMode.value = isDarkMode()
            KEY_NOTIFICATIONS_ENABLED -> _isNotificationsEnabled.value = isNotificationsEnabled()
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

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
    fun getRole(): String? = prefs.getString(KEY_ROLE, null)

    fun setDarkMode(enabled: Boolean?) {
        prefs.edit().apply {
            if (enabled == null) remove(KEY_DARK_MODE)
            else putBoolean(KEY_DARK_MODE, enabled)
            apply()
        }
    }

    fun isDarkMode(): Boolean? {
        return if (prefs.contains(KEY_DARK_MODE)) prefs.getBoolean(KEY_DARK_MODE, false) else null
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun isNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true) // Default to true
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}