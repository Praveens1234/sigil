package org.sigil.vault.security

/**
 * On-disk format constants and authenticated-data layouts (blueprint section 6.3).
 *
 * Every container (file blob, note blob, index, keyring, backup manifest)
 * shares one header:
 *
 *   magic | version | alg | kdf id and params | salt | chunk size | item id | nonce prefix
 *
 * Chunk i: AEAD(key, nonce = prefix||i, aad = header||item id||i||isLast, data <= 64 KiB)
 *
 * The item id and chunk number sit in the authenticated data, so chunks cannot
 * be swapped, reordered or cut short.
 */
internal object SigilFormat {
    const val MAGIC = "SIGIL01"
    const val VERSION = 1

    /** AEAD algorithm ids. */
    const val ALG_XCHACHA20_POLY1305 = 1
    const val ALG_AES_256_GCM = 2

    /** KDF ids (Argon2id is the default; PBKDF2 floor in section 6.3). */
    const val KDF_ARGON2ID = 1
    const val KDF_PBKDF2_SHA512 = 2
    const val PBKDF2_MIN_ITERATIONS = 210_000

    /** Argon2id bounds from the blueprint: 64 MiB to 1 GiB, 2 to 10 passes. */
    const val ARGON2_MIN_MIB = 64
    const val ARGON2_MAX_MIB = 1024
    const val ARGON2_DEFAULT_MIB = 64
    const val ARGON2_MIN_PASSES = 2
    const val ARGON2_MAX_PASSES = 10
    const val ARGON2_DEFAULT_PASSES = 3

    /** Chunk size for file content: 64 KiB. */
    const val CHUNK_SIZE = 64 * 1024

    /** Vault key: 256 random bits, made once, never derived from the secret. */
    const val VAULT_KEY_BYTES = 32
    /** Recovery key: 52 characters in 13 groups of 4. */
    const val RECOVERY_KEY_CHARS = 52
    const val RECOVERY_KEY_GROUPS = 13
    const val GROUP_SIZE = 4
}
