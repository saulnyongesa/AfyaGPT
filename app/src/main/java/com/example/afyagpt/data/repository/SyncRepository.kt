package com.example.afyagpt.data.repository

import com.example.afyagpt.data.local.dao.PatientDao
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
 * SyncRepository.kt
 * Manages manual and background synchronization of offline patient data
 * to the live Heroku Django REST Framework backend server.
 */
@Singleton
class SyncRepository @Inject constructor(
    private val patientDao: PatientDao,
    private val userPreferences: UserPreferences
) {
    /**
     * Performs a manual sync of all local Room DB patient records to the Heroku Django API.
     */
    suspend fun syncOfflineData(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val patients = patientDao.getAllPatients().first()
            if (patients.isEmpty()) {
                userPreferences.updateSyncStatus(DateTimeUtils.now(), 0)
                return@withContext Result.success(0)
            }

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
