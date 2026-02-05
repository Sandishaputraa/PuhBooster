# Add project specific ProGuard rules here.
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Shizuku rules
-keep class rikka.shizuku.** { *; }
-keep class dev.rikka.shizuku.** { *; }
-dontwarn rikka.shizuku.**
-dontwarn dev.rikka.shizuku.**

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keepclassmembers class kotlinx.coroutines.** { *; }

# Material Design
-keep class com.google.android.material.** { *; }
-keep interface com.google.android.material.** { *; }

# View binding
-keep class * extends androidx.viewbinding.ViewBinding { *; }

# Keep application
-keep class com.puh.booster.BoosterApplication { *; }
-keep class com.puh.booster.MainActivity { *; }
-keep class com.puh.booster.BoosterUtils { *; }

# Keep data classes
-keep class com.puh.booster.models.** { *; }

# Retrofit (if added later)
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# GSON (if added later)
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# WorkManager
-keep class androidx.work.** { *; }
-keepclassmembers class androidx.work.** { *; }

# If you have issues with reflection
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations