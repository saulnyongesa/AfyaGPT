package com.example.afyagpt.ui.screens.triage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afyagpt.data.local.entity.PatientEntity
import com.example.afyagpt.data.repository.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TriageUiState(
    val patients: List<PatientEntity> = emptyList(),
    val isPatientsLoading: Boolean = true,
    val searchQuery: String = "",
    val currentStep: Int = 0,            // 0=PatientSelect, 1=DangerSigns, 2=Vitals, 3A=Cough, 3B=Diarrhea, 3C=Fever, 3D=Ear, 4=Nutrition, 5=Result
    val selectedPatientId: Int? = null,
    val selectedPatientName: String = "",
    val selectedPatientAgeMonths: Int = 0,
    val visitType: String = "FACILITY", // FACILITY or CHW_HOME_VISIT
    val visitLocationNote: String = "",
    val suggestionSource: String = "LOCAL_RULES",
    
    // Step 1: Danger Signs
    val unableToDrink: Boolean = false,
    val vomitingEverything: Boolean = false,
    val convulsions: Boolean = false,
    val lethargic: Boolean = false,
    
    // Step 2: Vitals
    val weightKg: String = "",
    val heightCm: String = "",
    val temperatureC: String = "",
    val muacMm: String = "",
    val respRate: Int = 0,
    val o2SatPct: String = "",
    
    // Step 3A: Cough / Breathing
    val hasCough: Boolean = false,
    val coughDays: String = "",
    val chestIndrawing: Boolean = false,
    val stridor: Boolean = false,
    val wheezing: Boolean = false,
    
    // Step 3B: Diarrhea
    val hasDiarrhea: Boolean = false,
    val diarrheaDays: String = "",
    val bloodInStool: Boolean = false,
    val sunkenEyes: Boolean = false,
    val skinPinch: String = "NORMAL", // NORMAL, SLOW, VERY_SLOW
    val drinkingAbility: String = "NORMAL", // NORMAL, POOR, EAGER
    
    // Step 3C: Fever
    val hasFever: Boolean = false,
    val feverDays: String = "",
    val malariaRisk: String = "HIGH", // HIGH, LOW, NONE
    val rdtResult: String = "NOT_DONE", // NOT_DONE, POSITIVE, NEGATIVE
    val stiffNeck: Boolean = false,
    val bulgingFontanelle: Boolean = false,
    val measlesInLast3Months: Boolean = false,
    val measlesRash: Boolean = false,
    
    // Step 3D: Ear Problem
    val hasEarProblem: Boolean = false,
    val earPain: Boolean = false,
    val earDischarge: Boolean = false,
    val dischargeDays: String = "",
    val tenderSwellingBehindEar: Boolean = false,
    
    // Step 4: Malnutrition & Anemia
    val visibleWasting: Boolean = false,
    val oedemaBothFeet: Boolean = false,
    val palmarPallor: String = "NONE", // NONE, SOME, SEVERE
    
    // Step 5: Computed Classifications
    val respiratoryClass: String? = null,
    val diarrheaClass: String? = null,
    val feverClass: String? = null,
    val earClass: String? = null,
    val nutritionClass: String? = null,
    val anemiaClass: String? = null,
    val overallRisk: String = "LOW",
    val treatmentPlan: List<String> = emptyList(),
    val counselingMessages: List<String> = emptyList(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
) {
    // Property aliases for screen compatibility
    val earDischargeDays: String get() = dischargeDays
    val mastoidTenderness: Boolean get() = tenderSwellingBehindEar
    val bilateralOedema: Boolean get() = oedemaBothFeet
    val palmPallor: String get() = palmarPallor
}

@HiltViewModel
class TriageViewModel @Inject constructor(
    private val patientRepository: PatientRepository
) : ViewModel() {
    private val _state = MutableStateFlow(TriageUiState())
    val state: StateFlow<TriageUiState> = _state.asStateFlow()

    init {
        loadPatients()
    }

    fun loadPatients() {
        viewModelScope.launch {
            patientRepository.getAllPatients().collect { list ->
                _state.update { it.copy(patients = list, isPatientsLoading = false) }
            }
        }
    }

    fun searchPatients(query: String) {
        _state.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            patientRepository.searchPatients(query).collect { list ->
                _state.update { it.copy(patients = list) }
            }
        }
    }

    fun selectPatient(id: Int, name: String, ageMonths: Int) {
        _state.update { it.copy(selectedPatientId = id, selectedPatientName = name, selectedPatientAgeMonths = ageMonths) }
    }

    fun setVisitType(type: String) {
        _state.update { it.copy(visitType = type) }
    }

    fun setVisitLocationNote(note: String) {
        _state.update { it.copy(visitLocationNote = note) }
    }

    fun updateDangerSign(field: String, value: Boolean) {
        _state.update { currentState ->
            when (field) {
                "unableToDrink" -> currentState.copy(unableToDrink = value)
                "vomitingEverything" -> currentState.copy(vomitingEverything = value)
                "convulsions" -> currentState.copy(convulsions = value)
                "lethargic" -> currentState.copy(lethargic = value)
                else -> currentState
            }
        }
    }

    fun updateVitals(field: String, value: String) {
        _state.update { currentState ->
            when (field) {
                "weightKg" -> currentState.copy(weightKg = value)
                "temperatureC" -> currentState.copy(temperatureC = value)
                "muacMm" -> currentState.copy(muacMm = value)
                else -> currentState
            }
        }
    }

    fun setRespRate(rate: Int) {
        _state.update { it.copy(respRate = rate) }
    }

    fun updateCough(field: String, value: Any) {
        _state.update { currentState ->
            when (field) {
                "hasCough" -> currentState.copy(hasCough = value as Boolean)
                "chestIndrawing" -> currentState.copy(chestIndrawing = value as Boolean)
                "stridor" -> currentState.copy(stridor = value as Boolean)
                else -> currentState
            }
        }
    }

    fun updateDiarrhea(field: String, value: Any) {
        _state.update { currentState ->
            when (field) {
                "hasDiarrhea" -> currentState.copy(hasDiarrhea = value as Boolean)
                "diarrheaDays" -> currentState.copy(diarrheaDays = value as String)
                "bloodInStool" -> currentState.copy(bloodInStool = value as Boolean)
                "sunkenEyes" -> currentState.copy(sunkenEyes = value as Boolean)
                "skinPinch" -> currentState.copy(skinPinch = value as String)
                "drinkingAbility" -> currentState.copy(drinkingAbility = value as String)
                else -> currentState
            }
        }
    }

    fun updateFever(field: String, value: Any) {
        _state.update { currentState ->
            when (field) {
                "hasFever" -> currentState.copy(hasFever = value as Boolean)
                "feverDays" -> currentState.copy(feverDays = value as String)
                "rdtResult" -> currentState.copy(rdtResult = value as String)
                "stiffNeck" -> currentState.copy(stiffNeck = value as Boolean)
                "bulgingFontanelle" -> currentState.copy(bulgingFontanelle = value as Boolean)
                "measlesRash", "measlesInLast3Months" -> currentState.copy(measlesRash = value as Boolean, measlesInLast3Months = value as Boolean)
                else -> currentState
            }
        }
    }

    fun updateEar(field: String, value: Any) {
        _state.update { currentState ->
            when (field) {
                "hasEarProblem" -> currentState.copy(hasEarProblem = value as Boolean)
                "earPain" -> currentState.copy(earPain = value as Boolean)
                "earDischarge" -> currentState.copy(earDischarge = value as Boolean)
                "dischargeDays", "earDischargeDays" -> currentState.copy(dischargeDays = value as String)
                "tenderSwellingBehindEar", "mastoidTenderness" -> currentState.copy(tenderSwellingBehindEar = value as Boolean)
                else -> currentState
            }
        }
    }

    fun updateNutrition(field: String, value: Any) {
        _state.update { currentState ->
            when (field) {
                "visibleWasting" -> currentState.copy(visibleWasting = value as Boolean)
                "oedemaBothFeet", "bilateralOedema" -> currentState.copy(oedemaBothFeet = value as Boolean)
                "palmarPallor", "palmPallor" -> currentState.copy(palmarPallor = value as String)
                else -> currentState
            }
        }
    }

    fun runClassification() = evaluateClassifications()

    fun evaluateClassifications() {
        val s = _state.value

        // Respiratory
        val respClass = when {
            s.unableToDrink || s.vomitingEverything || s.convulsions || s.lethargic || s.chestIndrawing || s.stridor -> "SEVERE PNEUMONIA"
            s.hasCough && s.respRate >= 50 -> "PNEUMONIA"
            s.hasCough -> "NO PNEUMONIA: COUGH OR COLD"
            else -> null
        }

        // Diarrhea
        val diarrheaClass = when {
            s.hasDiarrhea && (s.lethargic || s.sunkenEyes || s.skinPinch == "VERY_SLOW") -> "SEVERE DEHYDRATION"
            s.hasDiarrhea && (s.drinkingAbility == "EAGER" || s.skinPinch == "SLOW") -> "SOME DEHYDRATION"
            s.hasDiarrhea -> "NO DEHYDRATION"
            else -> null
        }

        // Fever
        val feverClass = when {
            s.hasFever && (s.stiffNeck || s.bulgingFontanelle || s.lethargic) -> "VERY SEVERE FEBRILE DISEASE"
            s.hasFever && s.rdtResult == "POSITIVE" -> "MALARIA"
            s.hasFever -> "FEVER: NO MALARIA"
            else -> null
        }

        // Ear
        val earClass = when {
            s.tenderSwellingBehindEar -> "MASTOIDITIS"
            s.hasEarProblem && s.earDischarge -> "ACUTE EAR INFECTION"
            s.hasEarProblem -> "NO EAR INFECTION"
            else -> null
        }

        // Nutrition
        val muacVal = s.muacMm.toIntOrNull() ?: 999
        val nutritionClass = when {
            s.oedemaBothFeet || s.visibleWasting || muacVal < 115 -> "SEVERE ACUTE MALNUTRITION"
            muacVal in 115..124 -> "MODERATE ACUTE MALNUTRITION"
            else -> "NO MALNUTRITION"
        }

        // Determine overall risk
        val isCritical = respClass == "SEVERE PNEUMONIA" || diarrheaClass == "SEVERE DEHYDRATION" || feverClass == "VERY SEVERE FEBRILE DISEASE" || earClass == "MASTOIDITIS" || s.unableToDrink || s.convulsions
        val isHigh = respClass == "PNEUMONIA" || diarrheaClass == "SOME DEHYDRATION" || feverClass == "MALARIA" || nutritionClass == "SEVERE ACUTE MALNUTRITION"

        val overallRisk = when {
            isCritical -> "CRITICAL"
            isHigh -> "HIGH"
            else -> "LOW"
        }

        val counseling = mutableListOf<String>()
        if (respClass != null) counseling.add("Administer oral Amoxicillin for respiratory infection as per WHO guidelines.")
        if (diarrheaClass != null) counseling.add("Give Zinc 20mg daily for 10-14 days and ORS after each loose stool.")
        if (feverClass == "MALARIA") counseling.add("Administer first-line ACT (Coartem) according to child weight schedule.")

        _state.update {
            it.copy(
                respiratoryClass = respClass,
                diarrheaClass = diarrheaClass,
                feverClass = feverClass,
                earClass = earClass,
                nutritionClass = nutritionClass,
                overallRisk = overallRisk,
                counselingMessages = counseling
            )
        }
    }
}
