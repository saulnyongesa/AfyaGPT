package com.example.afyagpt.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.afyagpt.data.local.entity.VaccinationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for [VaccinationEntity].
 */
@Dao
interface VaccinationDao {

    /**
     * Inserts a new vaccination record.
     * @param v The [VaccinationEntity] to insert.
     * @return The row ID of the newly inserted record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccination(v: VaccinationEntity): Long

    /**
     * Updates an existing vaccination record.
     * @param v The [VaccinationEntity] to update.
     */
    @Update
    suspend fun updateVaccination(v: VaccinationEntity)

    /**
     * Retrieves all vaccination records for a specific patient.
     * @param patientId The ID of the patient.
     * @return A [Flow] emitting the list of [VaccinationEntity].
     */
    @Query("SELECT * FROM vaccinations WHERE patient_id = :patientId")
    fun getVaccinationsForPatient(patientId: Int): Flow<List<VaccinationEntity>>

    /**
     * Marks a vaccination as given with the administered date and administering user ID.
     * @param id The primary ID of the vaccination record.
     * @param date The date it was administered.
     * @param by The user ID who administered it.
     */
    @Query("UPDATE vaccinations SET status = 'GIVEN', administered_date = :date, administered_by = :by WHERE id = :id")
    suspend fun markAsGiven(id: Int, date: String, by: Int)

    /**
     * Retrieves all vaccinations that are overdue.
     * @return A [Flow] emitting the list of overdue [VaccinationEntity].
     */
    @Query("SELECT * FROM vaccinations WHERE status = 'PENDING' AND scheduled_date < date('now')")
    fun getOverdueVaccinations(): Flow<List<VaccinationEntity>>
}
