package com.example.afyagpt.data.repository

import com.example.afyagpt.data.local.dao.ChatMessageDao
import com.example.afyagpt.data.local.entity.ChatMessageEntity
import com.example.afyagpt.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * ChatRepository.kt
 *
 * Repository for managing AI Assessment Assistant chat history and decision support responses.
 */
class ChatRepository @Inject constructor(
    private val chatMessageDao: ChatMessageDao
) {
    fun getHistoryForPatient(patientId: Int): Flow<List<ChatMessageEntity>> =
        chatMessageDao.getMessagesForPatient(patientId)

    /**
     * Inserts a user message and generates an AI IMCI decision-support response,
     * persisting both to the Room database.
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

        // 2. Generate Clinical AI Response (WHO IMCI Decision Support Logic)
        val lower = userPrompt.lowercase()
        val aiResponseText = when {
            lower.contains("cough") || lower.contains("breathing") || lower.contains("chest") -> {
                "Based on WHO IMCI guidelines for $patientName: If chest indrawing or stridor is present, classify as SEVERE PNEUMONIA. Give first dose of oral Amoxicillin (40-50mg/kg/dose) and refer urgently to facility."
            }
            lower.contains("fever") || lower.contains("malaria") || lower.contains("temp") -> {
                "For fever assessment in high-risk zone: Perform Malaria Rapid Diagnostic Test (mRDT). If positive for P. falciparum, administer Artemisinin-based Combination Therapy (ACT) as per age weight schedule."
            }
            lower.contains("diarrhea") || lower.contains("stool") || lower.contains("dehydration") -> {
                "For diarrhea assessment: Check skin pinch (goes back >2s = Severe Dehydration) and sunken eyes. Give ORS + Zinc (20mg daily for 10-14 days). If severe dehydration, initiate Plan C IV fluids."
            }
            lower.contains("vitals") || lower.contains("weight") || lower.contains("muac") -> {
                "Vitals overview ($vitalsSummary): Check MUAC color band. <115mm indicates Severe Acute Malnutrition (SAM). Refer for Therapeutic Feeding (Ready-to-Use Therapeutic Food - RUTF)."
            }
            else -> {
                "AfyaGPT AI Assistant: Clinical notes received for $patientName. Ensure general danger signs (inability to drink, vomiting everything, convulsions, lethargy) are evaluated first before protocol classification."
            }
        }

        val aiMsg = ChatMessageEntity(
            patientId = patientId,
            sender = "AI",
            message = aiResponseText,
            timestamp = DateTimeUtils.now()
        )
        chatMessageDao.insertMessage(aiMsg)

        return aiMsg
    }
}
