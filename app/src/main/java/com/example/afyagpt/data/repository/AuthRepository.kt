/**
 * AuthRepository.kt
 *
 * Repository that mediates between ViewModels and the local data sources
 * (Room database via [UserDao] and session state via [UserPreferences]).
 *
 * This class encapsulates all authentication business logic — registration,
 * login, logout, and session restoration — so that ViewModels remain thin and
 * contain only UI-state transformation logic.
 *
 * All public functions are `suspend` or return [Flow] so that callers are forced
 * to execute them off the main thread using coroutines.
 *
 * Package: com.example.afyagpt.data.repository
 */
package com.example.afyagpt.data.repository

import com.example.afyagpt.data.local.dao.UserDao
import com.example.afyagpt.data.local.entity.UserEntity
import com.example.afyagpt.data.preferences.UserPreferences
import com.example.afyagpt.domain.model.User
import com.example.afyagpt.util.DateTimeUtils
import com.example.afyagpt.util.PinHasher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

// ── Result wrapper ─────────────────────────────────────────────────────────────

/**
 * Sealed class representing the outcome of an authentication operation.
 *
 * Using a sealed class instead of exceptions means the ViewModel can pattern-match
 * on the result without requiring a try-catch in every calling site.
 *
 * @param T The type of the successful result payload (e.g. [User]).
 */
sealed class AuthResult<out T> {

    /**
     * The operation completed without errors.
     *
     * @property data The result payload.
     */
    data class Success<T>(val data: T) : AuthResult<T>()

    /**
     * The operation failed.
     *
     * @property message A user-presentable description of the failure.
     */
    data class Error<T>(val message: String) : AuthResult<T>()
}

// ── Repository ─────────────────────────────────────────────────────────────────

/**
 * Repository for all authentication and session management operations.
 *
 * @param userDao     Room DAO for user database queries.
 * @param preferences DataStore-backed preferences for session state.
 */
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val preferences: UserPreferences
) {

    /**
     * Registers a new user, persists their record, and creates an active session.
     *
     * Steps:
     * 1. Check that the phone number is not already registered.
     * 2. If email is provided, check it is not already registered.
     * 3. Hash the PIN using [PinHasher] (salt + SHA-256).
     * 4. Build and insert the [UserEntity] into Room.
     * 5. Save the session to DataStore so the user is immediately logged in.
     * 6. Return [AuthResult.Success] with the domain [User].
     *
     * @param fullName           The user's full name.
     * @param email              Optional email address (blank is permitted).
     * @param phone              Kenyan mobile number (07xx / 01xx format).
     * @param profession         Profession constant string (matches [Profession] enum name).
     * @param professionalNumber Optional regulatory licence number.
     * @param facilityName       Name of the health facility.
     * @param county             Kenyan county.
     * @param pin                Plain-text 6-digit PIN (hashed immediately; not stored).
     * @return [AuthResult.Success] with the created [User], or [AuthResult.Error].
     */
    suspend fun registerUser(
        fullName: String,
        email: String,
        phone: String,
        profession: String,
        professionalNumber: String,
        facilityName: String,
        county: String,
        pin: String
    ): AuthResult<User> {
        // Guard: phone number must be unique.
        if (userDao.phoneExists(phone)) {
            return AuthResult.Error("A user with this phone number is already registered.")
        }

        // Guard: if email was provided, it must also be unique.
        if (email.isNotBlank() && userDao.emailExists(email)) {
            return AuthResult.Error("A user with this email address is already registered.")
        }

        return try {
            // Hash the PIN before any persistence — the plain-text PIN is discarded here.
            val pinHash = PinHasher.hashPin(pin)

            // Build the entity. Auto-generated id starts at 0; Room will replace it.
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

            // Insert the entity; Room returns the auto-generated row ID.
            val newId = userDao.insertUser(entity).toInt()

            // Reload the entity from the database to obtain the assigned ID.
            val savedEntity = userDao.findById(newId)
                ?: return AuthResult.Error("Registration succeeded but user could not be reloaded.")

            val user = User.fromEntity(savedEntity)

            // Persist the session so the user lands on the home screen immediately.
            preferences.saveSession(userId = user.id, theme = user.themePreference)

            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error("Registration failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    /**
     * Authenticates a user by their phone or email and PIN.
     *
     * Steps:
     * 1. Find the user by the provided identifier (phone or email).
     * 2. Verify the provided PIN against the stored hash using [PinHasher].
     * 3. Update [UserEntity.lastLoginAt] to the current timestamp.
     * 4. Save the session to DataStore.
     * 5. Return [AuthResult.Success] with the domain [User].
     *
     * @param identifier The phone number or email address entered by the user.
     * @param pin        The plain-text 6-digit PIN entered by the user.
     * @return [AuthResult.Success] with the authenticated [User], or [AuthResult.Error].
     */
    suspend fun loginUser(identifier: String, pin: String): AuthResult<User> {
        // Look up the user by phone or email in a single query.
        val entity = userDao.findByPhoneOrEmail(identifier)
            ?: return AuthResult.Error("No account found for this phone number or email.")

        // Reject login for soft-deleted (deactivated) accounts.
        if (!entity.isActive) {
            return AuthResult.Error("This account has been deactivated. Contact support.")
        }

        // Verify the PIN against the stored salted hash.
        if (!PinHasher.verifyPin(pin, entity.pinHash)) {
            return AuthResult.Error("Incorrect PIN. Please try again.")
        }

        return try {
            val loginTime = DateTimeUtils.now()

            // Record the login timestamp without rewriting the entire row.
            userDao.updateLastLogin(userId = entity.id, timestamp = loginTime)

            // Create the updated domain user with the new timestamp for the session.
            val user = User.fromEntity(entity.copy(lastLoginAt = loginTime))

            // Persist the session.
            preferences.saveSession(userId = user.id, theme = user.themePreference)

            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error("Login failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    /**
     * Clears the active session, effectively logging the user out.
     *
     * The user's data remains in the Room database for subsequent logins.
     * Only the DataStore session keys are reset.
     */
    suspend fun logout() {
        preferences.clearSession()
    }

    /**
     * Retrieves the currently authenticated user from the database.
     *
     * Reads the active user ID from DataStore, then performs a single database
     * look-up. Returns null if no session is active (user ID is 0) or if the
     * stored ID no longer matches a user record.
     *
     * @return The active [User], or null if unauthenticated.
     */
    suspend fun getCurrentUser(): User? {
        val userId = preferences.getActiveUserId().first()
        if (userId == 0) return null
        val entity = userDao.findById(userId) ?: return null
        return User.fromEntity(entity)
    }

    /**
     * Returns a [Flow] that emits the current login state.
     *
     * The AuthViewModel collects this to reactively navigate between the login
     * and home destinations whenever the session state changes.
     *
     * @return A [Flow] emitting `true` while a session is active.
     */
    fun isLoggedIn(): Flow<Boolean> = preferences.isLoggedIn()
}
