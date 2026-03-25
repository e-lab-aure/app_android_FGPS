# Conserver les composants Android declares dans le manifest
-keep public class com.fgps.MainActivity
-keep public class com.fgps.MockLocationService
-keep public class com.fgps.GpsWidgetProvider

# Conserver les membres Kotlin des data classes
-keep class com.fgps.GpsPosition { *; }

# Conserver les enums et companion objects Kotlin
-keepclassmembers class com.fgps.** {
    static ** Companion;
    static ** INSTANCE;
}

# Regles standard AndroidX
-dontwarn androidx.**
