/**
 * DatabaseModule.kt
 *
 * Hilt dependency-injection module responsible for providing Room database and
 * DAO instances as application-scoped singletons.
 *
 * Using Hilt for these bindings means:
 * - The same [AfyaGPTDatabase] instance is shared across all injection sites.
 * - Tests can replace this module with a fake/in-memory module without touching
 *   production code.
 * - No manual lifecycle management is needed — Hilt destroys the component only
 *   when the process terminates.
 *
 * Package: com.example.afyagpt.di
 */
package com.example.afyagpt.di

import android.content.Context
import com.example.afyagpt.data.local.AfyaGPTDatabase
import com.example.afyagpt.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module installed in [SingletonComponent] so all provided dependencies
 * live for the full duration of the application process.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the application's [AfyaGPTDatabase] singleton instance.
     *
     * Hilt calls this method once and caches the result; subsequent injection
     * points receive the same instance. The [ApplicationContext] qualifier
     * ensures that Room holds a reference to the application context rather
     * than an Activity context, preventing memory leaks.
     *
     * @param context The application context provided by Hilt automatically.
     * @return The singleton [AfyaGPTDatabase] instance.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AfyaGPTDatabase {
        return AfyaGPTDatabase.getInstance(context)
    }

    /**
     * Provides the [UserDao] obtained from the Room database.
     *
     * Room generates a concrete implementation of [UserDao] inside [AfyaGPTDatabase];
     * this function simply delegates to [AfyaGPTDatabase.userDao] so Hilt can inject
     * it wherever a [UserDao] is declared as a dependency.
     *
     * @param db The singleton [AfyaGPTDatabase] provided by [provideDatabase].
     * @return The Room-generated [UserDao] implementation.
     */
    @Provides
    @Singleton
    fun provideUserDao(db: AfyaGPTDatabase): UserDao {
        return db.userDao()
    }

    /**
     * Provides the [PatientDao].
     */
    @Provides
    @Singleton
    fun providePatientDao(db: AfyaGPTDatabase): com.example.afyagpt.data.local.dao.PatientDao {
        return db.patientDao()
    }

    /**
     * Provides the [TriageDao].
     */
    @Provides
    @Singleton
    fun provideTriageDao(db: AfyaGPTDatabase): com.example.afyagpt.data.local.dao.TriageDao {
        return db.triageDao()
    }

    /**
     * Provides the [VaccinationDao].
     */
    @Provides
    @Singleton
    fun provideVaccinationDao(db: AfyaGPTDatabase): com.example.afyagpt.data.local.dao.VaccinationDao {
        return db.vaccinationDao()
    }

    @Provides
    @Singleton
    fun provideChatMessageDao(db: AfyaGPTDatabase): com.example.afyagpt.data.local.dao.ChatMessageDao {
        return db.chatMessageDao()
    }
}
