package org.sigil.vault.security

/**
 * Mutable byte buffer with secure wipe (blueprint section 7: "byte buffers
 * with explicit zeroization, never Strings").
 *
 * All secrets — the vault key, derived keys, passwords, recovery keys — live
 * in one of these. [wipe] overwrites the backing array with zeros.
 */
class SecureBuffer(initialCapacity: Int = 32) {
    private var buf: ByteArray = ByteArray(initialCapacity.coerceAtLeast(1))
    private var len: Int = 0

    val size: Int get() = len
    fun isEmpty(): Boolean = len == 0

    fun ensureCapacity(cap: Int) {
        if (cap <= buf.size) return
        val nb = ByteArray(cap)
        System.arraycopy(buf, 0, nb, 0, len)
        wipeArray(buf)
        buf = nb
    }

    fun put(b: ByteArray, off: Int = 0, count: Int = b.size) {
        ensureCapacity(len + count)
        System.arraycopy(b, off, buf, len, count)
        len += count
    }

    fun putByte(v: Int) {
        ensureCapacity(len + 1)
        buf[len] = (v and 0xFF).toByte()
        len++
    }

    /** Returns a copy of the contents; the copy is the caller's responsibility. */
    fun toArray(): ByteArray = buf.copyOf(len)

    fun toHex(): String {
        val sb = StringBuilder(len * 2)
        for (i in 0 until len) {
            val v = buf[i].toInt() and 0xFF
            sb.append(HEX[v ushr 4])
            sb.append(HEX[v and 0x0F])
        }
        return sb.toString()
    }

    /** Secure wipe. Call when the secret leaves scope. */
    fun wipe() {
        wipeArray(buf)
        len = 0
    }

    private fun wipeArray(a: ByteArray) {
        for (i in a.indices) a[i] = 0
    }

    companion object {
        private val HEX = "0123456789abcdef".toCharArray()
    }
}

/** Copies [src] into a fresh [SecureBuffer]. */
fun SecureBuffer(src: ByteArray): SecureBuffer =
    SecureBuffer(src.size).also { it.put(src) }
