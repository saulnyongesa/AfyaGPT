package com.example.afyagpt.ui.screens.library

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class Protocol(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val symptoms: List<String>,
    val treatmentSteps: List<String>,
    val dangerSigns: List<String>
)

data class LibraryState(
    val searchQuery: String = "",
    val protocols: List<Protocol> = emptyList(),
    val filteredProtocols: List<Protocol> = emptyList(),
    val selectedProtocol: Protocol? = null
)

class LibraryViewModel : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    init {
        loadProtocols()
    }

    private fun loadProtocols() {
        val imciProtocols = listOf(
            Protocol(
                id = "p1",
                title = "Severe Pneumonia & ARI",
                category = "Respiratory",
                description = "Acute lower respiratory infection of the lungs. Requires immediate parenteral or oral antibiotic administration and urgent hospital referral.",
                symptoms = listOf(
                    "Fast breathing (2-11 mos: >=50/min, 12-59 mos: >=40/min)",
                    "Chest indrawing (lower chest wall moves in on inhalation)",
                    "Stridor or wheezing in a calm child"
                ),
                treatmentSteps = listOf(
                    "Give first dose of oral Amoxicillin (40-50 mg/kg/dose)",
                    "Refer URGENTLY to district hospital or referral facility",
                    "Keep child warm during transport",
                    "Administer oxygen if oxygen saturation < 90%"
                ),
                dangerSigns = listOf("Unable to drink or breastfeed", "Vomiting everything", "Convulsions", "Lethargic or unconscious")
            ),
            Protocol(
                id = "p2",
                title = "Uncomplicated & Severe Malaria",
                category = "Fever",
                description = "Plasmodium falciparum infection transmitted by Anopheles mosquitoes. Diagnosis confirmed via mRDT or blood smear.",
                symptoms = listOf(
                    "Fever (history of fever or body temperature >= 37.5°C)",
                    "Chills, rigor, and headache",
                    "Severe pallor or jaundice"
                ),
                treatmentSteps = listOf(
                    "Perform Malaria Rapid Diagnostic Test (mRDT)",
                    "If mRDT positive: Give 3-day course of Artemisinin-based Combination Therapy (AL / Coartem)",
                    "If severe signs present: Give first dose of IV/IM Artesunate (2.4 mg/kg) and refer URGENTLY",
                    "Give Paracetamol (10-15 mg/kg) for high fever >= 38.5°C"
                ),
                dangerSigns = listOf("Stiff neck", "Bulging fontanelle", "Jaundice", "Convulsions during illness")
            ),
            Protocol(
                id = "p3",
                title = "Acute Diarrhea & Dehydration Plan",
                category = "Gastrointestinal",
                description = "Acute loss of fluid and electrolytes. Assessment categorizes into Severe Dehydration, Some Dehydration, or No Dehydration.",
                symptoms = listOf(
                    "Lethargic or unconscious (Severe Dehydration)",
                    "Sunken eyes and skin pinch returning very slowly (> 2 seconds)",
                    "Restless, irritable, drinking eagerly (Some Dehydration)"
                ),
                treatmentSteps = listOf(
                    "Severe Dehydration (Plan C): Start IV Ringer's Lactate (100 ml/kg) or refer URGENTLY with mother giving sips of ORS",
                    "Some Dehydration (Plan B): Give 75 ml/kg ORS over 4 hours at facility",
                    "No Dehydration (Plan A): Give extra fluids, ORS home pack, and Zinc supplementation (20 mg daily for 10-14 days)",
                    "Continue frequent breastfeeding throughout"
                ),
                dangerSigns = listOf("Blood in stool (Dysentery)", "Lethargy/Unconsciousness")
            ),
            Protocol(
                id = "p4",
                title = "Severe Acute Malnutrition (SAM)",
                category = "Nutrition & Growth",
                description = "Critical wasting or nutritional oedema placing child at extreme risk of mortality.",
                symptoms = listOf(
                    "Mid-Upper Arm Circumference (MUAC) < 115 mm (Red zone)",
                    "Bilateral pitting oedema of feet and lower legs",
                    "Severe visible wasting (sagging skin folds on buttocks)"
                ),
                treatmentSteps = listOf(
                    "Check appetite test with Ready-to-Use Therapeutic Food (RUTF)",
                    "If appetite test passed and no medical complications: Enroll in Outpatient Therapeutic Program (OTP) with RUTF",
                    "If appetite test failed or medical complications present: Refer URGENTLY to Stabilization Center (Inpatient)",
                    "Give single dose of Vitamin A and Amoxicillin course"
                ),
                dangerSigns = listOf("Failed appetite test", "Hypothermia (< 35.5°C)", "Severe oedema (+3)")
            ),
            Protocol(
                id = "p5",
                title = "Acute Otitis Media & Mastoiditis",
                category = "Ear Infection",
                description = "Infection of the middle ear space or mastoid bone structure behind the ear pinna.",
                symptoms = listOf(
                    "Tender swelling behind the ear (Mastoiditis)",
                    "Ear pain or ear discharge lasting < 14 days",
                    "Pus draining from ear"
                ),
                treatmentSteps = listOf(
                    "If Mastoiditis: Give first dose of IM/IV antibiotic and Refer URGENTLY to ENT specialist",
                    "If Acute Ear Infection (<14 days): Give 5-day course of oral Amoxicillin",
                    "Wick ear dry with clean absorbent cloth 3 times daily",
                    "Give Paracetamol for ear pain"
                ),
                dangerSigns = listOf("Mastoid tenderness", "Swelling behind ear", "Severe headache")
            ),
            Protocol(
                id = "p6",
                title = "Neonatal Sepsis & Jaundice",
                category = "Young Infant (0-2 Months)",
                description = "Bacterial infection or hyperbilirubinemia in young infants under 2 months of age.",
                symptoms = listOf(
                    "Poor feeding or unable to suckle",
                    "Fever (>=37.5°C) or Hypothermia (<35.5°C)",
                    "Yellowing of palms and soles (Severe Jaundice)",
                    "Umblicus red or draining pus"
                ),
                treatmentSteps = listOf(
                    "Give first dose of IM Ampicillin (50 mg/kg) and Gentamicin (5 mg/kg)",
                    "Refer URGENTLY to neonatal intensive care / hospital",
                    "Re-warm infant during transport (Kangaroo Mother Care)",
                    "Keep infant dry and breastfeed frequently on way"
                ),
                dangerSigns = listOf("Convulsions", "Fast breathing >= 60/min", "Severe chest indrawing", "Jaundice on soles")
            )
        )
        _state.update { it.copy(protocols = imciProtocols, filteredProtocols = imciProtocols) }
    }

    fun search(query: String) {
        val filtered = if (query.isBlank()) {
            _state.value.protocols
        } else {
            _state.value.protocols.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.category.contains(query, ignoreCase = true) ||
                it.symptoms.any { sym -> sym.contains(query, ignoreCase = true) }
            }
        }
        _state.update { it.copy(searchQuery = query, filteredProtocols = filtered) }
    }

    fun selectProtocol(protocol: Protocol?) {
        _state.update { it.copy(selectedProtocol = protocol) }
    }
}
