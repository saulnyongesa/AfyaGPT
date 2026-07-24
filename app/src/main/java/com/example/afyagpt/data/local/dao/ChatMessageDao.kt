package com.example.afyagpt.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.afyagpt.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * ChatMessageDao.kt
 *
 * Data access object for AI Chat Assessment history.
 */
@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("SELECT * FROM chat_messages WHERE patient_id = :patientId ORDER BY timestamp ASC")
    fun getMessagesForPatient(patientId: Int): Flow<List<ChatMessageEntity>>

    @Query("DELETE FROM chat_messages WHERE patient_id = :patientId")
    suspend fun clearHistoryForPatient(patientId: Int)
}
