package com.example.smartcompanionapp.service

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


object FcmApiService {

    private const val TAG = "FcmApiService"
    
    // Project ID from google-services.json
    private const val PROJECT_ID = "fir-projects-293af"
    
    private const val FCM_V1_URL = 
        "https://fcm.googleapis.com/v1/projects/$PROJECT_ID/messages:send"
    
    private const val SCOPE = "https://www.googleapis.com/auth/firebase.messaging"

    private fun getAccessToken(context: Context): String? {
        return try {
            val stream = context.assets.open("service_account.json")
            val credentials = GoogleCredentials
                .fromStream(stream)
                .createScoped(listOf(SCOPE))
            
            credentials.refreshIfExpired()
            credentials.accessToken.tokenValue
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get token. Ensure 'service_account.json' is in assets.", e)
            null
        }
    }

    suspend fun sendToAllUsers(context: Context, title: String, body: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val accessToken = getAccessToken(context) ?: return@withContext false

                val url = URL(FCM_V1_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $accessToken")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000

                val payload = JSONObject().apply {
                    put("message", JSONObject().apply {
                        put("topic", "announcements")
                        
                        // Using 'data' ensures onMessageReceived() is called in all app states.
                        put("data", JSONObject().apply {
                            put("title", title)
                            put("body", body)
                            put("type", "announcement")
                        })
                        
                        put("android", JSONObject().apply {
                            put("priority", "HIGH")
                        })
                    })
                }

                OutputStreamWriter(connection.outputStream).use { it.write(payload.toString()) }

                val responseCode = connection.responseCode
                if (responseCode != 200) {
                    val error = connection.errorStream?.bufferedReader()?.readText()
                    Log.e(TAG, "FCM v1 Error $responseCode: $error")
                }
                
                Log.d(TAG, "FCM v1 Send Result: $responseCode")
                connection.disconnect()
                responseCode == 200

            } catch (e: Exception) {
                Log.e(TAG, "FCM v1 Connection Failed", e)
                false
            }
        }
}
