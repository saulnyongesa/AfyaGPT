package com.example.afyagpt.data.repository

import com.example.afyagpt.data.local.dao.FacilityDao
import com.example.afyagpt.data.local.entity.FacilityEntity
import com.example.afyagpt.util.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FacilityRepository.kt — Handles pre-login health facility fetching and pruning.
 */
@Singleton
class FacilityRepository @Inject constructor(
    private val facilityDao: FacilityDao
) {
    fun getFacilities(): Flow<List<FacilityEntity>> = facilityDao.getAllFacilities()

    suspend fun fetchAndCacheFacilities(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = URL(AppConstants.BACKEND_BASE_URL + "facilities/")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 8000
                readTimeout = 8000
            }
            if (conn.responseCode in 200..299) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }.trim()
                val jsonArray = if (responseText.startsWith("{")) {
                    val jsonObj = org.json.JSONObject(responseText)
                    jsonObj.optJSONArray("results") ?: JSONArray()
                } else if (responseText.startsWith("[")) {
                    JSONArray(responseText)
                } else {
                    JSONArray()
                }

                val list = mutableListOf<FacilityEntity>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val facilityName = obj.optString("name", "").trim()
                    if (facilityName.isNotBlank()) {
                        list.add(
                            FacilityEntity(
                                id = obj.optInt("id", i + 1),
                                name = facilityName,
                                county = obj.optString("county", "Nairobi").ifBlank { "Nairobi" },
                                subCounty = obj.optString("sub_county").takeIf { !it.isNull_or_blank() },
                                contactPhone = obj.optString("contact_phone").takeIf { !it.isNull_or_blank() }
                            )
                        )
                    }
                }
                if (list.isNotEmpty()) {
                    facilityDao.insertAll(list)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.isBlank() || this == "null"

    suspend fun pruneUnselectedFacilities(activeFacilityName: String) = withContext(Dispatchers.IO) {
        facilityDao.pruneOtherFacilities(activeFacilityName)
    }
}
