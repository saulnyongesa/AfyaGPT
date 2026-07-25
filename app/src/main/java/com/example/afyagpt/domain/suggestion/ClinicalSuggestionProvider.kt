package com.example.afyagpt.domain.suggestion

import com.example.afyagpt.data.local.entity.PatientEntity
import com.example.afyagpt.data.local.entity.TriageSessionEntity

/**
 * SuggestionSource
 * Indicates whether the classification / clinical recommendation came from
 * local IMCI offline rules or a remote AI engine.
 */
enum class SuggestionSource { LOCAL_RULES, REMOTE_AI }

/**
 * ClinicalSuggestion
 * Encapsulates computed classifications, treatment plan, and counseling points.
 */
data class ClinicalSuggestion(
    val respiratoryClass: String? = null,
    val diarrheaClass: String? = null,
    val feverClass: String? = null,
    val earClass: String? = null,
    val nutritionClass: String? = null,
    val overallRisk: String = "LOW",
    val treatmentPlan: List<String> = emptyList(),
    val counselingMessages: List<String> = emptyList(),
    val source: SuggestionSource = SuggestionSource.LOCAL_RULES
)

/**
 * ClinicalSuggestionProvider.kt
 *
 * Abstraction over how we get a classification and suggested plan for a patient.
 * LocalRuleSuggestionProvider (wrapping IMCIClassifier) is the permanent,
 * always available implementation.
 */
interface ClinicalSuggestionProvider {
    suspend fun suggest(
        patient: PatientEntity,
        currentSession: TriageSessionEntity,
        history: List<TriageSessionEntity> = emptyList()
    ): ClinicalSuggestion
}
