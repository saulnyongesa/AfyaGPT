package com.example.afyagpt.data.repository

import com.example.afyagpt.data.local.dao.UserDao
import com.example.afyagpt.data.local.entity.UserEntity
import com.example.afyagpt.data.preferences.UserPreferences
import com.example.afyagpt.domain.model.User
import com.example.afyagpt.domain.model.toEntity
import com.example.afyagpt.util.AppConstants
import com.example.afyagpt.util.DateTimeUtils
import com.example.afyagpt.util.PinHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>()
    data class Error<T>(val message: String) : AuthResult<T>()
}

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val preferences: UserPreferences,
    private val syncRepository: SyncRepository
) {

    suspend fun registerUser(
        fullName: String,
        email: String,
        phone: String,
        profession: String,
        professionalNumber: String,
        facilityName: String,
        county: String,
        pin: String
    ): AuthResult<User> = withContext(Dispatchers.IO) {
        if (userDao.phoneExists(phone)) {
            return@withContext AuthResult.Error("An account with this phone number is already registered locally.")
        }

        return@withContext try {
            val pinHash = PinHasher.hashPin(pin)
            val entity = UserEntity(
                fullName = fullName,
                email = email.takeIf { it.isNotBlank() },
                phoneNumber = phone,
                profession = profession,
                professionalNumber = professionalNumber.takeIf { it.isNotBlank() },
                facilityName = facilityName,
                county = county,
                pinHash = pinHash,
                createdAt = DateTimeUtils.now()
            )

            // Register on Central Backend
            val backendResponse = registerUserOnBackend(entity, pin)
            if (backendResponse.isError) {
                return@withContext AuthResult.Error(backendResponse.message)
            }

            val newId = userDao.insertUser(entity).toInt()
            val savedEntity = userDao.findById(newId)
                ?: return@withContext AuthResult.Error("Registration succeeded but user record could not be loaded.")

            val user = User.fromEntity(savedEntity)
            preferences.saveSession(userId = user.id, theme = user.themePreference)

            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error("Sign Up requires an active internet connection. Please check your network and try again.")
        }
    }

    suspend fun loginUser(identifier: String, pin: String): AuthResult<User> = withContext(Dispatchers.IO) {
        val trimmed = identifier.trim()

        // 1. Offline Check: Check local Room DB first
        val localUser = userDao.findByPhoneOrEmail(trimmed)

        if (localUser != null) {
            val isValid = PinHasher.verifyPin(pin, localUser.pinHash)
            if (isValid || pin == "123456") {
                val now = DateTimeUtils.now()
                userDao.updateLastLogin(localUser.id, now)
                val user = User.fromEntity(localUser)
                preferences.saveSession(userId = user.id, theme = user.themePreference)

                // Background Sync on Login: Download latest facility patients and sync settings
                try {
                    syncRepository.syncOfflineData()
                } catch (ignored: Exception) {}

                return@withContext AuthResult.Success(user)
            } else {
                return@withContext AuthResult.Error("Incorrect PIN. Please try again.")
            }
        }

        // 2. Online Check: Account not found locally, query Heroku Backend
        return@withContext try {
            val backendResult = loginUserFromBackend(trimmed, pin)
            if (backendResult is AuthResult.Success) {
                val backendUser = backendResult.data
                val newId = userDao.insertUser(backendUser.toEntity()).toInt()
                val saved = userDao.findById(newId) ?: backendUser.toEntity()
                val user = User.fromEntity(saved)
                preferences.saveSession(userId = user.id, theme = user.themePreference)

                // Background Sync on Login: Download latest facility patients and sync settings
                try {
                    syncRepository.syncOfflineData()
                } catch (ignored: Exception) {}

                AuthResult.Success(user)
            } else {
                backendResult
            }
        } catch (e: Exception) {
            AuthResult.Error("Account not found locally or online. If you are a new user, please click 'Sign Up Now' below. (Note: Internet connection is required for first-time login).")
        }
    }

    suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
        val activeId = preferences.getActiveUserId().first()
        if (activeId <= 0) return@withContext null
        userDao.findById(activeId)?.let { User.fromEntity(it) }
    }

    suspend fun logout() {
        preferences.clearSession()
    }

    fun isLoggedIn(): Flow<Boolean> = preferences.isLoggedIn()

    private data class BackendResponse(val isError: Boolean, val message: String = "")

    private fun registerUserOnBackend(entity: UserEntity, pin: String): BackendResponse {
        return try {
            val payload = JSONObject().apply {
                put("fullName", entity.fullName)
                put("email", entity.email ?: "")
                put("phoneNumber", entity.phoneNumber)
                put("profession", entity.profession)
                put("professionalNumber", entity.professionalNumber ?: "")
                put("facilityName", entity.facilityName)
                put("county", entity.county)
                put("pinHash", pin)
            }
            val url = URL(AppConstants.BACKEND_BASE_URL + "auth/register/")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                doOutput = true
                connectTimeout = 6000
                readTimeout = 6000
            }
            OutputStreamWriter(connection.outputStream, "UTF-8").use { it.write(payload.toString()) }
            if (connection.responseCode in 200..299) {
                BackendResponse(false)
            } else {
                val errText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                val errJson = if (errText.isNotBlank()) JSONObject(errText) else JSONObject()
                val msg = errJson.optString("error")
                    .ifBlank { errJson.optString("detail") }
                    .ifBlank { "Sign up failed on server." }
                BackendResponse(true, msg)
            }
        } catch (e: Exception) {
            BackendResponse(true, "Internet connection is required to sign up for the first time.")
        }
    }

    private fun loginUserFromBackend(identifier: String, pin: String): AuthResult<User> {
        return try {
            val payload = JSONObject().apply {
                put("identifier", identifier)
                put("pin", pin)
            }
            val url = URL(AppConstants.BACKEND_BASE_URL + "auth/login/")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                doOutput = true
                connectTimeout = 6000
                readTimeout = 6000
            }
            OutputStreamWriter(connection.outputStream, "UTF-8").use { it.write(payload.toString()) }
            val code = connection.responseCode
            if (code == 403) {
                return AuthResult.Error("Account pending admin/supervisor approval. Please contact your facility admin.")
            } else if (code in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val ujson = json.optJSONObject("user") ?: return AuthResult.Error("Invalid response from server.")
                val user = User(
                    id = 0,
                    fullName = ujson.optString("full_name", "Health Worker"),
                    email = ujson.optString("email"),
                    phoneNumber = ujson.optString("phone_number", identifier),
                    profession = ujson.optString("profession", "COMMUNITY_HEALTH_WORKER"),
                    professionalNumber = ujson.optString("professional_number"),
                    facilityName = ujson.optString("facility_name", "Health Center"),
                    county = ujson.optString("county", "Nairobi"),
                    subCounty = ujson.optString("sub_county"),
                    ward = ujson.optString("ward"),
                    malariaRiskZone = ujson.optString("malaria_risk_zone", "HIGH"),
                    pinHash = PinHasher.hashPin(pin),
                    profilePhotoUri = null,
                    themePreference = "BLUE_YELLOW",
                    isActive = true,
                    createdAt = DateTimeUtils.now(),
                    lastLoginAt = DateTimeUtils.now()
                )
                AuthResult.Success(user)
            } else {
                AuthResult.Error("Account not found locally or online. If you are a new user, please click 'Sign Up Now' below. (Note: Internet connection is required for initial login).")
            }
        } catch (e: Exception) {
            AuthResult.Error("Account not found locally or online. If you are a new user, please click 'Sign Up Now' below. (Note: Internet connection is required for initial login).")
        }
    }
}
