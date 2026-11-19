package org.sigil.vault

import android.app.Application
import org.sigil.vault.security.LockManager

/** Application-owned lifecycle and vault boundary. */
class SigilApp : Application() {
    override fun onCreate() {
        super.onCreate()
        LockManager.get(this)
    }

    override fun onTerminate() {
        LockManager.get(this).onStop()
        super.onTerminate()
    }
}
