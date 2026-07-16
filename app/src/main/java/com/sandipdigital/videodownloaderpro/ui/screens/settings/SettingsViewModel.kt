package com.sandipdigital.videodownloaderpro.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandipdigital.videodownloaderpro.data.datastore.AppSettings
import com.sandipdigital.videodownloaderpro.data.datastore.AppThemeMode
import com.sandipdigital.videodownloaderpro.data.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: SettingsDataStore
) : ViewModel() {

    val settings: StateFlow<AppSettings> = dataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun setDefaultQuality(value: String) = viewModelScope.launch { dataStore.setDefaultQuality(value) }
    fun setMaxParallel(value: Int) = viewModelScope.launch { dataStore.setMaxParallel(value) }
    fun setWifiOnly(value: Boolean) = viewModelScope.launch { dataStore.setWifiOnly(value) }
    fun setNotificationsEnabled(value: Boolean) = viewModelScope.launch { dataStore.setNotificationsEnabled(value) }
    fun setThemeMode(value: AppThemeMode) = viewModelScope.launch { dataStore.setThemeMode(value) }
    fun setDynamicColor(value: Boolean) = viewModelScope.launch { dataStore.setDynamicColor(value) }
    fun setLanguage(value: String) = viewModelScope.launch { dataStore.setLanguage(value) }
    fun setAutoUpdateCheck(value: Boolean) = viewModelScope.launch { dataStore.setAutoUpdateCheck(value) }
    fun setStorageLocation(value: String) = viewModelScope.launch { dataStore.setStorageLocation(value) }
}
