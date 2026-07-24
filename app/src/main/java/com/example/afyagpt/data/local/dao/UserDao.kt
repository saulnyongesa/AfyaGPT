/**
 * UserDao.kt
 *
 * Room Data Access Object (DAO) for all database operations involving the `users`
 * table. All functions are marked `suspend` to enforce coroutine-based (off-main-thread)
 * execution, preventing ANR errors from blocking the UI thread.
 *
 * Package: com.example.afyagpt.data.local.dao
 */
package com.example.afyagpt.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.afyagpt.data.local.entity.UserEntity

/**
 * DAO interface exposing CRUD and look-up operations for [UserEntity].
 *
 * Room generates the implementation at compile time. No SQL should be written
 * outside this interface for user-related queries.
 */
@Dao
interface UserDao {

    /**
     * Inserts a new user record into the database.
     *
     * Uses [OnConflictStrategy.ABORT] so that a duplicate phone number (unique index)
     * causes an [androidx.room.exception.SQLiteConstraintException], allowing the
     * repository layer to surface a meaningful error to the ViewModel.
     *
     * @param user The [UserEntity] to insert (id should be 0 to trigger auto-generation).
     * @return The row ID of the newly inserted record, or -1 on conflict.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    /**
     * Finds a user by their phone number.
     *
     * Used during login when the user enters their phone as the identifier.
     *
     * @param phone The phone number to search for (local format, e.g. "0712345678").
     * @return The matching [UserEntity], or null if not found.
     */
    @Query("SELECT * FROM users WHERE phone_number = :phone LIMIT 1")
    suspend fun findByPhone(phone: String): UserEntity?

    /**
     * Finds a user by their email address.
     *
     * @param email The email address to search for.
     * @return The matching [UserEntity], or null if not found.
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UserEntity?

    /**
     * Finds a user by either phone number or email address.
     *
     * This single query supports a unified login field where the user can type
     * either their registered phone or email without switching modes.
     *
     * @param identifier The phone number or email string entered by the user.
     * @return The first matching [UserEntity], or null if no match is found.
     */
    @Query(
        "SELECT * FROM users WHERE phone_number = :identifier OR email = :identifier LIMIT 1"
    )
    suspend fun findByPhoneOrEmail(identifier: String): UserEntity?

    /**
     * Retrieves a user by their auto-generated primary key.
     *
     * Used to reload the active user profile from DataStore's cached user ID.
     *
     * @param id The integer primary key of the user.
     * @return The matching [UserEntity], or null if the ID does not exist.
     */
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): UserEntity?

    /**
     * Replaces an existing user record with updated data.
     *
     * The entity's [UserEntity.id] must match an existing row; Room uses it as
     * the WHERE clause. Use this when updating profile fields.
     *
     * @param user The updated [UserEntity] (must include a valid id).
     */
    @Update
    suspend fun updateUser(user: UserEntity)

    /**
     * Updates the [UserEntity.lastLoginAt] timestamp for a specific user.
     *
     * Called immediately after a successful PIN verification to record the login time
     * without loading and re-saving the entire entity.
     *
     * @param userId    The primary key of the user to update.
     * @param timestamp An ISO 8601 datetime string representing the login time.
     */
    @Query("UPDATE users SET last_login_at = :timestamp WHERE id = :userId")
    suspend fun updateLastLogin(userId: Int, timestamp: String)

    /**
     * Updates the UI theme preference for a specific user.
     *
     * Keeping theme in the database (in addition to DataStore) allows it to be
     * restored correctly after app reinstallation or device migration.
     *
     * @param userId The primary key of the user.
     * @param theme  The new theme string ("BLUE_YELLOW", "DARK", or "LIGHT").
     */
    @Query("UPDATE users SET theme_preference = :theme WHERE id = :userId")
    suspend fun updateTheme(userId: Int, theme: String)

    /**
     * Checks whether a phone number is already registered.
     *
     * Used during registration to prevent duplicate accounts before attempting
     * an insert that would throw a constraint exception.
     *
     * @param phone The phone number to check.
     * @return `true` if at least one user with this phone number exists.
     */
    @Query("SELECT COUNT(*) > 0 FROM users WHERE phone_number = :phone")
    suspend fun phoneExists(phone: String): Boolean

    /**
     * Checks whether an email address is already registered.
     *
     * Only relevant when the email field is non-null; blank emails are allowed
     * multiple times since they represent users who skipped the optional field.
     *
     * @param email The email address to check (should be non-blank before calling).
     * @return `true` if at least one user with this email exists.
     */
    @Query("SELECT COUNT(*) > 0 FROM users WHERE email = :email")
    suspend fun emailExists(email: String): Boolean
}
