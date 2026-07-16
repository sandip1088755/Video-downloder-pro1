package com.sandipdigital.videodownloaderpro.ui

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
class RootViewModel @Inject constructor(
    private val preferences: PreferencesManager
) : ViewModel() {

    val onboardingDone: StateFlow<Boolean?> = preferences.onboardingDone
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val themeMode: StateFlow<ThemeMode> = preferences.themeMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    val dynamicColor: StateFlow<Boolean> = preferences.dynamicColorEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun completeOnboarding() = viewModelScope.launch {
        preferences.setOnboardingDone(true)
    }
}
