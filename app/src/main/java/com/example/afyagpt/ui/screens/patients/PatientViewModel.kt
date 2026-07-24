package com.example.afyagpt.ui.screens.patients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afyagpt.data.local.entity.PatientEntity
import com.example.afyagpt.data.repository.AuthRepository
import com.example.afyagpt.data.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PatientUiState(
    val patients: List<PatientEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val selectedPatient: PatientEntity? = null,
    // Registration form fields
    val regFullName: String = "",
    val regDob: String = "",
    val regSex: String = "MALE",
    val regBirthCert: String = "",
    val regPhone: String = "",
    val regCaregiver: String = "",
    val regGuardianPhone: String = "",
    val regGuardianRelation: String = "Parent",
    val regVillage: String = "",
    val regCounty: String = "",
    val regFacility: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val newlySavedPatientId: Int? = null,
    val error: String? = null
)

@HiltViewModel
class PatientViewModel @Inject constructor(
    private val patientRepository: PatientRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _state = MutableStateFlow(PatientUiState())
    val state: StateFlow<PatientUiState> = _state.asStateFlow()

    init {
        loadPatients()
        preloadUserFacility()
    }

    private fun preloadUserFacility() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null && _state.value.regFacility.isBlank()) {
                _state.update { it.copy(regFacility = currentUser.facilityName, regCounty = currentUser.county) }
            }
        }
    }

    fun loadPatients() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            patientRepository.getAllPatients().collect { list ->
                _state.update { it.copy(patients = list, isLoading = false) }
            }
        }
    }

    fun search(query: String) {
        _state.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            patientRepository.searchPatients(query).collect { list ->
                _state.update { it.copy(patients = list) }
            }
        }
    }

    fun selectPatient(patient: PatientEntity) { _state.update { it.copy(selectedPatient = patient) } }

    fun loadPatientById(id: Int) {
        viewModelScope.launch {
            val patient = patientRepository.getPatientById(id)
            if (patient != null) {
                _state.update { it.copy(selectedPatient = patient) }
            }
        }
    }

    fun updateRegField(field: String, value: String) {
        _state.update { s -> when(field) {
            "fullName" -> s.copy(regFullName = value)
            "dob" -> s.copy(regDob = value)
            "sex" -> s.copy(regSex = value)
            "birthCert" -> s.copy(regBirthCert = value)
            "phone" -> s.copy(regPhone = value)
            "caregiver" -> s.copy(regCaregiver = value)
            "guardianPhone" -> s.copy(regGuardianPhone = value)
            "guardianRelation" -> s.copy(regGuardianRelation = value)
            "village" -> s.copy(regVillage = value)
            "county" -> s.copy(regCounty = value)
            "facility" -> s.copy(regFacility = value)
            else -> s
        }}
    }

    fun registerPatient() {
        viewModelScope.launch {
            val s = _state.value
            if (s.regFullName.isBlank() || s.regDob.isBlank() || s.regFacility.isBlank()) {
                _state.update { it.copy(error = "Full name, date of birth, and facility name are required.") }
                return@launch
            }
            _state.update { it.copy(isSaving = true, error = null) }
            try {
                val newId = patientRepository.registerPatient(
                    fullName = s.regFullName,
                    dateOfBirth = s.regDob,
                    sex = s.regSex,
                    birthCertificateNumber = s.regBirthCert.ifBlank { null },
                    phoneNumber = s.regPhone.ifBlank { null },
                    caregiverName = s.regCaregiver.ifBlank { null },
                    guardianPhone = s.regGuardianPhone.ifBlank { null },
                    guardianRelation = s.regGuardianRelation.ifBlank { null },
                    village = s.regVillage.ifBlank { null },
                    county = s.regCounty.ifBlank { null },
                    facilityName = s.regFacility
                )
                _state.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true,
                        newlySavedPatientId = newId,
                        // Reset form fields
                        regFullName = "",
                        regDob = "",
                        regBirthCert = "",
                        regPhone = "",
                        regCaregiver = "",
                        regGuardianPhone = ""
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isSaving = false, error = e.localizedMessage ?: "Registration failed") }
            }
        }
    }

    fun clearError() { _state.update { it.copy(error = null) } }
    fun clearSaveSuccess() { _state.update { it.copy(saveSuccess = false, newlySavedPatientId = null) } }
}
