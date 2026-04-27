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

    init {
        // Single notification source logic remains the same
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

                    launch {
                        try { repository.syncFromFirestore() }
                        catch (e: Exception) { e.printStackTrace() }
                    }

                    launch {
                        repository.topUnread.collect { topItem ->
                            _state.update { current ->
                                val shouldSetBanner = !current.bannerDismissed && current.topAnnouncement == null && topItem != null
                                current.copy(
                                    topAnnouncement = if (shouldSetBanner) topItem else current.topAnnouncement,
                                    isLoading = false
                                )
                            }
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
                        current.copy(topAnnouncement = null, bannerDismissed = true)
                    else current
                }
            }

            is DashboardIntent.MarkAsRead -> {
                viewModelScope.launch {
                    // 1. Sync to Cloud and DB
                    repository.markAsRead(intent.announcementId)
                    
                    // 2. INSTANT local state update for snappy UI
                    _state.update { current ->
                        val updatedNews = current.campusNews.map { 
                            if (it.id == intent.announcementId) it.copy(isRead = true) else it 
                        }
                        current.copy(
                            campusNews = updatedNews,
                            topAnnouncement = if (current.topAnnouncement?.id == intent.announcementId) null else current.topAnnouncement
                        )
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
                    repository.insertSingle(intent.announcement)
                    AnnouncementNotificationService.createNotificationChannel(getApplication())
                    val stored = repository.getByTitle(intent.announcement.title)
                    if (stored != null) {
                        AnnouncementNotificationService.showAnnouncementNotification(getApplication(), stored)
                    }
                    FcmApiService.sendToAllUsers(getApplication(), intent.announcement.title, intent.announcement.content)
                    _state.update { it.copy(topAnnouncement = intent.announcement, bannerDismissed = false) }
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
