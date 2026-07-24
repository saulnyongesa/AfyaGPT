package com.example.afyagpt.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ChatMessageEntity.kt
 *
 * Room entity representing an AI Assessment Chat message stored per patient.
 */
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "patient_id") val patientId: Int,
    @ColumnInfo(name = "sender") val sender: String, // "USER" or "AI"
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "suggested_risk_level") val suggestedRiskLevel: String? = null,
    @ColumnInfo(name = "timestamp") val timestamp: String
)
