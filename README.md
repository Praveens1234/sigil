# Sigil

Sigil is a native Android offline cryptographic vault implementing the v4.0 blueprint: biometric-first unlocking, encrypted file blobs, private notes, a local tools suite, and a no-network security boundary.

## Build

The repository uses GitHub Actions to build the debug APK on Ubuntu with Temurin JDK 17, Gradle 8.9 and Android SDK Platform 35. No Android SDK is required on the contributor's phone.

## Security boundary

- No `android.permission.INTERNET` in the application manifest.
- No analytics, advertising, WebView, remote fonts or network client dependency.
- `allowBackup=false`; no cloud backup or device-transfer rules.
- Android Keystore-backed wrapping for short-lived in-memory key material.
- AES-256-GCM and ChaCha20-Poly1305 AEAD primitives from maintained libraries.
- Argon2id and PBKDF2-HMAC-SHA512 parameters are defined by the v4.0 format.
- Raw vault-key bytes are wiped when the activity stops.

This repository is a working foundation. The complete production feature set described by the blueprint is being implemented in verifiable build increments.
