package com.example.smartcompanionapp.data.database.announcement.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.smartcompanionapp.data.model.Announcement
import kotlinx.coroutines.flow.Flow

//@Dao
//interface AnnouncementDao {
//    @Query("SELECT * FROM announcements ORDER BY datePosted DESC")
//    fun getAllNews(): Flow<List<Announcement>>
//
//    @Query("SELECT * FROM announcements ORDER BY datePosted DESC LIMIT 1")
//    fun getTopUnreadAnnouncement(): Flow<Announcement?>
//
//    @Query("UPDATE announcements SET isRead = 1 WHERE id = :id")
//    suspend fun markAsRead(id: Int)
//
//    @Insert(onConflict = OnConflictStrategy.IGNORE)
//    suspend fun insertAnnouncements(announcements: List<Announcement>)
//
//    @Delete
//    suspend fun deleteAnnouncement(announcement: Announcement)
//
//    @Query("SELECT COUNT(*) FROM announcements")
//    suspend fun getCount(): Int
//
//    @Query("DELETE FROM announcements WHERE title NOT IN (:titles)")
//    suspend fun deleteOldAnnouncements(titles: List<String>)
//
//    @Transaction
//    suspend fun syncAnnouncements(announcements: List<Announcement>) {
//        val currentTitles = announcements.map { it.title }
//        deleteOldAnnouncements(currentTitles)
//        insertAnnouncements(announcements)
//    }
//}

/**
 * STEP 2 — ROOM DAO
 *
 * CHANGES from original:
 * + Added getAllTitles()  → WorkManager uses this to diff remote vs local,
 *                          so it only fires notifications for truly NEW announcements.
 * + Added getByTitle()   → After insert, Room assigns the auto-generated ID.
 *                          WorkManager needs the real ID to tag the notification correctly.
 *
 * Everything else is unchanged from your original DAO.
 */
@Dao
interface AnnouncementDao {

    // ── READ ──────────────────────────────────────────────────────────────────

    /** Live stream of ALL announcements, newest first. Collected by the UI. */
    @Query("SELECT * FROM announcements ORDER BY datePosted DESC")
    fun getAllNews(): Flow<List<Announcement>>

    /**
     * Live stream of the single latest UNREAD announcement.
     * Used by the Dashboard preview card to show the most urgent item.
     *
     * NOTE: The original query fetched the top row regardless of isRead;
     * this version filters for unread only so the card disappears once everything is read.
     */
    @Query("SELECT * FROM announcements WHERE isRead = 0 ORDER BY datePosted DESC LIMIT 1")
    fun getTopUnreadAnnouncement(): Flow<Announcement?>

    // ── WRITE ─────────────────────────────────────────────────────────────────

    /** Mark a single announcement as read. Called from UI and from the notification action. */
    @Query("UPDATE announcements SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    /**
     * Bulk insert. OnConflictStrategy.IGNORE means if a title already exists
     * (unique index), the row is silently skipped — no crash, no duplicate.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAnnouncements(announcements: List<Announcement>)

    @Delete
    suspend fun deleteAnnouncement(announcement: Announcement)

    // ── HELPERS for WorkManager diffing ──────────────────────────────────────

    /**
     * NEW — Returns every title currently stored in Room.
     * WorkManager compares this list against the Firestore response to find
     * announcements that don't exist locally yet → those get a notification.
     */
    @Query("SELECT title FROM announcements")
    suspend fun getAllTitles(): List<String>

    /**
     * NEW — Fetches a single stored announcement by its title.
     * Used right after insertAnnouncements() to retrieve the auto-generated Room ID,
     * which becomes the notification ID so we can cancel it on "Mark as Read".
     */
    @Query("SELECT * FROM announcements WHERE title = :title LIMIT 1")
    suspend fun getByTitle(title: String): Announcement?

    // ── SYNC TRANSACTION ─────────────────────────────────────────────────────

    /** Prune stale rows not present in the latest Firestore snapshot. */
    @Query("DELETE FROM announcements WHERE title NOT IN (:titles)")
    suspend fun deleteOldAnnouncements(titles: List<String>)

    @Query("SELECT COUNT(*) FROM announcements")
    suspend fun getCount(): Int

    /**
     * Atomic sync: delete stale rows THEN insert new ones in a single transaction.
     * If anything fails, both operations roll back together.
     */
    @Transaction
    suspend fun syncAnnouncements(announcements: List<Announcement>) {
        val currentTitles = announcements.map { it.title }
        deleteOldAnnouncements(currentTitles)
        insertAnnouncements(announcements)
    }
}