package com.example.afyagpt.data.repository

import com.example.afyagpt.data.local.dao.PatientDao
import com.example.afyagpt.data.local.entity.PatientEntity
import com.example.afyagpt.data.preferences.UserPreferences
import com.example.afyagpt.util.AppConstants
import com.example.afyagpt.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SyncRepository.kt — Bi-directional Delta Data Synchronization Manager
 *
 * Uploads local offline patient records & triage sessions to the live Heroku Django REST backend,
 * and downloads recent facility patient profiles updated by supervisors/admins.
 */
@Singleton
class SyncRepository @Inject constructor(
    private val patientDao: PatientDao,
    private val userPreferences: UserPreferences
) {
    suspend fun syncOfflineData(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val patients = patientDao.getAllPatients().first()
            val activeFacility = patients.firstOrNull()?.facilityName ?: "Health Center"

            val jsonPatients = JSONArray()
            for (p in patients) {
                val obj = JSONObject().apply {
                    put("patientUid", p.patientUid)
                    put("fullName", p.fullName)
                    put("dateOfBirth", p.dateOfBirth)
                    put("sex", p.sex)
                    put("caregiverName", p.caregiverName ?: "")
                    put("guardianPhone", p.guardianPhone ?: "")
                    put("birthCertificateNumber", p.birthCertificateNumber ?: "")
                    put("facilityName", p.facilityName)
                    put("county", p.county ?: "")
                    put("riskLevel", p.riskLevel)
                }
                jsonPatients.put(obj)
            }

            val payload = JSONObject().apply {
                put("patients", jsonPatients)
                put("facilityName", activeFacility)
            }

            val url = URL(AppConstants.BACKEND_BASE_URL + "sync/")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                doOutput = true
                connectTimeout = 10000
                readTimeout = 10000
            }

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val responseJson = JSONObject(responseText)

                // Silent Upsert Downloaded Delta Patients from Backend
                val downloadedArray = responseJson.optJSONArray("downloadedPatients")
                if (downloadedArray != null) {
                    for (i in 0 until downloadedArray.length()) {
                        val d = downloadedArray.getJSONObject(i)
                        val pUid = d.optString("patient_uid")
                        if (pUid.isNotBlank()) {
                            val existing = patientDao.getPatientByUid(pUid)
                            val entity = PatientEntity(
                                id = existing?.id ?: 0,
                                patientUid = pUid,
                                fullName = d.optString("full_name", "Unknown Patient"),
                                dateOfBirth = d.optString("date_of_birth", "2024-01-01"),
                                sex = d.optString("sex", "Male"),
                                caregiverName = d.optString("caregiver_name").takeIf { it.isNotBlank() },
                                guardianPhone = d.optString("guardian_phone").takeIf { it.isNotBlank() },
                                birthCertificateNumber = d.optString("birth_certificate_number").takeIf { it.isNotBlank() },
                                facilityName = d.optString("facility_name", activeFacility),
                                county = d.optString("county", "Nairobi"),
                                riskLevel = d.optString("risk_level", "LOW"),
                                registeredBy = existing?.registeredBy ?: 1,
                                createdAt = existing?.createdAt ?: DateTimeUtils.now(),
                                updatedAt = DateTimeUtils.now()
                            )
                            patientDao.insertPatient(entity)
                        }
                    }
                }

                userPreferences.updateSyncStatus(DateTimeUtils.now(), 0)
                Result.success(patients.size)
            } else {
                Result.failure(Exception("Sync failed with HTTP response code $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
