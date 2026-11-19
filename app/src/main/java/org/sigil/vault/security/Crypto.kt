package org.sigil.vault.security

import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.text.Normalizer
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.bouncycastle.crypto.engines.ChaCha7539Engine
import org.bouncycastle.crypto.modes.ChaCha20Poly1305
import org.bouncycastle.crypto.params.KeyParameter
import org.bouncycastle.crypto.params.ParametersWithIV

/**
 * AEAD, MAC and password-based key derivation used by Sigil.
 *
 * BouncyCastle's maintained light-weight API supplies ChaCha20-Poly1305;
 * AES-GCM and HMAC use the Android platform providers.
 */
internal object Crypto {
    private const val NONCE_GCM = 12
    private const val TAG_BITS = 128
    private const val TAG_BYTES = 16
    private val random = SecureRandom()

    fun xchacha20poly1305Encrypt(
        key: ByteArray,
        nonce: ByteArray,
        aad: ByteArray?,
        plaintext: ByteArray
    ): ByteArray {
        require(key.size == 32) { "XChaCha20-Poly1305 requires a 256-bit key" }
        require(nonce.size == 24) { "XChaCha20-Poly1305 requires a 192-bit nonce" }
        return chacha20Poly1305(encrypt = true, key, nonce, aad, plaintext)
    }

    fun xchacha20poly1305Decrypt(
        key: ByteArray,
        nonce: ByteArray,
        aad: ByteArray?,
        ciphertextAndTag: ByteArray
    ): ByteArray {
        require(key.size == 32)
        require(nonce.size == 24)
        require(ciphertextAndTag.size >= TAG_BYTES)
        return chacha20Poly1305(false, key, nonce, aad, ciphertextAndTag)
    }

    private fun chacha20Poly1305(
        encrypt: Boolean,
        key: ByteArray,
        nonce: ByteArray,
        aad: ByteArray?,
        input: ByteArray
    ): ByteArray {
        val aead = ChaCha20Poly1305(ChaCha7539Engine(), 16)
        aead.init(encrypt, ParametersWithIV(KeyParameter(key), nonce))
        if (aad != null && aad.isNotEmpty()) aead.processAadByte(aad[0])
        // processAADBytes avoids special cases for an empty AAD field.
        if (aad != null && aad.isNotEmpty()) {
            if (aad.size > 1) aead.processAADBytes(aad, 1, aad.size - 1)
        }
        val output = ByteArray(aead.getOutputSize(input.size))
        val used = aead.processBytes(input, 0, input.size, output, 0)
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

    /** Non-throwing decryption helper. */
    fun tryDecrypt(
        key: ByteArray,
        nonce: ByteArray,
        aad: ByteArray?,
        ciphertext: ByteArray
    ): ByteArray? = try {
        xchacha20poly1305Decrypt(key, nonce, aad, ciphertext)
    } catch (_: GeneralSecurityException) {
        null
    }

    /** UTF-8 encoding for authenticated string fields, never secrets. */
    fun utf8(value: String): ByteArray = value.toByteArray(StandardCharsets.UTF_8)
}
