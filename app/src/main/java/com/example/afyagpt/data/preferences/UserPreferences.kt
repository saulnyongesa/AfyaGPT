/**
 * UserPreferences.kt
 *
 * Jetpack DataStore-backed preferences store for session state and lightweight
 * app settings that do not belong in the Room database.
 *
 * DataStore<Preferences> is used here (as opposed to Proto DataStore) because
 * the data is simple key-value pairs and does not require a protobuf schema.
 *
 * DataStore advantages over SharedPreferences:
 * - Fully coroutine- and Flow-based (no blocking calls on the main thread).
 * - Atomic and transactional writes (no data corruption on crash).
 * - Typed keys prevent accidental key-name typos.
 *
 * Package: com.example.afyagpt.data.preferences
 */
package com.example.afyagpt.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// ── DataStore instance ─────────────────────────────────────────────────────────

/**
 * Top-level property delegate that creates (or returns the existing) DataStore
 * instance scoped to the application [Context]. The file on disk will be named
 * "afya_gpt_preferences.preferences_pb".
 *
 * Using a property delegate guarantees a single DataStore instance per process,
 * which is required to avoid DataStore's multi-instance assertion error.
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "afya_gpt_preferences"
)

/**
 * Singleton class that wraps the application's [DataStore] and exposes typed
 * read ([Flow]) and write ([suspend]) functions for each preference key.
 *
 * Injected via Hilt wherever session state or settings are needed (repositories,
 * ViewModels indirectly through repositories).
 *
 * @param context Application context; used to access the DataStore instance.
 */
@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // ── Preference keys ────────────────────────────────────────────────────────

    /**
     * Typed preference key declarations. Using the dedicated factory functions
     * (e.g. [intPreferencesKey]) ensures type safety at compile time.
     */
    private object Keys {
        /** ID of the currently logged-in user. 0 means no active session. */
        val ACTIVE_USER_ID = intPreferencesKey("active_user_id")

        /** Whether a user session is currently active. */
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")

        /** The active UI theme string (matches [AppTheme] enum name). */
        val ACTIVE_THEME = stringPreferencesKey("active_theme")

        /** ISO 8601 datetime of the last successful data sync with the server. */
        val LAST_SYNC_TIME = stringPreferencesKey("last_sync_time")

        /** Number of local records awaiting upload to the server. */
        val UNSYNCED_COUNT = intPreferencesKey("unsynced_count")

        /** App language preference ("ENGLISH" or "KISWAHILI"). */
        val ACTIVE_LANGUAGE = stringPreferencesKey("active_language")

        /** Cached WHO IMCI rules JSON string downloaded from server. */
        val WHO_RULES_JSON = stringPreferencesKey("who_rules_json")
    }

    fun getActiveLanguage(): Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.ACTIVE_LANGUAGE] ?: "ENGLISH"
    }

    suspend fun updateLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_LANGUAGE] = language
        }
    }

    fun getWhoRulesJson(): Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.WHO_RULES_JSON] ?: ""
    }

    suspend fun updateWhoRules(rulesJson: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WHO_RULES_JSON] = rulesJson
        }
    }

    // ── Write operations ───────────────────────────────────────────────────────

    /**
     * Saves a new login session by persisting the user's ID and preferred theme.
     * Also sets [Keys.IS_LOGGED_IN] to true.
     *
     * Called immediately after a successful PIN verification or registration.
     *
     * @param userId The primary key of the authenticated user.
     * @param theme  The user's theme preference string (e.g. "BLUE_YELLOW").
     */
    suspend fun saveSession(userId: Int, theme: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_USER_ID] = userId
            prefs[Keys.IS_LOGGED_IN] = true
            prefs[Keys.ACTIVE_THEME] = theme
        }
    }

    /**
     * Clears all session-related preferences, effectively logging the user out.
     *
     * Resets [Keys.ACTIVE_USER_ID] to 0 and [Keys.IS_LOGGED_IN] to false.
     * Theme is intentionally not cleared so the login screen respects the user's
     * last known preference.
     */
    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_USER_ID] = 0
            prefs[Keys.IS_LOGGED_IN] = false
            // Note: theme is preserved so the login screen maintains the last theme.
        }
    }

    /**
     * Updates the active UI theme preference without changing any other session data.
     *
     * @param theme The new theme string.
     */
    suspend fun updateTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_THEME] = theme
        }
    }

    /**
     * Persists the results of a background data sync operation.
     *
     * @param lastSync       ISO 8601 datetime of when the sync completed.
     * @param unsyncedCount  Number of records still pending upload after the sync.
     */
    suspend fun updateSyncStatus(lastSync: String, unsyncedCount: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_SYNC_TIME] = lastSync
            prefs[Keys.UNSYNCED_COUNT] = unsyncedCount
        }
    }

    // ── Read operations (Flows) ────────────────────────────────────────────────

    /**
     * Emits the ID of the currently active user.
     *
     * 0 indicates no active session (unauthenticated state).
     * Collect this in the repository to reload the user profile on app restart.
     *
     * @return A [Flow] emitting the stored user ID (default 0).
     */
    fun getActiveUserId(): Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.ACTIVE_USER_ID] ?: 0
    }

    /**
     * Emits whether a login session is currently active.
     *
     * The AuthViewModel observes this to decide whether to navigate to the home
     * screen or the login screen on app launch.
     *
     * @return A [Flow] emitting `true` if the user is logged in, `false` otherwise.
     */
    fun isLoggedIn(): Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_LOGGED_IN] ?: false
    }

    /**
     * Emits the active UI theme string.
     *
     * @return A [Flow] emitting the stored theme (default "BLUE_YELLOW").
     */
    fun getActiveTheme(): Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.ACTIVE_THEME] ?: "BLUE_YELLOW"
    }

    /**
     * Emits a [Pair] of the last sync time and the count of unsynced records.
     *
     * The sync status badge in the UI observes this to indicate data freshness.
     *
     * @return A [Flow] emitting (lastSyncTime: String, unsyncedCount: Int).
     *         Empty string and 0 are the defaults when no sync has occurred.
     */
    fun getSyncStatus(): Flow<Pair<String, Int>> = context.dataStore.data.map { prefs ->
        val lastSync = prefs[Keys.LAST_SYNC_TIME] ?: ""
        val count = prefs[Keys.UNSYNCED_COUNT] ?: 0
        Pair(lastSync, count)
    }
}
