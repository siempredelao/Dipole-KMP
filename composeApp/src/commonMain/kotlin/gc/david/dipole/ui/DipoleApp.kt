package gc.david.dipole.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import gc.david.dipole.saves.SettingsGamePreferences

@Composable
fun DipoleApp() {
    val preferences = remember { SettingsGamePreferences() }
    val settingsViewModel = viewModel { SettingsViewModel(preferences) }
    val settings by settingsViewModel.uiState.collectAsState()
    val viewModel = viewModel { DipoleViewModel(preferences = preferences) }
    val uiState by viewModel.uiState.collectAsState()
    DipoleTheme(settings.appearanceMode, settings.boardTheme) {
        DipoleScreen(uiState, settings, viewModel::onAction, settingsViewModel::onAction)
    }
}
