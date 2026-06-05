# ProGuard rules for NovaOS Launcher

# Keep Room entities
-keep class com.novaos.launcher.data.local.room.entity.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Compose
-dontwarn androidx.compose.**

# Keep Kotlin metadata
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
