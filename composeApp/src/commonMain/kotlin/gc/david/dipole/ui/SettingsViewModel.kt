/*
 * Copyright (c) 2026 David Aguiar Gonzalez
 *
 * Dipole, the game and its rules, was designed by Mark Steere
 * (Copyright (c) May 2007 by Mark Steere, https://www.marksteeregames.com).
 * This code, an implementation of the game, was developed by David Aguiar Gonzalez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
