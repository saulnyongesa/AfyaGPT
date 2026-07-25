package com.example.afyagpt.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.afyagpt.data.local.entity.FacilityEntity
import kotlinx.coroutines.flow.Flow

/**
 * FacilityDao.kt — Room DAO for Health Facility operations.
 */
@Dao
interface FacilityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(facilities: List<FacilityEntity>)

    @Query("SELECT * FROM facilities ORDER BY name ASC")
    fun getAllFacilities(): Flow<List<FacilityEntity>>

    @Query("SELECT * FROM facilities ORDER BY name ASC")
    suspend fun getAllFacilitiesSync(): List<FacilityEntity>

    /**
     * Requirement 3: Upon successful user login, delete all cached facilities from Room DB
     * except the single active facility the logged-in user works under.
     */
    @Query("DELETE FROM facilities WHERE name != :activeFacilityName")
    suspend fun pruneOtherFacilities(activeFacilityName: String)

    @Query("DELETE FROM facilities")
    suspend fun deleteAll()
}
