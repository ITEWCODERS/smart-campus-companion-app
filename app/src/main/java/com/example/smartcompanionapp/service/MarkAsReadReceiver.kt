package com.example.smartcompanionapp.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.smartcompanionapp.data.database.announcement.AppDatabase
import com.example.smartcompanionapp.data.repository.AnnouncementRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * MARK AS READ RECEIVER — Room only.
 *
 * Tapping "Mark as Read" in the notification shade:
 *  1. Cancels the notification
 *  2. Updates isRead = 1 in Room
 *
 * Room Flow emits → UI updates automatically (banner hides, card turns grey).
 * No Firestore needed — Room is the single source of truth.
 */
class MarkAsReadReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val announcementId = intent.getIntExtra("announcement_id", -1)
        if (announcementId == -1) return

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Dismiss the notification
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager
                manager.cancel(announcementId)

                // 2. Mark as read in Room — Flow emits → UI updates instantly
                val db   = AppDatabase.getDatabase(context)
                val repo = AnnouncementRepository(
                    db.announcementDao(),
                    FirebaseFirestore.getInstance() // unused but required by constructor
                )
                repo.markAsRead(announcementId)

            } finally {
                pendingResult.finish()
            }
        }
    }
}