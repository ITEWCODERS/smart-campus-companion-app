package com.example.smartcompanionapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcompanionapp.data.repository.AnnouncementRepository
import com.example.smartcompanionapp.intent.DashboardIntent
import com.example.smartcompanionapp.intent.DashboardState
import com.example.smartcompanionapp.data.model.Announcement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: AnnouncementRepository) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    // Add this function
    fun setAdminPrivileges(isAdmin: Boolean) {
        _state.update { it.copy(isAdmin = isAdmin) }
    }

    // Private state to hold IDs of announcements dismissed in this session
    private val dismissedInSession = MutableStateFlow<Set<Int>>(emptySet())

    init {
        viewModelScope.launch {
            // 1. Ensure data exists first
            repository.ensureDummyData()

            // 2. Observe campus news as before
            repository.campusNews.collect { news ->
                _state.update { it.copy(campusNews = news, isLoading = false) }
            }
        }

        viewModelScope.launch {
            // 3. Combine the raw top announcement with our session-dismissed set
            repository.topUnreadAnnouncement
                .combine(dismissedInSession) { announcement, dismissedIds ->
                    if (announcement != null && dismissedIds.contains(announcement.id)) {
                        null // Don't show the announcement if it's in the dismissed set
                    } else {
                        announcement
                    }
                }
                .collect { finalAnnouncement ->
                    _state.update { it.copy(topAnnouncement = finalAnnouncement) }
                }
        }
    }

    fun processIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.LoadData -> {
                // Logic to re-fetch data
            }
            is DashboardIntent.DismissAnnouncement -> {
                markAsRead(intent.announcementId)
                dismissedInSession.update { it + intent.announcementId }
            }
            is DashboardIntent.AddAnnouncement -> {
                addAnnouncement(intent.announcement)
            }
            is DashboardIntent.DeleteAnnouncement -> {
                deleteAnnouncement(intent.announcement)
            }
        }
    }

    private fun addAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            repository.postAnnouncement(announcement)
        }
    }

    private fun deleteAnnouncement(announcement: Announcement) {
        viewModelScope.launch {
            repository.deleteAnnouncement(announcement)
        }
    }

    private fun markAsRead(id: Int) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }
}
