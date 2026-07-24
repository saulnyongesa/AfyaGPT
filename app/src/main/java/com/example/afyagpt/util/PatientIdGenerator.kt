/**
 * PatientIdGenerator.kt
 *
 * Generates human-readable, prefixed unique identifiers for patients, clinical
 * sessions, and referrals in the AfyaGPT system.
 *
 * These IDs are designed to be:
 * - Memorable and speakable (short alphanumeric codes).
 * - Distinguishable by prefix (RE- for patients, TRG- for sessions, REF- for referrals).
 * - Locally generated (no server round-trip needed) to support offline-first operation.
 *
 * NOTE: For production, consider replacing random number suffixes with a UUID or
 * a server-assigned sequence to guarantee global uniqueness across devices.
 *
 * Package: com.example.afyagpt.util
 */
package com.example.afyagpt.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Singleton object that generates prefixed ID strings for clinical records.
 */
object PatientIdGenerator {

    /** Formatter producing compact date segments like "20260723" used in session/referral IDs. */
    private val DATE_SEGMENT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd")

    /**
     * Generates a unique patient registration ID in the format "RE-XXXXX".
     *
     * The numeric suffix is a random 5-digit number (10 000 – 99 999), giving
     * 90 000 possible values per day per device. Combine with a sync timestamp
     * for global uniqueness.
     *
     * Example output: "RE-40921"
     *
     * @return A patient UID string prefixed with "RE-".
     */
    fun generate(): String {
        // Random number in range [10000, 99999] for a consistent 5-digit display.
        val suffix = (10_000..99_999).random()
        return "RE-$suffix"
    }

    /**
     * Generates a clinical session ID in the format "TRG-yyyyMMdd-XXXX".
     *
     * Embedding the date makes it easy to filter or audit sessions by day without
     * parsing additional fields.
     *
     * Example output: "TRG-20260723-8472"
     *
     * @return A session ID string prefixed with "TRG-".
     */
    fun generateSessionId(): String {
        val datePart = LocalDate.now().format(DATE_SEGMENT_FORMATTER)
        val suffix = (1_000..9_999).random()
        return "TRG-$datePart-$suffix"
    }

    /**
     * Generates a referral ID in the format "REF-yyyyMMdd-XXXX".
     *
     * Used to track patient referrals between facilities and link follow-up records.
     *
     * Example output: "REF-20260723-1234"
     *
     * @return A referral ID string prefixed with "REF-".
     */
    fun generateReferralId(): String {
        val datePart = LocalDate.now().format(DATE_SEGMENT_FORMATTER)
        val suffix = (1_000..9_999).random()
        return "REF-$datePart-$suffix"
    }
}
