package com.example.smartcompanionapp.intent

import com.example.smartcompanionapp.data.model.Announcement

// 1. STATE: Represents everything visible on the screen at any given time
data class DashboardState(
    val topAnnouncement: Announcement? = null,
    val campusNews: List<Announcement> = emptyList(),
    val isLoading: Boolean = true,
    val isAdmin: Boolean = false

)

// 2. INTENT: Represents actions the user can take
sealed class DashboardIntent {
    object LoadData : DashboardIntent()
    data class DismissAnnouncement(val announcementId: Int) : DashboardIntent()
    data class AddAnnouncement(val announcement: Announcement) : DashboardIntent()
    data class DeleteAnnouncement(val announcement: Announcement) : DashboardIntent()
}
