package org.sigil.vault.security

import java.security.MessageDigest

/**
 * Hash primitives (blueprint section 6.1):
 *   SHA-256, SHA-512 (platform), SHA3-512 (bundled Keccak via commons-codec),
 *   optional SHA-384, SHA3-256, BLAKE2b, and legacy MD5 and SHA-1
 *   (checking old downloads only — never secure).
 */
internal object Hashes {

    /** SHA-256 (platform MessageDigest). */
    fun sha256(data: ByteArray): ByteArray = md("SHA-256", data)

    /** SHA-512 (platform MessageDigest). */
    fun sha512(data: ByteArray): ByteArray = md("SHA-512", data)

    /** SHA3-512 (commons-codec). */
    fun sha3_512(data: ByteArray): ByteArray = md("SHA3-512", data)

    /** SHA3-256 (commons-codec). */
    fun sha3_256(data: ByteArray): ByteArray = md("SHA3-256", data)

    /** SHA-384 (platform). */
    fun sha384(data: ByteArray): ByteArray = md("SHA-384", data)

    /** BLAKE2b-256 (commons-codec, truncation of the 512-bit digest). */
    fun blake2b_256(data: ByteArray): ByteArray = md("BLAKE2B-256", data)

    /** Legacy MD5 — for checking old downloads only. NOT SECURE. */
    fun md5(data: ByteArray): ByteArray = md("MD5", data)

    /** Legacy SHA-1 — for checking old downloads only. NOT SECURE. */
    fun sha1(data: ByteArray): ByteArray = md("SHA-1", data)

    private fun md(alg: String, data: ByteArray): ByteArray {
        return try {
            MessageDigest.getInstance(alg).digest(data)
        } catch (e: Exception) {
            // commons-codec supplies SHA3-*, Keccak and BLAKE2b digests.
            try {
                org.apache.commons.codec.digest.DigestUtils.digest(
                    org.apache.commons.codec.digest.MessageDigestAlgorithms.newDigest(alg),
                    data
                )
            } catch (fallback: Exception) {
                throw IllegalStateException("Hash $alg unavailable", fallback)
            }
        }
    }
}
