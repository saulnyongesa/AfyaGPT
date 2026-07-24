/**
 * PinHasher.kt
 *
 * Provides secure hashing and verification of 6-digit PINs using SHA-256 with
 * a randomly generated salt. This ensures that two users with the same PIN will
 * have different stored hashes, and that the original PIN cannot be recovered
 * from the stored hash (one-way function).
 *
 * Security approach:
 * - Salt prevents rainbow-table and pre-computation attacks.
 * - SHA-256 is a cryptographic hash function; it is computationally infeasible
 *   to reverse.
 * - SecureRandom is used instead of Random to guarantee cryptographic randomness.
 *
 * Package: com.example.afyagpt.util
 */
package com.example.afyagpt.util

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Singleton object responsible for PIN hashing and verification.
 *
 * Stored format: "<Base64-salt>:<Base64-hash>"
 * This format allows the salt and hash to be stored together in a single
 * database column and split apart during verification.
 */
object PinHasher {

    /** Length of the random salt in bytes. 16 bytes = 128-bit entropy. */
    private const val SALT_LENGTH_BYTES = 16

    /** The hashing algorithm used. SHA-256 is available on all Android versions. */
    private const val HASH_ALGORITHM = "SHA-256"

    /**
     * Hashes the given PIN with a freshly generated random salt.
     *
     * The process:
     * 1. Generate 16 cryptographically random bytes as the salt.
     * 2. Concatenate the salt bytes with the PIN's UTF-8 bytes.
     * 3. Compute SHA-256 over the concatenated bytes.
     * 4. Encode both salt and hash as Base64 and return them as "salt:hash".
     *
     * @param pin The plain-text 6-digit PIN entered by the user.
     * @return A string in the format "Base64(salt):Base64(sha256(salt+pin))".
     */
    fun hashPin(pin: String): String {
        // Generate a new random salt for every hashing operation.
        // Using SecureRandom (not java.util.Random) ensures cryptographic quality randomness.
        val salt = ByteArray(SALT_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }

        // Compute the hash by concatenating salt + PIN bytes, then applying SHA-256.
        val hash = computeHash(salt, pin)

        // Encode both components as Base64 (NO_WRAP avoids line breaks in the string).
        val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hashB64 = Base64.encodeToString(hash, Base64.NO_WRAP)

        return "$saltB64:$hashB64"
    }

    /**
     * Verifies a plain-text PIN against a previously stored hash string.
     *
     * The stored hash is split on ':' to recover the original salt, then the
     * candidate PIN is hashed with that same salt. If the resulting hash matches
     * the stored hash, the PIN is correct.
     *
     * @param pin The plain-text PIN entered by the user during login.
     * @param storedHash The "salt:hash" string retrieved from the database.
     * @return `true` if the PIN matches the stored hash; `false` otherwise.
     */
    fun verifyPin(pin: String, storedHash: String): Boolean {
        return try {
            // Split the stored value into its two components.
            val parts = storedHash.split(":")
            if (parts.size != 2) return false

            // Decode the original salt from Base64.
            val salt = Base64.decode(parts[0], Base64.NO_WRAP)
            // Decode the stored hash from Base64.
            val expectedHash = Base64.decode(parts[1], Base64.NO_WRAP)

            // Recompute the hash using the recovered salt and the candidate PIN.
            val actualHash = computeHash(salt, pin)

            // Use MessageDigest.isEqual for a constant-time comparison to prevent
            // timing side-channel attacks where an attacker could infer partial matches.
            MessageDigest.isEqual(actualHash, expectedHash)
        } catch (e: Exception) {
            // Any decoding or hashing error means verification fails safely.
            false
        }
    }

    /**
     * Internal helper that concatenates [salt] and [pin] bytes, then returns the SHA-256 digest.
     *
     * @param salt The raw salt bytes.
     * @param pin  The plain-text PIN string.
     * @return The SHA-256 digest byte array.
     */
    private fun computeHash(salt: ByteArray, pin: String): ByteArray {
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        // Feed salt first, then PIN bytes, into the digest.
        digest.update(salt)
        digest.update(pin.toByteArray(Charsets.UTF_8))
        return digest.digest()
    }
}
