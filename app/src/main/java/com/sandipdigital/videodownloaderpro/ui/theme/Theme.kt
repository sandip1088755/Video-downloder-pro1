package com.sandipdigital.videodownloaderpro.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.sandipdigital.videodownloaderpro.data.datastore.AppThemeMode

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    secondary = TealSecondary,
    tertiary = AmberTertiary,
    background = LightBackground,
    surface = LightSurface,
    error = ErrorRed
)

private val DarkColors = darkColorScheme(
    primary = BluePrimaryDark,
    secondary = TealSecondaryDark,
    tertiary = AmberTertiaryDark,
    background = DarkBackground,
    surface = DarkSurface,
    error = ErrorRedDark
)

@Composable
fun VideoDownloaderProTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    useDynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
