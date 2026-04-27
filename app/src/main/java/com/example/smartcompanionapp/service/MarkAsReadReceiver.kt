package com.example.smartcompanionapp.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.smartcompanionapp.data.database.announcement.AppDatabase
import com.example.smartcompanionapp.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MarkAsReadReceiver : BroadcastReceiver() {

    private val TAG = "MarkAsReadReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val announcementId    = intent.getIntExtra("announcement_id", -1)
        val announcementTitle = intent.getStringExtra("announcement_title")

        Log.d(TAG, "Mark as read: id=$announcementId title=$announcementTitle")

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // FALLBACK CHANGE: use "" consistently as the empty userId
                val userId = SessionManager(context).getUsername() ?: ""
                val dao    = AppDatabase.getDatabase(context).announcementDao()
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager

                when {
                    // Path 1: we have a valid Room ID
                    announcementId != -1 -> {
                        dao.markAsRead(announcementId, userId)
                        Log.d(TAG, "Marked id=$announcementId as read")

                        // Cancel using title hash (standard ID used by all services)
                        val stored = dao.getById(announcementId, userId)
                        if (stored != null) {
                            val notifId = AnnouncementNotificationService.stableId(stored.title)
                            manager.cancel(notifId)
                            Log.d(TAG, "Cancelled notification id=$notifId")
                        }
                    }

                    // Path 2: we only have a title (e.g. FCM path)
                    announcementTitle != null -> {
                        val stored = dao.getByTitle(announcementTitle, userId)
                        if (stored != null) {
                            dao.markAsRead(stored.id, userId)
                        }
                        val notifId = AnnouncementNotificationService.stableId(announcementTitle)
                        manager.cancel(notifId)
                        Log.d(TAG, "Cancelled notification id=$notifId (title path)")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in MarkAsReadReceiver", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * Replaced by AnnouncementNotificationService.stableId() to ensure consistency.
     */
}