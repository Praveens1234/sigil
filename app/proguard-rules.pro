# Sigil ProGuard/R8 rules (blueprint: R8 full mode, release logs nothing).

# Keep our entry points and model classes.
-keep class org.sigil.vault.** { *; }
-keepclassmembers class org.sigil.vault.** { *; }

# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Compose keeps its runtime entry points.
-keep class androidx.compose.runtime.** { *; }

# --- Cryptography keep rules ----------------------------------------------
# BouncyCastle (ML-KEM-1024, signing). Registered providers must survive.
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# Google Tink (X25519). Keeps the primitives and key managers.
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# Apache Commons Codec (SHA-3 / Keccak / BLAKE2b).
-keep class org.apache.commons.codec.** { *; }
-dontwarn org.apache.commons.codec.**

# zxing core (QR rendering only; keep the minimal encoder).
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**
