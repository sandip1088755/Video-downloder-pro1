package com.sandipdigital.videodownloaderpro.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

enum class AppThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val defaultQuality: String = "Q_720",
    val maxParallelDownloads: Int = 3,
    val wifiOnly: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val useDynamicColor: Boolean = true,
    val language: String = "en",
    val autoUpdateCheck: Boolean = true,
    val storageLocation: String = "" // empty = default app-specific external dir
)

@Singleton
class SettingsDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    private object Keys {
        val DEFAULT_QUALITY = stringPreferencesKey("default_quality")
        val MAX_PARALLEL = intPreferencesKey("max_parallel_downloads")
        val WIFI_ONLY = booleanPreferencesKey("wifi_only")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val LANGUAGE = stringPreferencesKey("language")
        val AUTO_UPDATE_CHECK = booleanPreferencesKey("auto_update_check")
        val STORAGE_LOCATION = stringPreferencesKey("storage_location")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            defaultQuality = prefs[Keys.DEFAULT_QUALITY] ?: "Q_720",
            maxParallelDownloads = prefs[Keys.MAX_PARALLEL] ?: 3,
            wifiOnly = prefs[Keys.WIFI_ONLY] ?: false,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            themeMode = AppThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: AppThemeMode.SYSTEM.name),
            useDynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: true,
            language = prefs[Keys.LANGUAGE] ?: "en",
            autoUpdateCheck = prefs[Keys.AUTO_UPDATE_CHECK] ?: true,
            storageLocation = prefs[Keys.STORAGE_LOCATION] ?: ""
        )
    }

    suspend fun setDefaultQuality(value: String) = context.dataStore.edit { it[Keys.DEFAULT_QUALITY] = value }
    suspend fun setMaxParallel(value: Int) = context.dataStore.edit { it[Keys.MAX_PARALLEL] = value }
    suspend fun setWifiOnly(value: Boolean) = context.dataStore.edit { it[Keys.WIFI_ONLY] = value }
    suspend fun setNotificationsEnabled(value: Boolean) = context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = value }
    suspend fun setThemeMode(value: AppThemeMode) = context.dataStore.edit { it[Keys.THEME_MODE] = value.name }
    suspend fun setDynamicColor(value: Boolean) = context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = value }
    suspend fun setLanguage(value: String) = context.dataStore.edit { it[Keys.LANGUAGE] = value }
    suspend fun setAutoUpdateCheck(value: Boolean) = context.dataStore.edit { it[Keys.AUTO_UPDATE_CHECK] = value }
    suspend fun setStorageLocation(value: String) = context.dataStore.edit { it[Keys.STORAGE_LOCATION] = value }
}
