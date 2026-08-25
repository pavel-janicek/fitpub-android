# Add project specific ProGuard rules here.
# Retrofit, kotlinx.serialization and osmdroid ship their own consumer rules.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.fitpub.android.data.dto.** { *** Companion; }
-keepclasseswithmembers class com.fitpub.android.data.dto.** { *; }