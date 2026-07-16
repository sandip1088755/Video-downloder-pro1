# Keep Room entities
-keep class com.sandipdigital.videodownloaderpro.data.local.entity.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp

# WorkManager workers must keep constructors
-keep class com.sandipdigital.videodownloaderpro.worker.** { *; }

# Media3
-keep class androidx.media3.** { *; }
