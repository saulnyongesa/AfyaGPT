package com.example.afyagpt.data.repository

import com.example.afyagpt.data.local.dao.ChatMessageDao
import com.example.afyagpt.data.local.dao.PatientDao
import com.example.afyagpt.data.local.dao.TriageDao
import com.example.afyagpt.data.local.entity.ChatMessageEntity
import com.example.afyagpt.domain.suggestion.ClinicalSuggestionCoordinator
import com.example.afyagpt.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * ChatRepository.kt
 *
 * Repository for managing AI Assessment Assistant chat history and decision support responses.
 * Context-aware: Loads actual patient triage history and routes suggestions through
 * ClinicalSuggestionCoordinator instead of generic keyword matching.
 */
class ChatRepository @Inject constructor(
    private val chatMessageDao: ChatMessageDao,
    private val patientDao: PatientDao,
    private val triageDao: TriageDao,
    private val coordinator: ClinicalSuggestionCoordinator
) {
    fun getHistoryForPatient(patientId: Int): Flow<List<ChatMessageEntity>> =
        chatMessageDao.getMessagesForPatient(patientId)

    /**
     * Inserts a user message and generates a patient-specific AI IMCI decision-support response,
     * persisting both to the Room database with suggestion source tracking.
     */
    suspend fun sendMessage(
        patientId: Int,
        userPrompt: String,
        patientName: String = "",
        vitalsSummary: String = ""
    ): ChatMessageEntity {
        val now = DateTimeUtils.now()

        // 1. Insert User Message
        val userMsg = ChatMessageEntity(
            patientId = patientId,
            sender = "USER",
            message = userPrompt,
            timestamp = now
        )
        chatMessageDao.insertMessage(userMsg)

        // 2. Fetch Patient Entity & Triage History from Database
        val patient = patientDao.getPatientById(patientId)
        val history = triageDao.getSessionsForPatientList(patientId)
        val latestSession = history.firstOrNull()

        // 3. Obtain Clinical Suggestion from Coordinator (Remote AI or Local IMCI Rules)
        val suggestion = if (patient != null && latestSession != null) {
            coordinator.getSuggestion(patient, latestSession, history)
        } else null

        // 4. Construct Patient-Specific AI Response
        val responseBuilder = StringBuilder()
        val displayName = patient?.fullName ?: patientName.ifBlank { "Patient" }

        responseBuilder.append("Clinical Assessment for $displayName:\n")

        if (latestSession != null) {
            responseBuilder.append("• Overall Risk: ${latestSession.overallRisk}\n")
            if (!latestSession.respiratoryClass.isNullOrBlank()) responseBuilder.append("• Respiratory: ${latestSession.respiratoryClass}\n")
            if (!latestSession.diarrheaClass.isNullOrBlank()) responseBuilder.append("• Diarrhea: ${latestSession.diarrheaClass}\n")
            if (!latestSession.feverClass.isNullOrBlank()) responseBuilder.append("• Fever: ${latestSession.feverClass}\n")
            if (!latestSession.nutritionClass.isNullOrBlank()) responseBuilder.append("• Nutrition: ${latestSession.nutritionClass}\n")
            responseBuilder.append("• Visit Channel: ${latestSession.visitType}\n")
        } else {
            responseBuilder.append("• No prior triage sessions recorded for this patient.\n")
        }

        if (suggestion != null && suggestion.treatmentPlan.isNotEmpty()) {
            responseBuilder.append("\nRecommended Protocol Actions:\n")
            suggestion.treatmentPlan.forEach { action ->
                responseBuilder.append(" - $action\n")
            }
        }

        val engineTag = if (suggestion?.source?.name == "REMOTE_AI") "[Engine: Remote AI]" else "[Engine: Local IMCI Rules]"
        responseBuilder.append("\n$engineTag (Ref: WHO/MOH IMCI Standards)")

        val aiMsgText = responseBuilder.toString()

        val aiMsg = ChatMessageEntity(
            patientId = patientId,
            sender = "AI",
            message = aiMsgText,
            suggestedRiskLevel = latestSession?.overallRisk ?: "LOW",
            timestamp = DateTimeUtils.now()
        )
        chatMessageDao.insertMessage(aiMsg)

        return aiMsg
    }
}
