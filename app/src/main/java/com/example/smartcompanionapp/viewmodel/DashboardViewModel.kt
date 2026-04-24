package com.example.smartcompanionapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcompanionapp.data.model.Announcement
import com.example.smartcompanionapp.data.repository.AnnouncementRepository
import com.example.smartcompanionapp.intent.DashboardIntent
import com.example.smartcompanionapp.intent.DashboardState
import com.example.smartcompanionapp.service.AnnouncementNotificationService
import com.example.smartcompanionapp.service.FcmApiService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(
    application: Application,
    private val repository: AnnouncementRepository
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    val allAnnouncements: StateFlow<List<Announcement>> =
        repository.allAnnouncements
            .stateIn(
                scope        = viewModelScope,
                started      = SharingStarted.Eagerly,
                initialValue = emptyList()
            )

    init {
        // ── SINGLE NOTIFICATION SOURCE ────────────────────────────────────────
        // All notifications are fired from ONE place: repository.newAnnouncementsFlow.
        // This eliminates every duplicate scenario:
        //
        //   • Poster's device: insertSingle() pre-marks the title in notifiedTitles,
        //     so when the real-time listener fires back it is skipped. The ViewModel
        //     fires exactly ONE local notification here from the flow.
        //
        //   • Other devices: real-time listener sees the new title (not in notifiedTitles),
        //     emits via the flow, ViewModel fires ONE notification here.
        //
        //   • FCM arrives on other devices: MyFirebaseMessagingService shows the OS tray
        //     notification. It does NOT call syncFromFirestore() anymore — the real-time
        //     listener already updated Room. This eliminates the duplicate from FCM + listener.
        //
        //   • App open with existing announcements: LoadData calls syncFromFirestore()
        //     which diffs against Room and emits truly-new items through the flow,
        //     so the user gets notified for announcements they've never seen.
        viewModelScope.launch {
            repository.newAnnouncementsFlow.collect { newItems ->
                AnnouncementNotificationService.createNotificationChannel(getApplication())
                newItems.forEach { announcement ->
                    AnnouncementNotificationService.showAnnouncementNotification(
                        getApplication(), announcement
                    )
                }
            }
        }

        repository.startRealtimeListener(viewModelScope)
        processIntent(DashboardIntent.LoadData)
    }

    override fun onCleared() {
        super.onCleared()
        repository.stopRealtimeListener()
    }

    fun processIntent(intent: DashboardIntent) {
        when (intent) {

            is DashboardIntent.LoadData -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }

                    // Initial sync — handles "existing announcements on login":
                    // syncFromFirestore() diffs remote vs Room, emits truly new items
                    // through newAnnouncementsFlow → notifications fire above.
                    launch {
                        try { repository.syncFromFirestore() }
                        catch (e: Exception) { e.printStackTrace() }
                    }

                    // Sticky dashboard banner
                    launch {
                        repository.topUnread.collect { topItem ->
                            _state.update { current ->
                                val shouldSetBanner =
                                    !current.bannerDismissed &&
                                            current.topAnnouncement == null &&
                                            topItem != null
                                current.copy(
                                    topAnnouncement = if (shouldSetBanner) topItem
                                    else current.topAnnouncement,
                                    isLoading       = false
                                )
                            }
                        }
                    }

                    // Campus news horizontal scroll
                    launch {
                        repository.allAnnouncements.collect { list ->
                            _state.update { it.copy(campusNews = list) }
                        }
                    }
                }
            }

            is DashboardIntent.DismissAnnouncement -> {
                _state.update { current ->
                    if (current.topAnnouncement?.id == intent.announcementId)
                        current.copy(topAnnouncement = null, bannerDismissed = true)
                    else current
                }
            }

            is DashboardIntent.MarkAsRead -> {
                viewModelScope.launch {
                    repository.markAsRead(intent.announcementId)
                    _state.update { current ->
                        if (current.topAnnouncement?.id == intent.announcementId)
                            current.copy(topAnnouncement = null, bannerDismissed = true)
                        else current
                    }
                }
            }

            is DashboardIntent.TriggerSync -> {
                viewModelScope.launch {
                    try { repository.syncFromFirestore() }
                    catch (e: Exception) { e.printStackTrace() }
                }
            }

            is DashboardIntent.AddAnnouncement -> {
                viewModelScope.launch {
                    // insertSingle() pre-marks title in notifiedTitles AND upserts to Room.
                    // The real-time listener will fire on THIS device but will skip the
                    // notification (title already in notifiedTitles).
                    // The newAnnouncementsFlow collector above fires the notification
                    // for the poster because insertSingle() does NOT emit to the flow —
                    // we fire it manually here so the poster gets exactly one notification.
                    repository.insertSingle(intent.announcement)

                    // Fire ONE local notification for the poster
                    AnnouncementNotificationService.createNotificationChannel(getApplication())
                    val stored = repository.getByTitle(intent.announcement.title)
                    if (stored != null) {
                        AnnouncementNotificationService.showAnnouncementNotification(
                            getApplication(), stored
                        )
                    }

                    // FCM push to all other devices — delivers OS tray notification
                    // even when their app is killed or in Doze mode.
                    // Their MyFirebaseMessagingService ONLY shows the tray notification
                    // now — it no longer calls syncFromFirestore() to avoid duplicates
                    // with the real-time listener.
                    FcmApiService.sendToAllUsers(
                        context = getApplication(),
                        title   = intent.announcement.title,
                        body    = intent.announcement.content
                    )

                    // Pin on dashboard banner
                    _state.update { current ->
                        current.copy(
                            topAnnouncement = intent.announcement,
                            bannerDismissed = false
                        )
                    }
                }
            }

            is DashboardIntent.DeleteAnnouncement -> {
                viewModelScope.launch {
                    repository.delete(intent.announcement)
                    _state.update { current ->
                        if (current.topAnnouncement?.id == intent.announcement.id)
                            current.copy(topAnnouncement = null)
                        else current
                    }
                }
            }
        }
    }
}