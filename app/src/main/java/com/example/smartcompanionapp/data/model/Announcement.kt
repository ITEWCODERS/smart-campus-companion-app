package com.example.smartcompanionapp.data.model



import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

//@Entity(
//    tableName = "announcements",
//    indices = [Index(value = ["title"], unique = true)] // Ensure titles are unique
//)
//data class Announcement(
//    @PrimaryKey(autoGenerate = true)
//    val id: Int = 0,
//    val title: String,
//    val content: String,
//    val datePosted: Long,
//    val isRead: Boolean
//)
@Entity(
    tableName = "announcements",
    indices = [Index(value = ["title"], unique = true)] // prevents duplicate inserts
)
data class Announcement(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,       // notification title + card header
    val content: String,     // notification body + card body
    val datePosted: Long,    // epoch millis — used for ordering and display
    val isRead: Boolean      // tracks read/unread state for UI badge + filtering
)
