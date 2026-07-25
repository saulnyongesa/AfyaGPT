package com.example.afyagpt.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * FacilityEntity.kt — Room Entity for cached Health Facilities.
 * Used during pre-login registration so health workers can choose their facility.
 */
@Entity(tableName = "facilities")
data class FacilityEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "county") val county: String,
    @ColumnInfo(name = "sub_county") val subCounty: String? = null,
    @ColumnInfo(name = "contact_phone") val contactPhone: String? = null
)
