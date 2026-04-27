package com.example.smartcompanionapp.intent

import com.example.smartcompanionapp.data.model.Announcement

// ── STATE ──────────────────────────────────────────────────────────────────────
data class DashboardState(
    val isLoading       : Boolean       = false,
    val topAnnouncement : Announcement? = null,   // banner shown on dashboard
    val campusNews      : List<Announcement> = emptyList(),
    val bannerDismissed : Boolean       = false    // user explicitly closed the banner
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