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

import gc.david.dipole.appearance.AppearanceMode
import gc.david.dipole.appearance.BoardTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsViewModelTest {

    private val preferences = FakeGamePreferences()

    @Test
    fun appearanceChoicesApplyAndAreRemembered() {
        val vm = SettingsViewModel(preferences)
        vm.onAction(SettingsAction.AppearanceModeChosen(AppearanceMode.Light))
        vm.onAction(SettingsAction.BoardThemeChosen(BoardTheme.HighContrast))
        assertEquals(AppearanceMode.Light, vm.uiState.value.appearanceMode)
        assertEquals(BoardTheme.HighContrast, vm.uiState.value.boardTheme)
        val reopened = SettingsViewModel(preferences).uiState.value
        assertEquals(AppearanceMode.Light, reopened.appearanceMode)
        assertEquals(BoardTheme.HighContrast, reopened.boardTheme)
    }

    @Test
    fun soundSwitchIsRemembered() {
        val vm = SettingsViewModel(preferences)
        assertTrue(vm.uiState.value.soundOn)
        vm.onAction(SettingsAction.SoundToggled)
        assertFalse(vm.uiState.value.soundOn)
        assertFalse(preferences.soundOn)
        assertFalse(SettingsViewModel(preferences).uiState.value.soundOn)
    }
}
