package com.example.smartcompanionapp.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "announcements",
    indices = [Index(value = ["title", "userId"], unique = true)] // unique per user
)
data class Announcement(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val datePosted: Long,
    val isRead: Boolean,
    val userId: String = "" // which user this read state belongs to
)