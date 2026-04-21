package com.example.smartcompanionapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcompanionapp.data.model.Announcement
import com.example.smartcompanionapp.data.repository.AnnouncementRepository
import com.example.smartcompanionapp.intent.DashboardIntent
import com.example.smartcompanionapp.intent.DashboardState
import com.example.smartcompanionapp.service.AnnouncementNotificationService
import com.example.smartcompanionapp.worker.AnnouncementWorkScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * DASHBOARD VIEW MODEL — Room only, MVI pattern.
 *
 * Key behaviour for notifications:
 * - AddAnnouncement → insertSingle() writes to Room → Flow emits → UI updates
 *   → immediately fires a notification via AnnouncementNotificationService.
 *   No WorkManager or Firestore needed for the notification to appear.
 *
 * - MarkAsRead → dao.markAsRead() → Flow emits → banner hides, card turns grey.
 *   Read state is permanent in Room and never overwritten.
 */
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
                started      = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    init {
        processIntent(DashboardIntent.LoadData)
    }

    fun processIntent(intent: DashboardIntent) {
        when (intent) {

            is DashboardIntent.LoadData -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    launch {
                        repository.topUnread.collect { topItem ->
                            _state.update { it.copy(topAnnouncement = topItem, isLoading = false) }
                        }
                    }
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
                        current.copy(topAnnouncement = null)
                    else current
                }
            }

            is DashboardIntent.MarkAsRead -> {
                viewModelScope.launch {
                    repository.markAsRead(intent.announcementId)
                    // Room Flow emits → UI updates automatically (no manual state change needed)
                }
            }

            is DashboardIntent.TriggerSync -> {
                // One-time WorkManager job (refresh button)
                // With no Firestore, this just re-ensures the channel exists
                AnnouncementWorkScheduler.syncNow(getApplication())
            }

            is DashboardIntent.AddAnnouncement -> {
                viewModelScope.launch {
                    // 1. Write to Room — Flow emits → UI updates instantly
                    repository.insertSingle(intent.announcement)

                    // 2. Fire notification immediately — no WorkManager delay needed
                    AnnouncementNotificationService.createNotificationChannel(getApplication())
                    val stored = repository.getByTitle(intent.announcement.title)
                    if (stored != null) {
                        AnnouncementNotificationService.showAnnouncementNotification(
                            getApplication(), stored
                        )
                    }
                }
            }

            is DashboardIntent.DeleteAnnouncement -> {
                viewModelScope.launch {
                    repository.delete(intent.announcement)
                }
            }
        }
    }
}