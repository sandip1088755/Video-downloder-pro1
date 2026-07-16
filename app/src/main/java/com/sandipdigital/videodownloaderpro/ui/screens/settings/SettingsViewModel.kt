package com.sandipdigital.videodownloaderpro.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandipdigital.videodownloaderpro.util.PreferencesManager
import com.sandipdigital.videodownloaderpro.util.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: PreferencesManager
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = preferences.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    val dynamicColor: StateFlow<Boolean> = preferences.dynamicColorEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val maxConcurrent: StateFlow<Int> = preferences.maxConcurrentDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 3)

    val wifiOnly: StateFlow<Boolean> = preferences.wifiOnly
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val autoRetry: StateFlow<Boolean> = preferences.autoRetry
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { preferences.setThemeMode(mode) }
    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch { preferences.setDynamicColorEnabled(enabled) }
    fun setMaxConcurrent(value: Int) = viewModelScope.launch { preferences.setMaxConcurrentDownloads(value) }
    fun setWifiOnly(enabled: Boolean) = viewModelScope.launch { preferences.setWifiOnly(enabled) }
    fun setAutoRetry(enabled: Boolean) = viewModelScope.launch { preferences.setAutoRetry(enabled) }
}
