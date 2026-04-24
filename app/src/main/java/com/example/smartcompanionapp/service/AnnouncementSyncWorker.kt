package com.example.smartcompanionapp.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smartcompanionapp.data.database.announcement.AppDatabase
import com.example.smartcompanionapp.data.model.Announcement
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
        return try {
            val sessionManager = SessionManager(context)
            val userId = sessionManager.getUsername() ?: return Result.success()

            val db        = AppDatabase.getDatabase(context)
            val dao       = db.announcementDao()
            val firestore = FirebaseFirestore.getInstance()

            // Ensure notification channel exists before any notify() call
            AnnouncementNotificationService.createNotificationChannel(context)

            // ── STEP 1: Fetch remote list from Firestore ──────────────────────
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

            // ── STEP 2: Diff BEFORE sync to find truly new items ──────────────
            val existingTitles = dao.getAllTitles(userId).toHashSet()
            val trulyNew = remoteAnnouncements.filter { it.title !in existingTitles }

            Log.d(TAG, "Syncing: Found ${trulyNew.size} new announcements.")

            // ── STEP 3: Atomic sync (delete stale + insert all remote) ─────────
            dao.syncAnnouncements(remoteAnnouncements, userId)

            // ── STEP 4: Notify gracefully ─────────────────────────────────────
            // We notify even if it's the first sync, but only if it's a few items
            // so we don't spam the user with history.
            if (trulyNew.isNotEmpty()) {
                if (trulyNew.size <= 3) {
                    // Small number? Show them individually.
                    trulyNew.forEach { newItem ->
                        val stored = dao.getByTitle(newItem.title, userId)
                        if (stored != null) {
                            AnnouncementNotificationService.showAnnouncementNotification(context, stored)
                        }
                    }
                } else {
                    // Multiple? Show a summary to avoid "Shedding" (system blocking noisy apps)
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
