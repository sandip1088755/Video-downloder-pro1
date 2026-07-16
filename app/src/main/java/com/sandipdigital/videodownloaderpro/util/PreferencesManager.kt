package com.sandipdigital.videodownloaderpro.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext private val context: Context) {

    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val MAX_CONCURRENT = intPreferencesKey("max_concurrent_downloads")
        val WIFI_ONLY = booleanPreferencesKey("wifi_only")
        val AUTO_RETRY = booleanPreferencesKey("auto_retry")

        }

    val onboardingDone: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = done }
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map {
        ThemeMode.valueOf(it[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    val dynamicColorEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.DYNAMIC_COLOR] ?: true }

    suspend fun setDynamicColorEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    val maxConcurrentDownloads: Flow<Int> =
        context.dataStore.data.map { it[Keys.MAX_CONCURRENT] ?: 3 }

    suspend fun setMaxConcurrentDownloads(value: Int) {
        context.dataStore.edit { it[Keys.MAX_CONCURRENT] = value }
    }

    val wifiOnly: Flow<Boolean> = context.dataStore.data.map { it[Keys.WIFI_ONLY] ?: false }

    suspend fun setWifiOnly(enabled: Boolean) {
        context.dataStore.edit { it[Keys.WIFI_ONLY] = enabled }
    }

    val autoRetry: Flow<Boolean> = context.dataStore.data.map { it[Keys.AUTO_RETRY] ?: true }

    suspend fun setAutoRetry(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_RETRY] = enabled }
    }
}
