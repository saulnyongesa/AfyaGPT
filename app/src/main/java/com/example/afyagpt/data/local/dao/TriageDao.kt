package com.example.afyagpt.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.afyagpt.data.local.entity.TriageSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [TriageSessionEntity].
 */
@Dao
interface TriageDao {

    /**
     * Inserts a new triage session into the database.
     * @param session The [TriageSessionEntity] to insert.
     * @return The row ID of the newly inserted session.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TriageSessionEntity): Long

    /**
     * Updates an existing triage session.
     * @param session The [TriageSessionEntity] to update.
     */
    @Update
    suspend fun updateSession(session: TriageSessionEntity)

    /**
     * Retrieves all triage sessions for a specific patient, ordered by creation date descending.
     * @param patientId The ID of the patient.
     * @return A [Flow] emitting the list of sessions.
     */
    @Query("SELECT * FROM triage_sessions WHERE patient_id = :patientId ORDER BY created_at DESC")
    fun getSessionsForPatient(patientId: Int): Flow<List<TriageSessionEntity>>

    /**
     * Retrieves the most recent triage sessions across all patients.
     * @param limit The maximum number of sessions to return.
     * @return A [Flow] emitting the list of sessions.
     */
    @Query("SELECT * FROM triage_sessions ORDER BY created_at DESC LIMIT :limit")
    fun getRecentSessions(limit: Int): Flow<List<TriageSessionEntity>>

    /**
     * Retrieves a triage session by its primary ID.
     * @param id The primary ID of the session.
     * @return The [TriageSessionEntity] if found, otherwise null.
     */
    @Query("SELECT * FROM triage_sessions WHERE id = :id")
    suspend fun getSessionById(id: Int): TriageSessionEntity?

    /**
     * Counts the number of triage sessions created on a specific date.
     * @param date The date string to check (e.g., YYYY-MM-DD).
     * @return The count of sessions created on that date.
     */
    @Query("SELECT COUNT(*) FROM triage_sessions WHERE DATE(created_at) = :date")
    suspend fun countSessionsToday(date: String): Int
}
