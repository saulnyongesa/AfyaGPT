package com.example.afyagpt.domain.suggestion

import com.example.afyagpt.data.local.entity.PatientEntity
import com.example.afyagpt.data.local.entity.TriageSessionEntity
import com.example.afyagpt.util.IMCIClassifier
import javax.inject.Inject
import javax.inject.Singleton

/**
 * LocalRuleSuggestionProvider.kt
 *
 * Wraps the existing IMCIClassifier object. This is the permanent fallback
 * and must remain fully functional and correct regardless of whether AI
 * is ever added.
 */
@Singleton
class LocalRuleSuggestionProvider @Inject constructor() : ClinicalSuggestionProvider {

    override suspend fun suggest(
        patient: PatientEntity,
        currentSession: TriageSessionEntity,
        history: List<TriageSessionEntity>
    ): ClinicalSuggestion {
        val respClass = IMCIClassifier.classifyRespiratory(
            ageMonths = 12,
            respRate = currentSession.respRate,
            chestIndrawing = false,
            stridor = false
        ).label

        val diarrheaClass = currentSession.diarrheaClass

        val feverClass = if (currentSession.temperatureC != null && currentSession.temperatureC >= 37.5f) {
            IMCIClassifier.classifyFever(
                hasFever = true,
                temperatureC = currentSession.temperatureC,
                rdtResult = "NOT_DONE",
                stiffNeck = false,
                measlesRash = false,
                daysWithFever = 2,
                malariaRiskZone = "HIGH"
            )?.label
        } else currentSession.feverClass

        val earClass = currentSession.earClass

        val (nutritionEnum, anemiaEnum) = IMCIClassifier.classifyNutrition(
            muacMm = currentSession.muacMm,
            visibleWasting = false,
            bilateralOedema = false,
            palmPallor = "NONE"
        )
        val nutritionClass = nutritionEnum.label

        val risk = currentSession.overallRisk.ifBlank { "LOW" }

        val treatments = mutableListOf<String>()
        val counseling = mutableListOf<String>()

        if (feverClass != null) {
            treatments.add("Paracetamol syrup (10-15 mg/kg) every 6 hours for fever > 38.5°C")
            counseling.add("Increase fluid intake and keep child comfortably cool")
        }

        if (nutritionClass.contains("Malnutrition", ignoreCase = true)) {
            treatments.add("Provide Ready-to-Use Therapeutic Food (RUTF) and check immunization status")
            counseling.add("Return for weekly weight check and MUAC monitoring")
        }

        if (treatments.isEmpty()) {
            treatments.add("Continue routine feeding and care at home")
            counseling.add("Return immediately if child develops danger signs (vomiting, convulsions, inability to drink)")
        }

        return ClinicalSuggestion(
            respiratoryClass = respClass,
            diarrheaClass = diarrheaClass,
            feverClass = feverClass,
            earClass = earClass,
            nutritionClass = nutritionClass,
            overallRisk = risk,
            treatmentPlan = treatments,
            counselingMessages = counseling,
            source = SuggestionSource.LOCAL_RULES
        )
    }
}
