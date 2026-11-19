package org.sigil.vault.security

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class LockPhase { FIRST_RUN, LOCKED, UNLOCKED }

/**
 * In-memory authentication gate. The application owns this object; no screen can
 * render vault content without the UNLOCKED phase and a loaded vault-key handle.
 */
class LockManager private constructor(context: Context) {
    private val hardware = HardwareKeyWrap(context)
    private val _phase = MutableStateFlow(LockPhase.FIRST_RUN)
    val phase: StateFlow<LockPhase> = _phase

    @Volatile
    private var vaultKey: ByteArray? = null

    fun onStop() {
        vaultKey?.fill(0)
        vaultKey = null
        if (_phase.value == LockPhase.UNLOCKED) _phase.value = LockPhase.LOCKED
    }

    fun lockdown() {
        hardware.destroy()
        onStop()
    }

    fun isUnlocked(): Boolean = _phase.value == LockPhase.UNLOCKED

    fun unlockKey(key: ByteArray) {
        require(key.size == 32)
        vaultKey?.fill(0)
        vaultKey = key.copyOf()
        _phase.value = LockPhase.UNLOCKED
    }

    /** Returns a copy; callers must wipe their copy after use. */
    fun keyCopy(): ByteArray? = vaultKey?.copyOf()

    fun markConfigured() {
        if (_phase.value == LockPhase.FIRST_RUN) _phase.value = LockPhase.LOCKED
    }

    companion object {
        @Volatile
        private var instance: LockManager? = null

        fun get(context: Context): LockManager =
            instance ?: synchronized(this) {
                instance ?: LockManager(context.applicationContext).also { instance = it }
            }
    }
}
