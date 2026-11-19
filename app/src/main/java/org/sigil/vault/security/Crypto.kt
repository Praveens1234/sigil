package org.sigil.vault.security

import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.Normalizer
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AEAD, MAC and normalization primitives used by Sigil.
 *
 * AES-256-GCM and HMAC use the Android platform providers. XChaCha20-Poly1305
 * is provided by BouncyCastle's maintained low-level AEAD API.
 */
internal object Crypto {
    private const val NONCE_GCM = 12
    private const val TAG_BITS = 128
    private const val TAG_BYTES = 16
    private val random = SecureRandom()

    fun xchacha20poly1305Encrypt(key: ByteArray, nonce: ByteArray, aad: ByteArray?, plaintext: ByteArray): ByteArray {
        require(key.size == 32) { "XChaCha20-Poly1305 requires a 256-bit key" }
        require(nonce.size == 24) { "XChaCha20-Poly1305 requires a 192-bit nonce" }
        // BouncyCastle's AEAD API is available through the Android artifact.
        val engine = org.bouncycastle.crypto.engines.ChaCha7539Engine()
        val aead = org.bouncycastle.crypto.modes.ChaCha20Poly1305(engine, TAG_BYTES)
        aead.init(true, org.bouncycastle.crypto.params.ParametersWithIV(org.bouncycastle.crypto.params.KeyParameter(key), nonce))
        if (aad != null) aead.processAADBytes(aad, 0, aad.size)
        val output = ByteArray(aead.getOutputSize(plaintext.size))
        val used = aead.processBytes(plaintext, 0, plaintext.size, output, 0)
        aead.doFinal(output, used)
        return output
    }

    fun xchacha20poly1305Decrypt(key: ByteArray, nonce: ByteArray, aad: ByteArray?, blob: ByteArray): ByteArray {
        require(key.size == 32)
        require(nonce.size == 24)
        require(blob.size >= TAG_BYTES)
        val engine = org.bouncycastle.crypto.engines.ChaCha7539Engine()
        val aead = org.bouncycastle.crypto.modes.ChaCha20Poly1305(engine, TAG_BYTES)
        aead.init(false, org.bouncycastle.crypto.params.ParametersWithIV(org.bouncycastle.crypto.params.KeyParameter(key), nonce))
        if (aad != null) aead.processAADBytes(aad, 0, aad.size)
        val output = ByteArray(aead.getOutputSize(blob.size))
        val used = aead.processBytes(blob, 0, blob.size, output, 0)
        aead.doFinal(output, used)
        return output
    }

    fun aes256gcmEncrypt(key: ByteArray, aad: ByteArray?, plaintext: ByteArray): ByteArray {
        require(key.size == 32)
        val nonce = ByteArray(NONCE_GCM).also(random::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, nonce))
        if (aad != null) cipher.updateAAD(aad)
        return nonce + cipher.doFinal(plaintext)
    }

    fun aes256gcmDecrypt(key: ByteArray, aad: ByteArray?, blob: ByteArray): ByteArray {
        require(key.size == 32)
        require(blob.size >= NONCE_GCM + TAG_BYTES)
        val nonce = blob.copyOfRange(0, NONCE_GCM)
        val ciphertext = blob.copyOfRange(NONCE_GCM, blob.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, nonce))
        if (aad != null) cipher.updateAAD(aad)
        return cipher.doFinal(ciphertext)
    }

    fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray = hmac("HmacSHA256", key, data)
    fun hmacSha512(key: ByteArray, data: ByteArray): ByteArray = hmac("HmacSHA512", key, data)

    private fun hmac(algorithm: String, key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance(algorithm)
        mac.init(SecretKeySpec(key, algorithm))
        return mac.doFinal(data)
    }

    fun newNonce(size: Int = 24): ByteArray = ByteArray(size).also(random::nextBytes)
    fun normaliseSecret(value: String, phrase: Boolean = false): String {
        val normalised = Normalizer.normalize(value, Normalizer.Form.NFKC)
        return if (phrase) normalised.trim().replace(Regex("\\s+"), " ") else normalised
    }

    fun tryDecrypt(key: ByteArray, nonce: ByteArray, aad: ByteArray?, blob: ByteArray): ByteArray? = try {
        xchacha20poly1305Decrypt(key, nonce, aad, blob)
    } catch (_: GeneralSecurityException) {
        null
    }

    fun utf8(value: String): ByteArray = value.toByteArray(StandardCharsets.UTF_8)
}
