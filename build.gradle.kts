// Sigil — root build file. No network permission is ever declared; a CI check
// greps the merged manifest for it and fails the build.
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "1.9.24" apply false
}
