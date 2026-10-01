package gc.david.dipole.ui

import androidx.lifecycle.ViewModel
import gc.david.dipole.appearance.AppearanceMode
import gc.david.dipole.appearance.BoardTheme
import gc.david.dipole.saves.GamePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** App-wide settings: how the app looks and whether moves make a sound. Remembered between launches. */
data class SettingsUiState(
    /** Whether moves make a sound and vibrate. */
    val soundOn: Boolean = true,
    val appearanceMode: AppearanceMode = AppearanceMode.System,
    val boardTheme: BoardTheme = BoardTheme.Wood,
)

sealed interface SettingsAction {
    data object SoundToggled : SettingsAction
    data class AppearanceModeChosen(val mode: AppearanceMode) : SettingsAction
    data class BoardThemeChosen(val theme: BoardTheme) : SettingsAction
}

class SettingsViewModel(private val preferences: GamePreferences) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            soundOn = preferences.soundOn,
            appearanceMode = preferences.appearanceMode,
            boardTheme = preferences.boardTheme,
        ),
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.SoundToggled -> {
                preferences.soundOn = !_uiState.value.soundOn
                _uiState.update { it.copy(soundOn = preferences.soundOn) }
            }
            is SettingsAction.AppearanceModeChosen -> {
                preferences.appearanceMode = action.mode
                _uiState.update { it.copy(appearanceMode = action.mode) }
            }
            is SettingsAction.BoardThemeChosen -> {
                preferences.boardTheme = action.theme
                _uiState.update { it.copy(boardTheme = action.theme) }
            }
        }
    }
}
