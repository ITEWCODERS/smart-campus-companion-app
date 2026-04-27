package com.example.smartcompanionapp.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smartcompanionapp.data.database.announcement.AppDatabase
import com.example.smartcompanionapp.data.model.Announcement
import com.example.smartcompanionapp.data.repository.AnnouncementRepository
import com.example.smartcompanionapp.data.session.SessionManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class AnnouncementSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val TAG = "AnnouncementSyncWorker"

    override suspend fun doWork(): Result {
        val sessionManager = SessionManager(context)
        
        // ── PART 3 ADDITION: Master Switch Check ──────────────────────────
        if (!sessionManager.isLoggedIn() || !sessionManager.isNotificationsEnabled()) {
            Log.d(TAG, "Sync skipped: User logged out or notifications disabled.")
            return Result.success() 
        }

        val userId = sessionManager.getUserId() ?: ""

        return try {
            val db        = AppDatabase.getDatabase(context)
            val dao       = db.announcementDao()
            val firestore = FirebaseFirestore.getInstance()

            val repo = AnnouncementRepository(
                dao       = dao,
                firestore = firestore,
                userId    = userId,
                context   = context.applicationContext
            )

            AnnouncementNotificationService.createNotificationChannel(context)

            // Get existing titles BEFORE sync for diffing
            val existingTitles = dao.getAllTitles(userId).toHashSet()

            // Perform sync
            repo.syncFromFirestore()

            // ── PART 3 ADDITION: Channel Specific Check ─────────────────────
            if (!sessionManager.isAnnouncementsEnabled()) {
                Log.d(TAG, "Announcement channel disabled. Skipping notification.")
                return Result.success()
            }

            // Fetch remote announcements again for notification diff
            val snapshot = firestore
                .collection("announcements")
                .orderBy("datePosted", Query.Direction.DESCENDING)
                .get()
                .await()

            val remoteAnnouncements = snapshot.documents.mapNotNull { doc ->
                val title   = doc.getString("title")    ?: return@mapNotNull null
                val content = doc.getString("content")  ?: return@mapNotNull null
                val date    = doc.getLong("datePosted") ?: System.currentTimeMillis()
                Announcement(
                    title      = title,
                    content    = content,
                    datePosted = date,
                    isRead     = false,
                    userId     = userId
                )
            }

            val trulyNew = remoteAnnouncements.filter {
                it.title !in existingTitles && !repo.isAlreadyNotified(it.title)
            }

            if (trulyNew.isNotEmpty()) {
                if (trulyNew.size <= 3) {
                    trulyNew.forEach { newItem ->
                        val stored = dao.getByTitle(newItem.title, userId)
                        if (stored != null) {
                            AnnouncementNotificationService.showAnnouncementNotification(context, stored)
                        }
                    }
                } else {
                    AnnouncementNotificationService.showSummaryNotification(context, trulyNew.size)
                }
            }

            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            Result.retry()
        }
    }
}
