package org.sigil.vault.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Hardware-backed AES-256 key used to wrap the vault key for the return window. */
class HardwareKeyWrap(private val context: Context) {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val alias = "org.sigil.vault.hardware-wrap"

    fun ensureKey(): Boolean = try {
        if (!keyStore.containsAlias(alias)) {
            val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val spec = KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .setUnlockedDeviceRequired(true)
                .build()
            generator.init(spec)
            generator.generateKey()
        }
        true
    } catch (_: Exception) {
        false
    }

    fun wrap(plain: ByteArray): ByteArray? = try {
        ensureKey()
        val key = keyStore.getKey(alias, null) as SecretKey
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plain)
        iv + encrypted
    } catch (_: Exception) {
        null
    }

    fun unwrap(blob: ByteArray): ByteArray? = try {
        val key = keyStore.getKey(alias, null) as SecretKey
        val iv = blob.copyOfRange(0, 12)
        val encrypted = blob.copyOfRange(12, blob.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        cipher.doFinal(encrypted)
    } catch (_: Exception) {
        null
    }

    fun destroy() {
        try {
            if (keyStore.containsAlias(alias)) keyStore.deleteEntry(alias)
        } catch (_: Exception) {
            Unit
        }
    }
}
