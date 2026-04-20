package com.example.smartcompanionapp.data.repository

import com.example.smartcompanionapp.data.database.announcement.dao.AnnouncementDao
import com.example.smartcompanionapp.data.model.Announcement
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class AnnouncementRepository(
    private val dao: AnnouncementDao,
    private val firestore: FirebaseFirestore
) {
    private val announcementsCollection = firestore.collection("announcements")

    val campusNews: Flow<List<Announcement>> = dao.getAllNews()
    val topUnreadAnnouncement: Flow<Announcement?> = dao.getTopUnreadAnnouncement()

    suspend fun markAsRead(id: Int) {
        dao.markAsRead(id)
        // Optionally sync read state to firestore if needed
    }

    suspend fun postAnnouncement(announcement: Announcement) {
        dao.insertAnnouncements(listOf(announcement))
        // Sync to Firestore
        try {
            announcementsCollection.document(announcement.id.toString()).set(announcement).await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun deleteAnnouncement(announcement: Announcement) {
        dao.deleteAnnouncement(announcement)
        // Sync delete to Firestore
        try {
            announcementsCollection.document(announcement.id.toString()).delete().await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    suspend fun refreshAnnouncementsFromFirestore() {
        try {
            val snapshot = announcementsCollection.get().await()
            val remoteAnnouncements = snapshot.toObjects(Announcement::class.java)
            dao.insertAnnouncements(remoteAnnouncements)
        } catch (e: Exception) {
            // Handle error
        }
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
        
        // Optionally sync dummy data to firestore if it's empty there too
    }
}
