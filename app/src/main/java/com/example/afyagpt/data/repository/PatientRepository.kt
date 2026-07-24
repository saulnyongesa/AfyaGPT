package com.example.afyagpt.data.repository

import com.example.afyagpt.data.local.dao.PatientDao
import com.example.afyagpt.data.local.entity.PatientEntity
import com.example.afyagpt.data.preferences.UserPreferences
import com.example.afyagpt.util.DateTimeUtils
import com.example.afyagpt.util.PatientIdGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * PatientRepository.kt
 *
 * Repository mediating patient CRUD operations and queries for triage workflows.
 * Automatically initializes Kenya EPI vaccination schedule upon registration.
 */
class PatientRepository @Inject constructor(
    private val patientDao: PatientDao,
    private val vaccinationRepository: VaccinationRepository,
    private val userPreferences: UserPreferences
) {
    /**
     * Inserts a new patient record into the local Room database, auto-generates
     * the 14-dose KEPI vaccination schedule, and returns the generated row ID.
     */
    suspend fun registerPatient(
        fullName: String,
        dateOfBirth: String,
        sex: String,
        birthCertificateNumber: String? = null,
        phoneNumber: String? = null,
        caregiverName: String? = null,
        guardianPhone: String? = null,
        guardianRelation: String? = null,
        village: String? = null,
        county: String? = null,
        facilityName: String
    ): Int {
        val uid = PatientIdGenerator.generate()
        val userId = userPreferences.getActiveUserId().first()
        val now = DateTimeUtils.now()
        
        val patient = PatientEntity(
            patientUid = uid,
            fullName = fullName,
            dateOfBirth = dateOfBirth,
            sex = sex,
            birthCertificateNumber = birthCertificateNumber?.ifBlank { null },
            phoneNumber = phoneNumber?.ifBlank { null },
            caregiverName = caregiverName?.ifBlank { null },
            guardianPhone = guardianPhone?.ifBlank { null },
            guardianRelation = guardianRelation?.ifBlank { null },
            village = village?.ifBlank { null },
            subLocation = null,
            county = county?.ifBlank { null },
            householdId = null,
            facilityName = facilityName,
            registeredBy = userId,
            createdAt = now,
            updatedAt = now,
            isActive = true
        )
        val insertedId = patientDao.insertPatient(patient).toInt()

        // Auto-generate Kenya EPI Vaccination Schedule for the new patient
        try {
            vaccinationRepository.generateEpiSchedule(
                patientId = insertedId,
                dateOfBirth = dateOfBirth,
                registeredBy = userId
            )
        } catch (e: Exception) {
            // Log or ignore schedule generation fallback
        }

        return insertedId
    }

    fun getAllPatients(): Flow<List<PatientEntity>> = patientDao.getAllPatients()
    fun searchPatients(query: String): Flow<List<PatientEntity>> = patientDao.searchPatients(query)
    fun getRecentPatients(limit: Int = 5): Flow<List<PatientEntity>> = patientDao.getRecentPatients(limit)
    suspend fun getPatientById(id: Int): PatientEntity? = patientDao.getPatientById(id)
    suspend fun updatePatient(patient: PatientEntity) = patientDao.updatePatient(patient)
}
