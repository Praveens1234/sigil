package org.sigil.vault.security

import java.security.MessageDigest

/** Hash primitives from the Android platform providers. */
internal object Hashes {
    fun sha256(data: ByteArray): ByteArray = md("SHA-256", data)
    fun sha512(data: ByteArray): ByteArray = md("SHA-512", data)
    fun sha3_512(data: ByteArray): ByteArray = md("SHA3-512", data)
    fun sha3_256(data: ByteArray): ByteArray = md("SHA3-256", data)
    fun sha384(data: ByteArray): ByteArray = md("SHA-384", data)
    fun blake2b_256(data: ByteArray): ByteArray = md("BLAKE2B-256", data)
    fun md5(data: ByteArray): ByteArray = md("MD5", data)
    fun sha1(data: ByteArray): ByteArray = md("SHA-1", data)

    private fun md(alg: String, data: ByteArray): ByteArray =
        MessageDigest.getInstance(alg).digest(data)
}
