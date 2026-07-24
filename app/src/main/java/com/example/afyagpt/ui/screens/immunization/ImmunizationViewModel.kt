package com.example.afyagpt.ui.screens.immunization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afyagpt.data.local.entity.VaccinationEntity
import com.example.afyagpt.data.repository.PatientRepository
import com.example.afyagpt.data.repository.VaccinationRepository
import com.example.afyagpt.util.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ImmunizationUiState(
    val vaccinations: List<VaccinationEntity> = emptyList(),
    val isLoading: Boolean = true,
    val patientId: Int = 0,
    val patientName: String = "",
    val error: String? = null
)

@HiltViewModel
class ImmunizationViewModel @Inject constructor(
    private val vaccinationRepository: VaccinationRepository,
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ImmunizationUiState())
    val state: StateFlow<ImmunizationUiState> = _state.asStateFlow()

    fun loadForPatient(patientId: Int, patientName: String) {
        _state.update { it.copy(patientId = patientId, patientName = patientName, isLoading = true) }
        viewModelScope.launch {
            // Pre-check if schedule exists, if not generate it automatically
            val existing = vaccinationRepository.getVaccinationsForPatient(patientId)
            existing.collect { list ->
                if (list.isEmpty()) {
                    val patient = patientRepository.getPatientById(patientId)
                    if (patient != null) {
                        vaccinationRepository.generateEpiSchedule(
                            patientId = patientId,
                            dateOfBirth = patient.dateOfBirth,
                            registeredBy = patient.registeredBy
                        )
                    }
                }
                _state.update { it.copy(vaccinations = list, isLoading = false) }
            }
        }
    }

    fun markVaccineGiven(vaccinationId: Int, administeredBy: Int = 0) {
        viewModelScope.launch {
            val today = DateTimeUtils.today()
            vaccinationRepository.markVaccineGiven(vaccinationId, today, administeredBy)
        }
    }
}
