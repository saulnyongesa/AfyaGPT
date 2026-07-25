package com.example.afyagpt.data.repository

import com.example.afyagpt.util.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data model for remote announcements / directives.
 */
data class RemoteAnnouncement(
    val id: Int,
    val title: String,
    val message: String,
    val priority: String,
    val targetCounty: String,
    val createdAt: String
)

/**
 * AnnouncementRepository.kt
 * Fetches Ministry & Facility directives from the central backend when internet is available.
 */
@Singleton
class AnnouncementRepository @Inject constructor() {

    suspend fun fetchAnnouncements(): List<RemoteAnnouncement> = withContext(Dispatchers.IO) {
        try {
            val url = URL(AppConstants.BACKEND_BASE_URL + "announcements/")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                connectTimeout = 5000
                readTimeout = 5000
            }

            if (connection.responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(responseText)
                val list = mutableListOf<RemoteAnnouncement>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        RemoteAnnouncement(
                            id = obj.optInt("id"),
                            title = obj.optString("title", "Notice"),
                            message = obj.optString("message", ""),
                            priority = obj.optString("priority", "INFO"),
                            targetCounty = obj.optString("target_county", "ALL"),
                            createdAt = obj.optString("created_at", "")
                        )
                    )
                }
                return@withContext list
            }
        } catch (ignored: Exception) { }
        emptyList()
    }
}
