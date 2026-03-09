package com.example.smartcompanionapp.data.database.announcement.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.smartcompanionapp.model.Announcement
import kotlinx.coroutines.flow.Flow

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY datePosted DESC")
    fun getAllNews(): Flow<List<Announcement>>

    @Query("SELECT * FROM announcements ORDER BY datePosted DESC LIMIT 1")
    fun getTopUnreadAnnouncement(): Flow<Announcement?>

    @Query("UPDATE announcements SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncements(announcements: List<Announcement>)

    @Query("SELECT COUNT(*) FROM announcements")
    suspend fun getCount(): Int

    @Transaction
    suspend fun ensureDummyData(announcements: List<Announcement>) {
        if (getCount() == 0) {
            insertAnnouncements(announcements)
        }
    }
}
