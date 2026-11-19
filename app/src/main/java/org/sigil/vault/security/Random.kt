package org.sigil.vault.security

import java.security.SecureRandom

/** Cryptographically strong generation for non-secret identifiers and salts. */
object Random {
    private val random = SecureRandom()
    fun bytes(size: Int): ByteArray = ByteArray(size).also(random::nextBytes)
}
