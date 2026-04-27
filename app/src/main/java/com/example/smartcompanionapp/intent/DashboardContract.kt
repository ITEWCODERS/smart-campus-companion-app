package com.example.smartcompanionapp.intent

import com.example.smartcompanionapp.data.model.Announcement

// ── STATE ──────────────────────────────────────────────────────────────────────
data class DashboardState(
    val topAnnouncement: Announcement? = null,
    val campusNews: List<Announcement> = emptyList(),
    val isLoading: Boolean = true,
    val isAdmin: Boolean = false

)

// ── INTENTS ───────────────────────────────────────────────────────────────────
sealed class DashboardIntent {
    object LoadData                                          : DashboardIntent()
    data class DismissAnnouncement(val announcementId: Int)  : DashboardIntent()
    data class MarkAsRead(val announcementId: Int)           : DashboardIntent()
    object TriggerSync                                       : DashboardIntent()
    data class AddAnnouncement(val announcement: Announcement): DashboardIntent()
    data class DeleteAnnouncement(val announcement: Announcement): DashboardIntent()
}