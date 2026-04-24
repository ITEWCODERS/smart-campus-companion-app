package com.example.smartcompanionapp.service

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object FcmApiService {

    private const val TAG = "FcmApiService"
    // Legacy endpoint is more reliable for device-to-device when clocks are out of sync
    private const val FCM_URL = "https://fcm.googleapis.com/fcm/send"
    
    // Using the Project API Key
    private const val SERVER_KEY = "AIzaSyBzhsduan8paR28nAoOU0tpvys9Dy1MCUE"

    suspend fun sendToAllUsers(context: Context, title: String, body: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val url = URL(FCM_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "key=$SERVER_KEY")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val payload = JSONObject().apply {
                    put("to", "/topics/announcements")
                    put("priority", "high")
                    
                    // ── THE 'notification' BLOCK ─────────────────────────────
                    // This forces the ANDROID SYSTEM to show the tray 
                    // notification even if the app is killed/backgrounded.
                    put("notification", JSONObject().apply {
                        put("title", "📢 $title")
                        put("body", body)
                        put("sound", "default")
                        put("click_action", "OPEN_ALL_ANNOUNCEMENTS")
                        put("android_channel_id", "campus_announcements_v2")
                    })
                    
                    // ── THE 'data' BLOCK ─────────────────────────────────────
                    // This tells our code to sync the database
                    put("data", JSONObject().apply {
                        put("title", title)
                        put("body", body)
                        put("type", "announcement")
                    })
                }

                OutputStreamWriter(connection.outputStream).use { it.write(payload.toString()) }

                val code = connection.responseCode
                Log.d(TAG, "FCM Send Result: $code")
                
                connection.disconnect()
                code == 200

            } catch (e: Exception) {
                Log.e(TAG, "FCM Connection Failed", e)
                false
            }
        }
}
