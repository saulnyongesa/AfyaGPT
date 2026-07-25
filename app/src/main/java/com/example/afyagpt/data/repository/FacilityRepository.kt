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
                connectTimeout = 6000
                readTimeout = 6000
            }
            if (conn.responseCode in 200..299) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(responseText)
                val list = mutableListOf<FacilityEntity>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        FacilityEntity(
                            id = obj.optInt("id", i + 1),
                            name = obj.optString("name", "Facility"),
                            county = obj.optString("county", "Nairobi"),
                            subCounty = obj.optString("sub_county").takeIf { it.isNotBlank() },
                            contactPhone = obj.optString("contact_phone").takeIf { it.isNotBlank() }
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    facilityDao.insertAll(list)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun pruneUnselectedFacilities(activeFacilityName: String) = withContext(Dispatchers.IO) {
        facilityDao.pruneOtherFacilities(activeFacilityName)
    }
}
