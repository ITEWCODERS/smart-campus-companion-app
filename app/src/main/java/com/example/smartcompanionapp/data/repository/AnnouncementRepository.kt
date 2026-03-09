package com.example.smartcompanionapp.data.repository

import com.example.smartcompanionapp.data.database.announcement.dao.AnnouncementDao
import com.example.smartcompanionapp.model.Announcement
import kotlinx.coroutines.flow.Flow

class AnnouncementRepository(private val dao: AnnouncementDao) {

    val campusNews: Flow<List<Announcement>> = dao.getAllNews()
    val topUnreadAnnouncement: Flow<Announcement?> = dao.getTopUnreadAnnouncement()

    suspend fun markAsRead(id: Int) {
        dao.markAsRead(id)
    }

    suspend fun ensureDummyData() {
        val dummyData = listOf(
            Announcement(
                title = "Urgent: Classes Suspended",
                content = "Due to heavy rain, afternoon classes are suspended. Stay safe!",
                datePosted = System.currentTimeMillis(),
                isRead = false
            ),
            Announcement(
                title = "Library Renovation",
                content = "The 3rd floor is closed for renovation.",
                datePosted = System.currentTimeMillis() - 86400000,
                isRead = true
            ),
            Announcement(
                title = "Tech Fair Registration",
                content = "Sign up for the hackathon at the CS Dept.",
                datePosted = System.currentTimeMillis() - 172800000,
                isRead = true
            )
        )
        dao.ensureDummyData(dummyData)
    }
}
