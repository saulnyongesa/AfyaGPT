/**
 * DateTimeUtils.kt
 *
 * Date, time, age, and obstetric calculation utilities used throughout the
 * AfyaGPT clinical workflow. All functions operate on ISO 8601 strings
 * (yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss) so data can be stored as plain text
 * in SQLite without losing precision.
 *
 * Requires API 26+ (Android 8.0 Oreo) for java.time support. For older devices
 * enable desugaring in the module's build.gradle:
 *   compileOptions { isCoreLibraryDesugaringEnabled = true }
 *   dependencies { coreLibraryDesugaring("com.android.tools.build:desugaring:...") }
 *
 * Package: com.example.afyagpt.util
 */
package com.example.afyagpt.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Singleton object providing date/time helper functions for the AfyaGPT app.
 *
 * All "now" queries use the device's local time zone so that timestamps are
 * meaningful in the Kenyan clinical context.
 */
object DateTimeUtils {

    // ── Formatters ─────────────────────────────────────────────────────────────

    /** ISO 8601 date formatter: "yyyy-MM-dd" */
    private val ISO_DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE

    /** ISO 8601 datetime formatter: "yyyy-MM-ddTHH:mm:ss" */
    private val ISO_DATETIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    /** Human-readable date: "23 Jul 2026" */
    private val DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy")

    /** Human-readable date + time: "23 Jul 2026, 7:02 PM" */
    private val DISPLAY_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a")

    // ── Current date/time ──────────────────────────────────────────────────────

    /**
     * Returns the current local date-time as an ISO 8601 string.
     *
     * Example output: "2026-07-23T19:07:01"
     *
     * @return ISO 8601 datetime string for the device's current local time.
     */
    fun now(): String = LocalDateTime.now().format(ISO_DATETIME_FORMATTER)

    /**
     * Returns today's date as an ISO 8601 date string.
     *
     * Example output: "2026-07-23"
     *
     * @return ISO 8601 date string (yyyy-MM-dd) for today.
     */
    fun today(): String = LocalDate.now().format(ISO_DATE_FORMATTER)

    // ── Display formatting ─────────────────────────────────────────────────────

    /**
     * Formats an ISO date string into a human-readable display format.
     *
     * Example: "2026-07-23" → "23 Jul 2026"
     *
     * @param isoDate A date string in "yyyy-MM-dd" format.
     * @return A formatted display string, or the original string on parse failure.
     */
    fun formatDisplayDate(isoDate: String): String {
        return try {
            LocalDate.parse(isoDate, ISO_DATE_FORMATTER).format(DISPLAY_DATE_FORMATTER)
        } catch (e: Exception) {
            isoDate // Fail gracefully — return the raw string rather than crashing.
        }
    }

    /**
     * Formats an ISO datetime string into a human-readable display format.
     *
     * Example: "2026-07-23T19:07:01" → "23 Jul 2026, 7:07 PM"
     *
     * @param isoDate A datetime string in ISO 8601 format.
     * @return A formatted display string, or the original string on parse failure.
     */
    fun formatDisplayDateTime(isoDate: String): String {
        return try {
            LocalDateTime.parse(isoDate, ISO_DATETIME_FORMATTER)
                .format(DISPLAY_DATETIME_FORMATTER)
        } catch (e: Exception) {
            isoDate
        }
    }

    // ── Age calculations ───────────────────────────────────────────────────────

