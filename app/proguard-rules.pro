# Project specific ProGuard rules
-keepattributes *Annotation*, InnerClasses, Signature, SourceFile, LineNumberTable

# kotlinx.serialization
-keepclassmembers class ** {
    private synthetic <init>(...);
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-dontnote kotlinx.serialization.**
-keepclassmembers class com.fitpub.android.data.dto.** {
    *** Companion;
    *** serializer(...);
}
-keepclasseswithmembers class com.fitpub.android.data.dto.** { *; }

# Retrofit
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# osmdroid
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# Coil
-keep class coil.** { *; }
-dontwarn coil.**
