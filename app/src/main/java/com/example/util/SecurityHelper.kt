package com.example.util

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SecurityHelper {

    private const val ITERATIONS = 12000
    private const val KEY_LENGTH = 256

    /**
     * Hashes a 4-digit PIN using PBKDF2WithHmacSHA256 and a cryptographically secure random salt.
     */
    fun hashPin(pin: String): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)

        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = skf.generateSecret(spec).encoded

        val saltHex = salt.joinToString("") { "%02x".format(it) }
        val hashHex = hash.joinToString("") { "%02x".format(it) }
        return "pbkdf2:$saltHex:$hashHex"
    }

    /**
     * Verifies the PIN against the stored hash. Supports PBKDF2 with salt, and falls back to SHA-256 for legacy compatibility.
     */
    fun verifyPin(pin: String, storedHash: String?): Boolean {
        if (storedHash.isNullOrBlank()) return true

        return if (storedHash.startsWith("pbkdf2:")) {
            try {
                val parts = storedHash.split(":")
                if (parts.size != 3) return false
                val saltHex = parts[1]
                val expectedHashHex = parts[2]

                val salt = saltHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
                val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
                val skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                val hash = skf.generateSecret(spec).encoded
                val computedHashHex = hash.joinToString("") { "%02x".format(it) }

                MessageDigest.isEqual(computedHashHex.toByteArray(), expectedHashHex.toByteArray())
            } catch (_: Exception) {
                false
            }
        } else {
            // Legacy SHA-256 fallback
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
            val sha256 = hash.joinToString("") { "%02x".format(it) }
            MessageDigest.isEqual(sha256.toByteArray(), storedHash.toByteArray())
        }
    }
}