    /**
     * Calculates a patient's age in complete months from their date of birth to today.
     *
     * Useful for paediatric assessments where age in months is the clinical standard
     * (e.g., MUAC assessments for children 6–59 months).
     *
     * @param dobIso The patient's date of birth in "yyyy-MM-dd" format.
     * @return The number of complete months elapsed, or 0 on parse failure.
     */
    fun calculateAgeMonths(dobIso: String): Int {
        return try {
            val dob = LocalDate.parse(dobIso, ISO_DATE_FORMATTER)
            val today = LocalDate.now()
            Period.between(dob, today).let { it.years * 12 + it.months }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Calculates a patient's age in complete years from their date of birth to today.
     *
     * @param dobIso The patient's date of birth in "yyyy-MM-dd" format.
     * @return The number of complete years elapsed, or 0 on parse failure.
     */
    fun calculateAgeYears(dobIso: String): Int {
        return try {
            val dob = LocalDate.parse(dobIso, ISO_DATE_FORMATTER)
            Period.between(dob, LocalDate.now()).years
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Produces a natural-language age description suitable for clinical records.
     *
     * Examples:
     * - "2 years 3 months"  (for a child aged 2y 3m)
     * - "9 months"          (for an infant under 1 year)
     * - "35 years"          (for an adult with 0 additional months)
     *
     * @param dobIso The patient's date of birth in "yyyy-MM-dd" format.
     * @return A human-readable age string.
     */
    fun ageDescription(dobIso: String): String {
        return try {
            val dob = LocalDate.parse(dobIso, ISO_DATE_FORMATTER)
            val period = Period.between(dob, LocalDate.now())
            val years = period.years
            val months = period.months

            when {
                years == 0 -> "$months month${if (months != 1) "s" else ""}"
                months == 0 -> "$years year${if (years != 1) "s" else ""}"
                else -> "$years year${if (years != 1) "s" else ""} $months month${if (months != 1) "s" else ""}"
            }
        } catch (e: Exception) {
            "Unknown age"
        }
    }

    // ── Obstetric utilities ────────────────────────────────────────────────────

    /**
     * Calculates the Estimated Date of Delivery (EDD) using Naegele's rule:
     * EDD = Last Menstrual Period (LMP) + 280 days (40 weeks).
     *
     * @param lmpIso The date of the Last Menstrual Period in "yyyy-MM-dd" format.
     * @return The EDD as an ISO 8601 date string, or an empty string on failure.
     */
    fun calculateEDD(lmpIso: String): String {
        return try {
            val lmp = LocalDate.parse(lmpIso, ISO_DATE_FORMATTER)
            lmp.plusDays(280).format(ISO_DATE_FORMATTER)
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Calculates the gestational age in complete weeks from the LMP to today.
     *
     * Used in ANC visits to record and display weeks of gestation.
     *
     * @param lmpIso The date of the Last Menstrual Period in "yyyy-MM-dd" format.
     * @return The number of complete gestational weeks (0–42+), or 0 on failure.
     */
    fun gestationalAgeWeeks(lmpIso: String): Int {
        return try {
            val lmp = LocalDate.parse(lmpIso, ISO_DATE_FORMATTER)
            ChronoUnit.WEEKS.between(lmp, LocalDate.now()).toInt().coerceAtLeast(0)
        } catch (e: Exception) {
            0
        }
    }

    // ── Relative time ──────────────────────────────────────────────────────────

    /**
     * Returns a short relative time description for use in feeds and activity logs.
     *
     * Resolution:
     * - < 60 seconds   → "Just now"
     * - < 60 minutes   → "Xm ago"     (e.g. "2m ago")
     * - < 24 hours     → "Xh ago"     (e.g. "3h ago")
     * - Yesterday      → "Yesterday"
     * - Otherwise      → "23 Jul"     (day + abbreviated month, no year)
     *
     * @param isoDate An ISO 8601 datetime string (yyyy-MM-ddTHH:mm:ss).
     * @return A short relative-time string for display in the UI.
     */
    fun timeAgoDescription(isoDate: String): String {
        return try {
            val then = LocalDateTime.parse(isoDate, ISO_DATETIME_FORMATTER)
            val now = LocalDateTime.now()

            val secondsAgo = ChronoUnit.SECONDS.between(then, now)
            val minutesAgo = ChronoUnit.MINUTES.between(then, now)
            val hoursAgo = ChronoUnit.HOURS.between(then, now)

            val today = LocalDate.now()
            val thenDate = then.toLocalDate()

            when {
                secondsAgo < 60 -> "Just now"
                minutesAgo < 60 -> "${minutesAgo}m ago"
                hoursAgo < 24 -> "${hoursAgo}h ago"
                thenDate == today.minusDays(1) -> "Yesterday"
                else -> then.format(DateTimeFormatter.ofPattern("d MMM"))
            }
        } catch (e: Exception) {
            isoDate
        }
    }
}
