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

    @Query("SELECT * FROM announcements WHERE userId = :userId ORDER BY datePosted DESC")
    fun getAllNews(userId: String): Flow<List<Announcement>>

    @Query("SELECT * FROM announcements WHERE isRead = 0 AND userId = :userId ORDER BY datePosted DESC LIMIT 1")
    fun getTopUnreadAnnouncement(userId: String): Flow<Announcement?>

    @Query("UPDATE announcements SET isRead = 1 WHERE id = :id AND userId = :userId")
    suspend fun markAsRead(id: Int, userId: String)

    @Upsert
    suspend fun upsertAnnouncements(announcements: List<Announcement>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAnnouncements(announcements: List<Announcement>)

    @Delete
    suspend fun deleteAnnouncement(announcement: Announcement)

    @Query("SELECT title FROM announcements WHERE userId = :userId")
    suspend fun getAllTitles(userId: String): List<String>

    @Query("SELECT * FROM announcements WHERE title = :title AND userId = :userId LIMIT 1")
    suspend fun getByTitle(title: String, userId: String): Announcement?

    @Query("SELECT isRead FROM announcements WHERE title = :title AND userId = :userId LIMIT 1")
    suspend fun isRead(title: String, userId: String): Boolean?

    // NEW: look up by Room primary key — used by MarkAsReadReceiver to resolve

    @Query("SELECT * FROM announcements WHERE id = :id AND userId = :userId LIMIT 1")
    suspend fun getById(id: Int, userId: String): Announcement?

    @Query("DELETE FROM announcements WHERE title NOT IN (:titles) AND userId = :userId")
    suspend fun deleteOldAnnouncements(titles: List<String>, userId: String)

    @Query("SELECT COUNT(*) FROM announcements WHERE userId = :userId")
    suspend fun getCount(userId: String): Int

    @Transaction
    suspend fun syncAnnouncements(announcements: List<Announcement>, userId: String) {
        val currentTitles = announcements.map { it.title }
        deleteOldAnnouncements(currentTitles, userId)
        upsertAnnouncements(announcements)
    }
}