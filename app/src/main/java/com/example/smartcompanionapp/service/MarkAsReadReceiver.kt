package com.example.smartcompanionapp.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.smartcompanionapp.data.database.announcement.AppDatabase
import com.example.smartcompanionapp.data.session.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MarkAsReadReceiver : BroadcastReceiver() {

    private val TAG = "MarkAsReadReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val announcementId    = intent.getIntExtra("announcement_id", -1)
        val announcementTitle = intent.getStringExtra("announcement_title")

        Log.d(TAG, "Mark as read triggered: id=$announcementId title=$announcementTitle")

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sessionManager = SessionManager(context)
                val userId = sessionManager.getUserId() ?: ""
                val dao    = AppDatabase.getDatabase(context).announcementDao()
                val firestore = FirebaseFirestore.getInstance()
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                suspend fun updateCloudState(title: String) {
                    if (userId.isNotEmpty()) {
                        try {
                            firestore.collection("users").document(userId)
                                .collection("read_announcements")
                                .document(title)
                                .set(mapOf("read" to true, "timestamp" to System.currentTimeMillis()))
                            Log.d(TAG, "Successfully synced read state to Cloud for: $title")
                        } catch (e: Exception) {
                            Log.e(TAG, "Cloud sync failed", e)
                        }
                    }
                }

                when {
                    // Scenario 1: We have the internal Room ID
                    announcementId != -1 -> {
                        dao.markAsRead(announcementId, userId)
                        val stored = dao.getById(announcementId, userId)
                        if (stored != null) {
                            updateCloudState(stored.title)
                            manager.cancel(AnnouncementNotificationService.stableId(stored.title))
                        }
                    }

                    // Scenario 2: We only have the Title (e.g., FCM tray notification)
                    announcementTitle != null -> {
                        val stored = dao.getByTitle(announcementTitle, userId)
                        if (stored != null) {
                            dao.markAsRead(stored.id, userId)
                            updateCloudState(stored.title)
                        } else {
                            // Even if not in Room yet, mark as read in Cloud to prevent it showing as NEW later
                            updateCloudState(announcementTitle)
                        }
                        manager.cancel(AnnouncementNotificationService.stableId(announcementTitle))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in MarkAsReadReceiver", e)
            } finally {
                pendingResult.finish()
                Log.d(TAG, "MarkAsReadReceiver finished")
            }
        }
    }
}
