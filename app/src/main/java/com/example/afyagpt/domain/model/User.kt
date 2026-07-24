/**
 * User.kt
 *
 * Clean domain model representing an AfyaGPT user. Unlike [UserEntity], this class
 * carries no Room or Android framework annotations, making it usable in pure Kotlin
 * unit tests and decoupled from the database implementation.
 *
 * Conversion between the domain model and the entity is handled by the companion
 * object factory method and the [toEntity] extension function, following the
 * clean-architecture mapping pattern.
 *
 * Package: com.example.afyagpt.domain.model
 */
package com.example.afyagpt.domain.model

import com.example.afyagpt.data.local.entity.UserEntity

// ── Supporting enumerations ────────────────────────────────────────────────────

/**
 * Available UI colour themes for the AfyaGPT application.
 *
 * The string name of each constant is used as the persisted value in both
 * [UserEntity.themePreference] and DataStore, so renaming these constants is a
 * breaking change that requires a database migration.
 */
enum class AppTheme {
    /** Brand default: AfyaGPT blue and yellow palette. */
    BLUE_YELLOW,

    /** High-contrast dark mode. */
    DARK,

    /** Clean light mode. */
    LIGHT
}

/**
 * Health professions supported by the AfyaGPT registration flow.
 *
 * @property displayName A user-facing, human-readable label for the profession.
 */
enum class Profession(val displayName: String) {
    COMMUNITY_HEALTH_WORKER("Community Health Worker"),
    NURSE("Nurse"),
    CLINICAL_OFFICER("Clinical Officer"),
    MEDICAL_OFFICER("Medical Officer"),
    MIDWIFE("Midwife"),
    NUTRITIONIST("Nutritionist"),
    OTHER("Other");

    companion object {
        /**
         * Returns the [Profession] whose name matches [value], or [OTHER] if not found.
         * Safe to call with any string from the database.
         *
         * @param value The stored string value (matches enum constant name).
         */
        fun fromString(value: String): Profession =
            entries.firstOrNull { it.name == value } ?: OTHER
    }
}

// ── Domain model ───────────────────────────────────────────────────────────────

/**
 * Domain representation of an AfyaGPT user.
 *
 * This is the object passed between the domain, presentation, and UI layers.
 * All business rules (e.g., display name formatting, theme resolution) operate
 * on this class rather than on [UserEntity].
 *
 * @property id                 Database primary key (0 before first persistence).
 * @property fullName           The user's full name.
 * @property email              Optional email address.
 * @property phoneNumber        Primary identifier; Kenyan mobile format.
 * @property profession         Profession constant name stored as a string.
 * @property professionalNumber Optional regulatory licence number.
 * @property facilityName       Name of the health facility.
 * @property county             Kenyan county.
 * @property subCounty          Sub-county (optional).
 * @property ward               Ward (optional).
 * @property malariaRiskZone    "HIGH", "LOW", or "NONE".
 * @property pinHash            Salted SHA-256 PIN hash. Never expose to UI.
 * @property profilePhotoUri    Local URI of the profile photo, or null.
 * @property themePreference    Persisted theme string (matches [AppTheme] name).
 * @property isActive           Whether the account is enabled.
 * @property createdAt          ISO 8601 creation datetime.
 * @property lastLoginAt        ISO 8601 last-login datetime, or null.
 */
data class User(
    val id: Int = 0,
    val fullName: String,
    val email: String? = null,
    val phoneNumber: String,
    val profession: String,
    val professionalNumber: String? = null,
    val facilityName: String,
    val county: String,
    val subCounty: String? = null,
    val ward: String? = null,
    val malariaRiskZone: String = "HIGH",
    val pinHash: String,
    val profilePhotoUri: String? = null,
    val themePreference: String = "BLUE_YELLOW",
    val isActive: Boolean = true,
    val createdAt: String,
    val lastLoginAt: String? = null
) {
    // ── Derived properties ─────────────────────────────────────────────────────

    /**
     * The user's [Profession] enum value, resolved from the stored string.
     * Falls back to [Profession.OTHER] if the stored string is unrecognised.
     */
    val professionEnum: Profession
        get() = Profession.fromString(profession)

    /**
     * The user's [AppTheme] enum value, resolved from the stored string.
     * Falls back to [AppTheme.BLUE_YELLOW] if unrecognised.
     */
    val appTheme: AppTheme
        get() = AppTheme.entries.firstOrNull { it.name == themePreference } ?: AppTheme.BLUE_YELLOW

    /**
     * The user's first name, derived by splitting [fullName] on whitespace.
     * Useful for personalised greetings in the UI ("Hello, Jane").
     */
    val firstName: String
        get() = fullName.trim().split(" ").firstOrNull() ?: fullName

    // ── Factory ────────────────────────────────────────────────────────────────

    companion object {

        /**
         * Converts a [UserEntity] (database layer) into a [User] (domain layer).
         *
         * Called by the repository after every database read to ensure the rest of
         * the application never holds a reference to a Room-annotated entity.
         *
         * @param entity The [UserEntity] fetched from Room.
         * @return The equivalent domain [User] object.
         */
        fun fromEntity(entity: UserEntity): User = User(
            id = entity.id,
            fullName = entity.fullName,
            email = entity.email,
            phoneNumber = entity.phoneNumber,
            profession = entity.profession,
            professionalNumber = entity.professionalNumber,
            facilityName = entity.facilityName,
            county = entity.county,
            subCounty = entity.subCounty,
            ward = entity.ward,
            malariaRiskZone = entity.malariaRiskZone,
            pinHash = entity.pinHash,
            profilePhotoUri = entity.profilePhotoUri,
            themePreference = entity.themePreference,
            isActive = entity.isActive,
            createdAt = entity.createdAt,
            lastLoginAt = entity.lastLoginAt
        )
    }
}

// ── Extension functions ────────────────────────────────────────────────────────

/**
 * Converts this [User] domain object back to a [UserEntity] for database persistence.
 *
 * Used by the repository when it needs to update a user record after modifying
 * the domain object (e.g., after a theme change).
 *
 * @return A [UserEntity] with all fields mapped from this [User].
 */
fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    fullName = fullName,
    email = email,
    phoneNumber = phoneNumber,
    profession = profession,
    professionalNumber = professionalNumber,
    facilityName = facilityName,
    county = county,
    subCounty = subCounty,
    ward = ward,
    malariaRiskZone = malariaRiskZone,
    pinHash = pinHash,
    profilePhotoUri = profilePhotoUri,
    themePreference = themePreference,
    isActive = isActive,
    createdAt = createdAt,
    lastLoginAt = lastLoginAt
)
