package com.example.smartcompanionapp.data.database.announcement.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.smartcompanionapp.data.model.Announcement
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnouncementDao {

    // Returns announcements for a specific user, newest first
    @Query("SELECT * FROM announcements WHERE userId = :userId ORDER BY datePosted DESC")
    fun getAllNews(userId: String): Flow<List<Announcement>>

    // Top unread for a specific user
    @Query("SELECT * FROM announcements WHERE isRead = 0 AND userId = :userId ORDER BY datePosted DESC LIMIT 1")
    fun getTopUnreadAnnouncement(userId: String): Flow<Announcement?>

    // Mark as read for a specific user
    @Query("UPDATE announcements SET isRead = 1 WHERE id = :id AND userId = :userId")
    suspend fun markAsRead(id: Int, userId: String)

    // ROOT CAUSE FIX 1:
    // The old @Insert(OnConflictStrategy.IGNORE) silently skipped rows that already
    // existed (same title+userId index), so Room never updated and the Flow never
    // emitted — meaning Device 2's UI never refreshed after FCM sync.
    // @Upsert = INSERT OR REPLACE: always writes the row, always triggers the Flow.
    @Upsert
    suspend fun upsertAnnouncements(announcements: List<Announcement>)

    // Keep the plain insert for any call sites that truly want IGNORE semantics
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAnnouncements(announcements: List<Announcement>)

    @Delete
    suspend fun deleteAnnouncement(announcement: Announcement)

    @Query("SELECT title FROM announcements WHERE userId = :userId")
    suspend fun getAllTitles(userId: String): List<String>

    @Query("SELECT * FROM announcements WHERE title = :title AND userId = :userId LIMIT 1")
    suspend fun getByTitle(title: String, userId: String): Announcement?

    @Query("DELETE FROM announcements WHERE title NOT IN (:titles) AND userId = :userId")
    suspend fun deleteOldAnnouncements(titles: List<String>, userId: String)

    @Query("SELECT COUNT(*) FROM announcements WHERE userId = :userId")
    suspend fun getCount(userId: String): Int

    // ROOT CAUSE FIX 2:
    // Old syncAnnouncements used insertAnnouncements (IGNORE) after deleting stale
    // rows. If a row was deleted then re-inserted with the same title+userId it would
    // work, but if the delete didn't fire (titles list unchanged) the IGNORE skipped
    // the upsert entirely and the Flow got no emission.
    // Now we always upsert — this guarantees Room writes the row and the Flow emits.
    @Transaction
    suspend fun syncAnnouncements(announcements: List<Announcement>, userId: String) {
        val currentTitles = announcements.map { it.title }
        // Remove announcements from Room that no longer exist in Firestore
        deleteOldAnnouncements(currentTitles, userId)
        // Upsert all remote announcements — always triggers Flow emission
        upsertAnnouncements(announcements)
    }
}