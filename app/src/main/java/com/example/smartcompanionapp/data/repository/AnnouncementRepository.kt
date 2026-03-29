package com.example.smartcompanionapp.data.repository

import com.example.smartcompanionapp.data.database.announcement.dao.AnnouncementDao
import com.example.smartcompanionapp.data.model.Announcement
import kotlinx.coroutines.flow.Flow

class AnnouncementRepository(private val dao: AnnouncementDao) {

    val campusNews: Flow<List<Announcement>> = dao.getAllNews()
    val topUnreadAnnouncement: Flow<Announcement?> = dao.getTopUnreadAnnouncement()

    suspend fun markAsRead(id: Int) {
        dao.markAsRead(id)
    }

    suspend fun postAnnouncement(announcement: Announcement) {
        dao.insertAnnouncements(listOf(announcement))
    }

    suspend fun deleteAnnouncement(announcement: Announcement) {
        dao.deleteAnnouncement(announcement)
    }

    suspend fun ensureDummyData() {
        // Only insert if the database is currently empty
        if (dao.getCount() > 0) return

        val dummyData = listOf(
            Announcement(
                title = "Urgent: Holiday Alert",
                content = "Due to Holy week, all classes are cancelled.",
                datePosted = System.currentTimeMillis(),
                isRead = false 
            ),
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
        dao.insertAnnouncements(dummyData)
    }
}
