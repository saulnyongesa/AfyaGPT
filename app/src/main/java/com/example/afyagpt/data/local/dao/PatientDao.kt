package com.example.afyagpt.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.afyagpt.data.local.entity.PatientEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [PatientEntity].
 */
@Dao
interface PatientDao {

    /**
     * Inserts a new patient into the database.
     * @param patient The [PatientEntity] to insert.
     * @return The row ID of the newly inserted patient.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: PatientEntity): Long

    /**
     * Updates an existing patient in the database.
     * @param patient The [PatientEntity] to update.
     */
    @Update
    suspend fun updatePatient(patient: PatientEntity)

    /**
     * Retrieves a patient by their primary ID.
     * @param id The primary key of the patient.
     * @return The [PatientEntity] if found, otherwise null.
     */
    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getPatientById(id: Int): PatientEntity?

    /**
     * Retrieves a patient by their unique UID.
     * @param uid The UID of the patient.
     * @return The [PatientEntity] if found, otherwise null.
     */
    @Query("SELECT * FROM patients WHERE patient_uid = :uid")
    suspend fun getPatientByUid(uid: String): PatientEntity?

    /**
     * Retrieves all patients ordered by their last update time (descending).
     * @return A [Flow] emitting the list of [PatientEntity].
     */
    @Query("SELECT * FROM patients ORDER BY updated_at DESC")
    fun getAllPatients(): Flow<List<PatientEntity>>

    /**
     * Searches for patients by name or UID.
     * @param query The search query string.
     * @return A [Flow] emitting the matching list of [PatientEntity].
     */
    @Query("SELECT * FROM patients WHERE full_name LIKE '%' || :query || '%' OR patient_uid LIKE '%' || :query || '%'")
    fun searchPatients(query: String): Flow<List<PatientEntity>>

    /**
     * Retrieves the most recently updated patients.
     * @param limit The maximum number of patients to return.
     * @return A [Flow] emitting the list of [PatientEntity].
     */
    @Query("SELECT * FROM patients ORDER BY updated_at DESC LIMIT :limit")
    fun getRecentPatients(limit: Int): Flow<List<PatientEntity>>

    /**
     * Counts the number of patients registered on a specific date.
     * @param date The date string in YYYY-MM-DD format.
     * @return The number of patients registered on that date.
     */
    @Query("SELECT COUNT(*) FROM patients WHERE DATE(created_at) = :date")
    suspend fun countPatientsToday(date: String): Int

    /**
     * Deactivates a patient by setting is_active to false (0).
     * @param id The primary key of the patient.
     */
    @Query("UPDATE patients SET is_active = 0 WHERE id = :id")
    suspend fun deactivatePatient(id: Int)
}
