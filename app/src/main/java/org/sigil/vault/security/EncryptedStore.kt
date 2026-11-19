package org.sigil.vault.security

import android.content.Context
import java.io.File

/** Encrypted records are stored as individual files below the app-private vault directory. */
class EncryptedStore(context: Context) {
    private val root = File(context.filesDir, "vault-store")

    init {
        require(root.exists() || root.mkdirs()) { "Cannot create vault storage" }
    }

    fun directory(): File = root

    fun read(name: String): ByteArray? {
        val file = File(root, name)
        return if (file.isFile) file.readBytes() else null
    }

    fun write(name: String, bytes: ByteArray) {
        val target = File(root, name)
        val temp = File(root, "$name.tmp-${System.nanoTime()}")
        temp.writeBytes(bytes)
        require(temp.renameTo(target)) { "Atomic write failed for $name" }
    }

    fun remove(name: String) {
        File(root, name).delete()
    }
}
