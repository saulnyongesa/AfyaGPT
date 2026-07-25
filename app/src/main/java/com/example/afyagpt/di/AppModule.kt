package com.example.afyagpt.di

import android.content.Context
import com.example.afyagpt.data.local.dao.PatientDao
import com.example.afyagpt.data.local.dao.UserDao
import com.example.afyagpt.data.local.dao.VaccinationDao
import com.example.afyagpt.data.preferences.UserPreferences
import com.example.afyagpt.data.repository.AuthRepository
import com.example.afyagpt.data.repository.PatientRepository
import com.example.afyagpt.data.repository.SyncRepository
import com.example.afyagpt.data.repository.VaccinationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module installed in [SingletonComponent] that provides cross-cutting
 * application services: DataStore preferences, auth, patient, and vaccination repositories.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * Provides the [UserPreferences] singleton backed by Jetpack DataStore.
     */
    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences {
        return UserPreferences(context)
    }

    /**
     * Provides the [AuthRepository] singleton for all authentication operations.
     */
    @Provides
    @Singleton
    fun provideAuthRepository(
        userDao: UserDao,
        preferences: UserPreferences,
        syncRepository: SyncRepository,
        connectivityChecker: com.example.afyagpt.domain.suggestion.ConnectivityChecker,
        facilityRepository: com.example.afyagpt.data.repository.FacilityRepository
    ): AuthRepository {
        return AuthRepository(userDao, preferences, syncRepository, connectivityChecker, facilityRepository)
    }

    /**
     * Provides the [PatientRepository] singleton for patient CRUD operations.
     */
    @Provides
    @Singleton
    fun providePatientRepository(
        patientDao: PatientDao,
        vaccinationRepository: VaccinationRepository,
        preferences: UserPreferences
    ): PatientRepository {
        return PatientRepository(patientDao, vaccinationRepository, preferences)
    }

    /**
     * Provides the [VaccinationRepository] singleton for immunization tracking.
     */
    @Provides
    @Singleton
    fun provideVaccinationRepository(
        vaccinationDao: VaccinationDao
    ): VaccinationRepository {
        return VaccinationRepository(vaccinationDao)
    }
}
