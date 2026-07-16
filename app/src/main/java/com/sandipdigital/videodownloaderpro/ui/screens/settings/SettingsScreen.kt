package com.sandipdigital.videodownloaderpro.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sandipdigital.videodownloaderpro.data.datastore.AppThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Settings", fontWeight = FontWeight.SemiBold) }) }) { padding ->
        Column(
            Modifier.padding(padding).verticalScroll(rememberScrollState()).fillMaxSize().padding(vertical = 8.dp)
        ) {
            SectionHeader("Downloads")

            SettingsDropdownRow(
                title = "Default quality",
                value = settings.defaultQuality,
                options = listOf("Q_360", "Q_480", "Q_720", "Q_1080", "Q_1440", "Q_2160", "ORIGINAL"),
                labelMapper = { it.removePrefix("Q_") },
                onSelected = viewModel::setDefaultQuality
            )

            SettingsSliderRow(
                title = "Maximum parallel downloads",
                value = settings.maxParallelDownloads,
                range = 1..5,
                onValueChange = viewModel::setMaxParallel
            )

            SettingsSwitchRow(
                title = "Wi-Fi only downloads",
                subtitle = "Pause downloads automatically on mobile data",
                checked = settings.wifiOnly,
                onCheckedChange = viewModel::setWifiOnly
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SectionHeader("Notifications")

            SettingsSwitchRow(
                title = "Download notifications",
                subtitle = "Progress and completion alerts",
                checked = settings.notificationsEnabled,
                onCheckedChange = viewModel::setNotificationsEnabled
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SectionHeader("Appearance")

            SettingsDropdownRow(
                title = "Theme",
                value = settings.themeMode.name,
                options = AppThemeMode.entries.map { it.name },
                labelMapper = { it.lowercase().replaceFirstChar { c -> c.uppercase() } },
                onSelected = { viewModel.setThemeMode(AppThemeMode.valueOf(it)) }
            )

            SettingsSwitchRow(
                title = "Dynamic color (Material You)",
                subtitle = "Use wallpaper-based color palette on Android 12+",
                checked = settings.useDynamicColor,
                onCheckedChange = viewModel::setDynamicColor
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SectionHeader("Language")

            SettingsDropdownRow(
                title = "App language",
                value = settings.language,
                options = listOf("en", "hi"),
                labelMapper = { if (it == "hi") "हिन्दी (Hindi)" else "English" },
                onSelected = viewModel::setLanguage
            )

            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            SectionHeader("Updates")

            SettingsSwitchRow(
                title = "Check for updates automatically",
                subtitle = "Notify when a new version is available",
                checked = settings.autoUpdateCheck,
                onCheckedChange = viewModel::setAutoUpdateCheck
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsSwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsSliderRow(title: String, value: Int, range: IntRange, onValueChange: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
        Text("$title: $value", style = MaterialTheme.typography.bodyLarge)
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = range.last - range.first - 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDropdownRow(
    title: String,
    value: String,
    options: List<String>,
    labelMapper: (String) -> String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = labelMapper(value),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(labelMapper(option)) },
                        onClick = { onSelected(option); expanded = false }
                    )
                }
            }
        }
    }
}
