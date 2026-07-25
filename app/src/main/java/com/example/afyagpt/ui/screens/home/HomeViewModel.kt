package com.example.afyagpt.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afyagpt.data.local.entity.PatientEntity
import com.example.afyagpt.data.preferences.UserPreferences
import com.example.afyagpt.data.repository.AnnouncementRepository
import com.example.afyagpt.data.repository.AuthRepository
import com.example.afyagpt.data.repository.PatientRepository
import com.example.afyagpt.data.repository.RemoteAnnouncement
import com.example.afyagpt.data.repository.SyncRepository
import com.example.afyagpt.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * HomeUiState — State representation for the Home Dashboard.
 */
data class HomeUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val unsyncedRecords: Int = 0,
    val totalPatientsToday: Int = 0,
    val pendingFollowUps: Int = 0,
    val recentPatients: List<PatientEntity> = emptyList(),
    val announcements: List<RemoteAnnouncement> = emptyList(),
    val isLoggedOut: Boolean = false,
    val syncMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val patientRepository: PatientRepository,
    private val syncRepository: SyncRepository,
    private val announcementRepository: AnnouncementRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
        fetchRemoteAnnouncements()

        viewModelScope.launch {
            userPreferences.getSyncStatus().collect { (_, unsyncedCount) ->
                _uiState.update { it.copy(unsyncedRecords = unsyncedCount) }
            }
        }
    }

    /** Loads the current user and streams recent patients (up to 20) from Room DB. */
    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val user = authRepository.getCurrentUser()
            if (user == null) {
                logout()
                return@launch
            }

            patientRepository.getRecentPatients(limit = 20).collect { recentList ->
                _uiState.update {
                    it.copy(
                        user = user,
                        isLoading = false,
                        recentPatients = recentList,
                        totalPatientsToday = recentList.size,
                        unsyncedRecords = recentList.size
                    )
                }
            }
        }
    }

    /** Fetches live Ministry & Facility Directives from Heroku server when internet is available. */
    fun fetchRemoteAnnouncements() {
        viewModelScope.launch {
            val list = announcementRepository.fetchAnnouncements()
            if (list.isNotEmpty()) {
                _uiState.update { it.copy(announcements = list) }
            }
        }
    }

    /** Triggers a manual sync of all local patient data to Heroku backend. */
    fun syncNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = syncRepository.syncOfflineData()
            fetchRemoteAnnouncements()
            if (result.isSuccess) {
                val count = result.getOrDefault(0)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        unsyncedRecords = 0,
                        syncMessage = "Successfully synced $count records to server!"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        syncMessage = "Sync failed: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }
}
