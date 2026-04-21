package com.example.smartcompanionapp.intent

import com.example.smartcompanionapp.data.model.Announcement

/**
 * MVI STATE — everything the Dashboard UI needs to render.
 *
 * CHANGES:
 * - No structural changes to DashboardState itself.
 * - topAnnouncement stays as the banner shown at the top of the Dashboard.
 * - campusNews is the horizontal scroll list of announcement cards.
 */
data class DashboardState(
    val topAnnouncement: Announcement? = null,
    val campusNews: List<Announcement> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * MVI INTENTS — user actions dispatched to the ViewModel.
 *
 * CHANGES from original:
 * + Added MarkAsRead(announcementId) — dispatched when the user taps
 *   "Mark as Read" on an announcement card in AllAnnouncementsScreen,
 *   or when MarkAsReadReceiver fires from the notification action button.
 * + Added TriggerSync — dispatched by the refresh button to enqueue
 *   a one-time WorkManager job without blocking the UI coroutine.
 */
sealed class DashboardIntent {
    object LoadData                                          : DashboardIntent()
    object TriggerSync                                       : DashboardIntent() // NEW: refresh button
    data class DismissAnnouncement(val announcementId: Int)  : DashboardIntent()
    data class MarkAsRead(val announcementId: Int)           : DashboardIntent() // NEW: mark read
    data class AddAnnouncement(val announcement: Announcement): DashboardIntent()
    data class DeleteAnnouncement(val announcement: Announcement): DashboardIntent()
}