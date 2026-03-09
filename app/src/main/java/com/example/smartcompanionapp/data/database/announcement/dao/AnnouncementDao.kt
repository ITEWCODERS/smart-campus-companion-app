package com.example.smartcompanionapp.data.database.announcement.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.smartcompanionapp.data.model.Announcement
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY datePosted DESC")
    fun getAllNews(): Flow<List<Announcement>>

    @Query("SELECT * FROM announcements ORDER BY datePosted DESC LIMIT 1")
    fun getTopUnreadAnnouncement(): Flow<Announcement?>

    @Query("UPDATE announcements SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAnnouncements(announcements: List<Announcement>)

    @Delete
    suspend fun deleteAnnouncement(announcement: Announcement)

    @Query("SELECT COUNT(*) FROM announcements")
    suspend fun getCount(): Int

    @Query("DELETE FROM announcements WHERE title NOT IN (:titles)")
    suspend fun deleteOldAnnouncements(titles: List<String>)

    @Transaction
    suspend fun syncAnnouncements(announcements: List<Announcement>) {
        val currentTitles = announcements.map { it.title }
        deleteOldAnnouncements(currentTitles)
        insertAnnouncements(announcements)
    }
}
