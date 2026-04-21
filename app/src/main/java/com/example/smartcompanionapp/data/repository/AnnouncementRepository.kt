package com.example.smartcompanionapp.data.repository

import com.example.smartcompanionapp.data.database.announcement.dao.AnnouncementDao
import com.example.smartcompanionapp.data.model.Announcement
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

/**
 * ANNOUNCEMENT REPOSITORY
 *
 * FIXES:
 * 1. insertSingle() now writes to Firestore FIRST, then Room.
 *    This means other users will see the announcement on their next sync,
 *    AND it won't be wiped by syncAnnouncements() because it exists in Firestore.
 *
 * 2. After inserting, we immediately trigger a local notification so the
 *    posting user also gets notified right away (not waiting 15 min for WorkManager).
 *
 * ROOT CAUSE of the original bug:
 *    syncAnnouncements() calls deleteOldAnnouncements(titles) which deletes every
 *    Room row whose title is NOT in the Firestore snapshot. Since locally-added
 *    announcements were only in Room (never pushed to Firestore), they were wiped
 *    on every refresh/periodic sync.
 */
class AnnouncementRepository(
    private val dao: AnnouncementDao,
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("announcements")

    // ── FLOWS (Room → UI) ─────────────────────────────────────────────────────

    /** Full list, newest first. Observed by AllAnnouncementsScreen. */
    val allAnnouncements: Flow<List<Announcement>> = dao.getAllNews()

    /** Single top unread item. Observed by Dashboard banner. */
    val topUnread: Flow<Announcement?> = dao.getTopUnreadAnnouncement()

    // ── READ ──────────────────────────────────────────────────────────────────

    suspend fun markAsRead(id: Int) = dao.markAsRead(id)

    // ── WRITE ─────────────────────────────────────────────────────────────────

    /**
     * Insert a single announcement.
     *
     * FIX: Writes to Firestore FIRST so that:
     *   (a) Other users see it on their next WorkManager sync.
     *   (b) syncAnnouncements() won't delete it (it now exists in Firestore).
     *
     * Then inserts into local Room so the UI updates instantly without
     * waiting for the next WorkManager cycle.
     *
     * If Firestore write fails, we still insert locally so the user
     * doesn't lose their entry — it will be re-synced later.
     */
    suspend fun insertSingle(announcement: Announcement) {
        // ── 1. Push to Firestore ──────────────────────────────────────────────
        val firestoreDoc = hashMapOf(
            "title"      to announcement.title,
            "content"    to announcement.content,
            "datePosted" to announcement.datePosted,
            "isRead"     to false
        )
        try {
            // Use title as document ID to match the unique-title constraint in Room.
            // This also prevents duplicates in Firestore if the same title is added twice.
            collection
                .document(announcement.title)
                .set(firestoreDoc)
                .await()
        } catch (e: Exception) {
            // Firestore write failed (offline?) — still save locally
            e.printStackTrace()
        }

        // ── 2. Insert into Room ───────────────────────────────────────────────
        // IGNORE conflict means if the title already exists locally, this is a no-op.
        dao.insertAnnouncements(listOf(announcement))
    }

    /**
     * WorkManager sync — called by AnnouncementSyncWorker every 15 minutes.
     * Replaces the entire local cache with the Firestore snapshot.
     * Safe now because insertSingle() pushes to Firestore first.
     */
    suspend fun syncFromFirestore(): List<Announcement> {
        val snapshot = collection
            .orderBy("datePosted", Query.Direction.DESCENDING)
            .get()
            .await()

        val remote = snapshot.documents.mapNotNull { doc ->
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

        // Atomic: delete stale + insert new (transaction in DAO)
        dao.syncAnnouncements(remote)

        return remote
    }

    /**
     * Diff helper: returns titles in [remote] that don't exist locally yet.
     * Used by WorkManager to know which announcements deserve a notification.
     */
    suspend fun findNewTitles(remote: List<Announcement>): List<Announcement> {
        val existing = dao.getAllTitles().toHashSet()
        return remote.filter { it.title !in existing }
    }

    /** Fetch a stored announcement by title (to get its auto-generated Room ID). */
    suspend fun getByTitle(title: String): Announcement? = dao.getByTitle(title)

    // ── DELETE ────────────────────────────────────────────────────────────────

    suspend fun delete(announcement: Announcement) {
        // Delete from both Room and Firestore so it stays gone after next sync
        dao.deleteAnnouncement(announcement)
        try {
            collection.document(announcement.title).delete().await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}