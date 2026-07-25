/**
 * AfyaGPTDatabase.kt
 *
 * Room database configuration and singleton accessor for the AfyaGPT application.
 *
 * This class declares all entities and DAOs that belong to the local SQLite database.
 * A single database instance is shared across the entire application lifetime to
 * avoid the overhead of repeated connection setup and to prevent concurrent-write
 * issues that arise with multiple instances.
 *
 * Migration strategy (current):
 * - [fallbackToDestructiveMigration] is enabled during early development. This
 *   means that if the schema version is bumped without providing a proper
 *   [androidx.room.migration.Migration] object, Room will drop and recreate all
 *   tables, deleting any existing data.
 *
 * Migration strategy (production):
 * - Replace [fallbackToDestructiveMigration] with explicit Migration objects
 *   (e.g., `addMigrations(MIGRATION_1_2, MIGRATION_2_3)`) before releasing to
 *   end users so that field worker data is never lost on an app update.
 *
 * Package: com.example.afyagpt.data.local
 */
package com.example.afyagpt.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.afyagpt.data.local.dao.UserDao
import com.example.afyagpt.data.local.entity.UserEntity

import com.example.afyagpt.data.local.dao.ChatMessageDao
import com.example.afyagpt.data.local.dao.PatientDao
import com.example.afyagpt.data.local.dao.TriageDao
import com.example.afyagpt.data.local.dao.VaccinationDao
import com.example.afyagpt.data.local.entity.ChatMessageEntity
import com.example.afyagpt.data.local.entity.PatientEntity
import com.example.afyagpt.data.local.entity.TriageSessionEntity
import com.example.afyagpt.data.local.entity.VaccinationEntity

/**
 * Room database class for AfyaGPT.
 *
 * Lists all entity classes ([entities]) that belong to this database. Each entity
 * maps to a table in SQLite. Incrementing [version] without providing a migration
 * will trigger destructive migration while [fallbackToDestructiveMigration] is set.
 *
 * [exportSchema] is set to `true` so Room generates a JSON schema file in the
 * `schemas/` directory at build time. Commit these files to version control to
 * review schema changes in pull requests and to write migration tests.
 */
@Database(
    entities = [
        UserEntity::class,
        PatientEntity::class,
        TriageSessionEntity::class,
        VaccinationEntity::class,
        ChatMessageEntity::class
    ],
    version = 5,
    exportSchema = true
)
abstract class AfyaGPTDatabase : RoomDatabase() {

    /**
     * Provides access to the [UserDao] for all user-related database operations.
     *
     * Room generates the concrete implementation of this method at compile time.
     */
    abstract fun userDao(): UserDao

    abstract fun patientDao(): PatientDao
    abstract fun triageDao(): TriageDao
    abstract fun vaccinationDao(): VaccinationDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {

        /**
         * The on-disk filename for the SQLite database file.
         * Changing this name in production would create a new empty database and
         * require a manual migration of the old file.
         */
        private const val DATABASE_NAME = "afyagpt_db"

        /**
         * Volatile ensures that the value of [INSTANCE] is always read from and
         * written to main memory rather than a CPU cache, guaranteeing visibility
         * across all threads without the need for explicit synchronisation on reads.
         */
        @Volatile
        private var INSTANCE: AfyaGPTDatabase? = null

        /**
         * Returns the application-wide singleton [AfyaGPTDatabase] instance.
         *
         * Thread safety is achieved with the classic double-checked locking pattern:
         * 1. First check (outside synchronized): avoids acquiring the lock on every
         *    call once the instance has been created.
         * 2. Synchronized block + second check: ensures only one thread initialises
         *    the instance when multiple threads call [getInstance] concurrently for
         *    the first time.
         *
         * @param context Any [Context]; the application context is extracted internally
         *                so there is no risk of leaking an Activity or Fragment.
         * @return The singleton [AfyaGPTDatabase] instance.
         */
        fun getInstance(context: Context): AfyaGPTDatabase {
            // First check — no locking overhead after initialisation.
            return INSTANCE ?: synchronized(this) {
                // Second check — necessary because another thread may have initialised
                // the instance between our first check and acquiring the lock.
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        /**
         * Builds the [AfyaGPTDatabase] using Room's builder API.
         *
         * Separated from [getInstance] so the creation logic can be tested in isolation.
         *
         * @param appContext The application context (not an Activity context).
         * @return A fully configured [AfyaGPTDatabase] instance.
         */
        private fun buildDatabase(appContext: Context): AfyaGPTDatabase {
            return Room.databaseBuilder(
                appContext,
                AfyaGPTDatabase::class.java,
                DATABASE_NAME
            )
                // TODO: Replace with explicit Migration objects before production release.
                // Example: .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }
    }
}
