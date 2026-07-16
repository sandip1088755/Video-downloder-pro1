package com.sandipdigital.videodownloaderpro.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sandipdigital.videodownloaderpro.util.ThemeMode

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onOpenPrivacy: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val maxConcurrent by viewModel.maxConcurrent.collectAsState()
    val wifiOnly by viewModel.wifiOnly.collectAsState()
    val autoRetry by viewModel.autoRetry.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(20.dp))

        Text("Appearance", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        SingleChoiceSegment(
            options = listOf("System", "Light", "Dark"),
            selectedIndex = when (themeMode) {
                ThemeMode.SYSTEM -> 0; ThemeMode.LIGHT -> 1; ThemeMode.DARK -> 2
            },
            onSelected = {
                viewModel.setThemeMode(listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK)[it])
            }
        )
        Spacer(Modifier.height(12.dp))
        SettingSwitchRow(
            title = "Material You dynamic color",
            subtitle = "Use colors from your wallpaper (Android 12+)",
            checked = dynamicColor,
            onCheckedChange = viewModel::setDynamicColor
        )

        Spacer(Modifier.height(24.dp))
        Text("Downloads", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text("Max simultaneous downloads: $maxConcurrent", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = maxConcurrent.toFloat(),
            onValueChange = { viewModel.setMaxConcurrent(it.toInt()) },
            valueRange = 1f..5f,
            steps = 3
        )
        SettingSwitchRow(
            title = "Wi-Fi only",
            subtitle = "Pause downloads when on mobile data",
            checked = wifiOnly,
            onCheckedChange = viewModel::setWifiOnly
        )
        SettingSwitchRow(
            title = "Auto retry on failure",
            subtitle = "Automatically retry failed downloads",
            checked = autoRetry,
            onCheckedChange = viewModel::setAutoRetry
        )

        Spacer(Modifier.height(24.dp))
        Text("About", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        SettingLinkRow("Privacy Policy", onOpenPrivacy)
        SettingLinkRow("Terms of Service", onOpenTerms)
        SettingLinkRow("About Video Downloader Pro", onOpenAbout)
    }
}

@Composable
private fun SingleChoiceSegment(options: List<String>, selectedIndex: Int, onSelected: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, label ->
            SegmentedButton(index = index, count = options.size, selected = index == selectedIndex, onClick = { onSelected(index) }, label = label)
        }
    }
}

@Composable
private fun SegmentedButton(index: Int, count: Int, selected: Boolean, onClick: () -> Unit, label: String) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = Modifier.padding(end = 8.dp)
    )
}

@Composable
private fun SettingSwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingLinkRow(title: String, onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(title, modifier = Modifier.fillMaxWidth())
    }
}
