/**
 * UserEntity.kt
 *
 * Room database entity representing an AfyaGPT app user — a community health
 * worker (CHW), nurse, clinical officer, or any other registered health professional.
 *
 * Each user record stores authentication credentials (PIN hash), profile details,
 * facility/location metadata, and UI preferences. Sensitive data (PIN) is always
 * stored hashed; the plain-text PIN is never persisted.
 *
 * Table name: "users"
 *
 * Package: com.example.afyagpt.data.local.entity
 */
package com.example.afyagpt.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity class for the `users` table.
 *
 * A unique index is enforced on [phoneNumber] because phone number acts as the
 * primary login identifier. [email] is optional but indexed for faster look-ups
 * when a user logs in with their email address.
 *
 * @property id                 Auto-generated primary key. Zero before insertion.
 * @property fullName           The user's full name as entered during registration.
 * @property email              Optional email address used as an alternative login ID.
 * @property phoneNumber        Kenyan mobile number in local format (07xx/01xx).
 *                              Must be unique across all users.
 * @property profession         Free-text profession label (e.g. "NURSE").
 * @property professionalNumber Optional regulatory/licence number (e.g. NCK licence).
 * @property facilityName       Name of the health facility the user is attached to.
 * @property county             Kenyan administrative county.
 * @property subCounty          Sub-county (optional).
 * @property ward               Ward (optional).
 * @property malariaRiskZone    Malaria transmission zone: "HIGH", "LOW", or "NONE".
 *                              Defaults to "HIGH" as most Kenyan CHW zones are high-risk.
 * @property pinHash            SHA-256 hash of the user's PIN, stored as "salt:hash".
 *                              Never store or log the plain-text PIN.
 * @property profilePhotoUri    URI string pointing to the user's profile photo on device
 *                              storage; null if no photo has been set.
 * @property themePreference    UI theme identifier. One of "BLUE_YELLOW", "DARK", "LIGHT".
 * @property isActive           Soft-delete flag. Inactive users cannot log in.
 * @property createdAt          ISO 8601 datetime when the account was created.
 * @property lastLoginAt        ISO 8601 datetime of the user's most recent login; null
 *                              until the first login after registration.
 */
@Entity(
    tableName = "users",
    indices = [
        // Enforce uniqueness and speed up phone-based look-ups.
        Index(value = ["phone_number"], unique = true),
        // Speed up email-based look-ups (email is optional, so not unique).
        Index(value = ["email"])
    ]
)
data class UserEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    /** The user's full name (minimum 3 characters, letters and spaces only). */
    @ColumnInfo(name = "full_name")
    val fullName: String,

    /** Optional email address; null if the user chose not to provide one. */
    @ColumnInfo(name = "email")
    val email: String? = null,

    /** Primary login identifier. Stored in local Kenyan format (07xx / 01xx). */
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,

    /** The user's health profession as a string constant (matches [Profession] enum names). */
    @ColumnInfo(name = "profession")
    val profession: String,

    /** Optional regulatory body licence or registration number. */
    @ColumnInfo(name = "professional_number")
    val professionalNumber: String? = null,

    /** Name of the primary health facility (dispensary, health centre, hospital). */
    @ColumnInfo(name = "facility_name")
    val facilityName: String,

    /** Kenyan county where the user works (e.g. "Kisumu", "Nairobi"). */
    @ColumnInfo(name = "county")
    val county: String,

    /** Sub-county; optional because some users may not know or need this level of detail. */
    @ColumnInfo(name = "sub_county")
    val subCounty: String? = null,

    /** Ward; optional granularity for community health unit assignment. */
    @ColumnInfo(name = "ward")
    val ward: String? = null,

    /**
     * Malaria risk classification for the user's operational area.
     * Drives which malaria-specific clinical decision-support prompts are shown.
     * Values: "HIGH", "LOW", "NONE".
     */
    @ColumnInfo(name = "malaria_risk_zone")
    val malariaRiskZone: String = "HIGH",

    /**
     * Salted SHA-256 hash of the user's 6-digit PIN.
     * Format: "Base64(salt):Base64(sha256(salt+pin))".
     * See [com.example.afyagpt.util.PinHasher] for implementation.
     */
    @ColumnInfo(name = "pin_hash")
    val pinHash: String,

    /** Absolute URI of the user's profile photo stored locally; null if not set. */
    @ColumnInfo(name = "profile_photo_uri")
    val profilePhotoUri: String? = null,

    /**
     * The user's preferred UI colour theme.
     * Possible values: "BLUE_YELLOW" (default brand), "DARK", "LIGHT".
     */
    @ColumnInfo(name = "theme_preference")
    val themePreference: String = "BLUE_YELLOW",

    /**
     * Whether this account is active. Set to false to effectively deactivate a user
     * without deleting their data (soft delete).
     */
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    /** ISO 8601 datetime when this record was first inserted into the database. */
    @ColumnInfo(name = "created_at")
    val createdAt: String,

    /** ISO 8601 datetime of the most recent successful login; null before first login. */
    @ColumnInfo(name = "last_login_at")
    val lastLoginAt: String? = null
)
