package com.example.smartcompanionapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smartcompanionapp.data.database.announcement.AppDatabase
import com.example.smartcompanionapp.data.model.Announcement
import com.example.smartcompanionapp.data.repository.AnnouncementRepository
import com.example.smartcompanionapp.service.AnnouncementNotificationService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await


class AnnouncementSyncWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val db        = AppDatabase.getDatabase(context)
            val dao       = db.announcementDao()
            val firestore = FirebaseFirestore.getInstance()
            val repo      = AnnouncementRepository(dao, firestore)

            // Ensure notification channel exists before any notify() call
            AnnouncementNotificationService.createNotificationChannel(context)

            // ── STEP 1: Fetch remote list from Firestore ──────────────────────
            val snapshot = firestore
                .collection("announcements")
                .orderBy("datePosted", Query.Direction.DESCENDING)
                .get()
                .await() // top-level kotlinx.coroutines.tasks.await — no extension needed

            val remoteAnnouncements = snapshot.documents.mapNotNull { doc ->
                val title   = doc.getString("title")    ?: return@mapNotNull null
                val content = doc.getString("content")  ?: return@mapNotNull null
                val date    = doc.getLong("datePosted") ?: System.currentTimeMillis()
                Announcement(
                    title      = title,
                    content    = content,
                    datePosted = date,
                    isRead     = false
                )
            }

            // ── STEP 2: Diff BEFORE sync ──────────────────────────────────────
            // findNewTitles() compares remote list against current Room contents.
            // MUST happen before syncAnnouncements() wipes and rewrites Room.
            val newAnnouncements = repo.findNewTitles(remoteAnnouncements)

            // ── STEP 3: Atomic sync (delete stale + insert all remote) ─────────
            dao.syncAnnouncements(remoteAnnouncements)

            // ── STEP 4: Notify for each truly new announcement ────────────────
            newAnnouncements.forEach { newItem ->
                val stored = dao.getByTitle(newItem.title)
                if (stored != null) {
                    AnnouncementNotificationService.showAnnouncementNotification(
                        context,
                        stored
                    )
                }
            }

            Result.success()

        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}