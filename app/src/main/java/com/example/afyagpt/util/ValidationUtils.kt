/**
 * ValidationUtils.kt
 *
 * Centralised input-validation functions for all authentication and registration
 * forms in the AfyaGPT app. All validators return a [ValidationResult] so the
 * UI can display specific error messages without coupling business logic to
 * Android View concerns.
 *
 * Package: com.example.afyagpt.util
 */
package com.example.afyagpt.util

/**
 * Represents the outcome of a validation check.
 *
 * - [Valid]   — the input passed all checks.
 * - [Invalid] — the input failed; [Invalid.message] describes why.
 */
sealed class ValidationResult {
    /** The input is acceptable. */
    object Valid : ValidationResult()

    /**
     * The input is not acceptable.
     *
     * @property message A human-readable explanation suitable for display in the UI.
     */
    data class Invalid(val message: String) : ValidationResult()
}

/**
 * Singleton object containing all field-level validation logic.
 *
 * Every function that returns [Boolean] is a simple predicate (used internally or
 * for compound checks). Functions that return [ValidationResult] provide
 * user-facing feedback.
 */
object ValidationUtils {

    // ── Regex constants ────────────────────────────────────────────────────────

    /** Accepts only letters (including accented) and spaces; minimum 3 characters. */
    private val FULL_NAME_REGEX = Regex("^[\\p{L} ]{3,}$")

    /** Standard email regex — covers the vast majority of valid email addresses. */
    private val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )

    /**
     * Kenyan local mobile format:
     * - Starts with 07 or 01
     * - Followed by exactly 8 more digits → total 10 digits.
     */
    private val KENYAN_PHONE_REGEX = Regex("^0[17]\\d{8}$")

    /** Exactly 6 decimal digits. */
    private val PIN_REGEX = Regex("^\\d{6}$")

    /**
     * Professional/licence numbers are alphanumeric, optionally including
     * hyphens or forward-slashes, minimum 4 characters.
     */
    private val PROFESSIONAL_NUMBER_REGEX = Regex("^[A-Za-z0-9/-]{4,}$")

    // ── Public validators ──────────────────────────────────────────────────────

    /**
     * Validates a user's full name.
     *
     * Rules: at least 3 characters, letters and spaces only (supports Unicode letters
     * so Swahili and other scripts are accepted).
     *
     * @param name The raw name string from the UI field.
     * @return `true` if the name is valid.
     */
    fun isValidFullName(name: String): Boolean = FULL_NAME_REGEX.matches(name.trim())

    /**
     * Validates a full name and returns a descriptive [ValidationResult].
     *
     * @param name The raw name string.
     */
    fun validateFullName(name: String): ValidationResult {
        val trimmed = name.trim()
        return when {
            trimmed.isBlank() -> ValidationResult.Invalid("Full name is required")
            trimmed.length < 3 -> ValidationResult.Invalid("Name must be at least 3 characters")
            !FULL_NAME_REGEX.matches(trimmed) ->
                ValidationResult.Invalid("Name may contain letters and spaces only")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validates an email address.
     *
     * Email is optional in AfyaGPT — if the field is blank, validation passes.
     *
     * @param email The raw email string (may be blank).
     * @return `true` if the email is blank (optional) or matches a valid email pattern.
     */
    fun isValidEmail(email: String): Boolean =
        email.isBlank() || EMAIL_REGEX.matches(email.trim())

    /**
     * Validates an email address and returns a descriptive [ValidationResult].
     *
     * @param email The raw email string.
     */
    fun validateEmail(email: String): ValidationResult {
        if (email.isBlank()) return ValidationResult.Valid // optional field
        return if (EMAIL_REGEX.matches(email.trim())) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("Enter a valid email address (e.g. jane@example.com)")
        }
    }

    /**
     * Validates a Kenyan mobile phone number in local format (07xx or 01xx, 10 digits).
     *
     * @param phone The raw phone string from the UI field.
     * @return `true` if the number matches the expected Kenyan mobile format.
     */
    fun isValidPhoneNumber(phone: String): Boolean =
        KENYAN_PHONE_REGEX.matches(phone.trim())

    /**
     * Validates a Kenyan phone number and returns a descriptive [ValidationResult].
     *
     * @param phone The raw phone string.
     */
    fun validatePhoneNumber(phone: String): ValidationResult {
        val trimmed = phone.trim()
        return when {
            trimmed.isBlank() -> ValidationResult.Invalid("Phone number is required")
            !KENYAN_PHONE_REGEX.matches(trimmed) ->
                ValidationResult.Invalid(
                    "Enter a valid Kenyan number starting with 07 or 01 (10 digits)"
                )
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validates a 6-digit numeric PIN.
     *
     * @param pin The raw PIN string.
     * @return `true` if the PIN is exactly 6 digits.
     */
    fun isValidPin(pin: String): Boolean = PIN_REGEX.matches(pin)

    /**
     * Validates a PIN and returns a descriptive [ValidationResult].
     *
     * @param pin The raw PIN string.
     */
    fun validatePin(pin: String): ValidationResult {
        return when {
            pin.isBlank() -> ValidationResult.Invalid("PIN is required")
            !PIN_REGEX.matches(pin) -> ValidationResult.Invalid("PIN must be exactly 6 digits")
            else -> ValidationResult.Valid
        }
    }

    /**
     * Checks whether a PIN and its confirmation entry are identical.
     *
     * @param pin     The original PIN entry.
     * @param confirm The confirmation PIN entry.
     * @return `true` if both strings are equal.
     */
    fun pinsMatch(pin: String, confirm: String): Boolean = pin == confirm

    /**
     * Validates that two PIN entries match and returns a [ValidationResult].
     *
     * @param pin     The original PIN string.
     * @param confirm The confirmation PIN string.
     */
    fun validatePinsMatch(pin: String, confirm: String): ValidationResult {
        return if (pinsMatch(pin, confirm)) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid("PINs do not match")
        }
    }

    /**
     * Validates a professional/licence number.
     *
     * The field is optional — if blank, validation passes. Otherwise the number
     * must be at least 4 alphanumeric characters (hyphens and slashes allowed).
     *
     * @param number The raw professional number string.
     * @return `true` if blank (optional) or matches the expected pattern.
     */
    fun isValidProfessionalNumber(number: String): Boolean =
        number.isBlank() || PROFESSIONAL_NUMBER_REGEX.matches(number.trim())

    /**
     * Validates a professional number and returns a descriptive [ValidationResult].
     *
     * @param number The raw professional number string.
     */
    fun validateProfessionalNumber(number: String): ValidationResult {
        if (number.isBlank()) return ValidationResult.Valid // optional
        return if (PROFESSIONAL_NUMBER_REGEX.matches(number.trim())) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(
                "Licence number must be at least 4 alphanumeric characters"
            )
        }
    }

    /**
     * Converts a Kenyan local mobile number (07xx or 01xx) to the E.164 international
     * format (+2547xx or +2541xx) required for SMS / API integrations.
     *
     * Example: "0712345678" → "+254712345678"
     *
     * @param phone A validated 10-digit Kenyan local number.
     * @return The number in E.164 format, or the original string if it cannot be converted.
     */
    fun formatPhoneToE164(phone: String): String {
        val trimmed = phone.trim()
        return if (trimmed.startsWith("0") && trimmed.length == 10) {
            // Replace the leading '0' with the Kenya country code '+254'.
            "+254${trimmed.substring(1)}"
        } else {
            // Return unchanged if the format is unexpected (caller should validate first).
            trimmed
        }
    }
}
