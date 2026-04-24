package com.example.smartcompanionapp.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.smartcompanionapp.data.database.announcement.AppDatabase
import com.example.smartcompanionapp.data.repository.AnnouncementRepository
import com.example.smartcompanionapp.data.session.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MarkAsReadReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val announcementId    = intent.getIntExtra("announcement_id", -1)
        val announcementTitle = intent.getStringExtra("announcement_title")
        val userId            = SessionManager(context).getUsername() ?: ""

        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager
                val db   = AppDatabase.getDatabase(context)
                val repo = AnnouncementRepository(
                    db.announcementDao(),
                    FirebaseFirestore.getInstance(),
                    userId
                )

                if (announcementId != -1) {
                    manager.cancel(announcementId)
                    repo.markAsRead(announcementId)
                } else if (announcementTitle != null) {
                    val stored = repo.getByTitle(announcementTitle)
                    if (stored != null) {
                        manager.cancel(stored.id)
                        repo.markAsRead(stored.id)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}