/**
 * SearchViewModel.kt
 *
 * ViewModel for global categorized search across Patients and Clinical Protocols.
 * Package: com.example.afyagpt.ui.screens.search
 */
package com.example.afyagpt.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afyagpt.data.local.entity.PatientEntity
import com.example.afyagpt.data.repository.PatientRepository
import com.example.afyagpt.ui.screens.library.Protocol
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val patientResults: List<PatientEntity> = emptyList(),
    val protocolResults: List<Protocol> = emptyList()
)

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    // Standard clinical protocol corpus for global search
    private val clinicalProtocols = listOf(
        Protocol(
            id = "p1",
            title = "Severe Pneumonia",
            category = "Respiratory",
            description = "Severe infection of the lungs. Requires immediate antibiotic treatment and referral.",
            symptoms = listOf("Fast breathing", "Chest indrawing", "Stridor in calm child"),
            treatmentSteps = listOf("Give antibiotic", "Refer URGENTLY"),
            dangerSigns = listOf("Unable to drink", "Convulsions")
        ),
        Protocol(
            id = "p2",
            title = "Malaria",
            category = "Fever",
            description = "Mosquito-borne infection. Treat based on rapid diagnostic test (RDT) results.",
            symptoms = listOf("Fever", "Chills", "Headache"),
            treatmentSteps = listOf("Perform RDT", "Give ACT if positive"),
            dangerSigns = listOf("Stiff neck", "Lethargic")
        ),
        Protocol(
            id = "p3",
            title = "Severe Dehydration (Diarrhea)",
            category = "Gastrointestinal",
            description = "Critical loss of fluids due to acute watery diarrhea.",
            symptoms = listOf("Lethargic", "Sunken eyes", "Slow skin pinch"),
            treatmentSteps = listOf("Give fluid Plan C", "ORS on referral"),
            dangerSigns = listOf("Unconscious")
        )
    )

    init {
        viewModelScope.launch {
            searchQueryFlow
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    executeSearch(query)
                }
        }
    }

    fun onQueryChange(newQuery: String) {
        _state.update { it.copy(query = newQuery, isLoading = newQuery.isNotBlank()) }
        searchQueryFlow.value = newQuery
    }

    private suspend fun executeSearch(query: String) {
        if (query.isBlank()) {
            _state.update {
                it.copy(
                    isLoading = false,
                    patientResults = emptyList(),
                    protocolResults = emptyList()
                )
            }
            return
        }

        try {
            val patients = patientRepository.searchPatients(query).first()
            val protocols = clinicalProtocols.filter { p ->
                p.title.contains(query, ignoreCase = true) ||
                p.category.contains(query, ignoreCase = true) ||
                p.symptoms.any { sym -> sym.contains(query, ignoreCase = true) }
            }
            _state.update {
                it.copy(
                    isLoading = false,
                    patientResults = patients,
                    protocolResults = protocols
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(isLoading = false) }
        }
    }
}
