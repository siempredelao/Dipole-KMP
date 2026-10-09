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

package gc.david.dipole.saves

import com.russhwolf.settings.MapSettings
import gc.david.dipole.appearance.AppearanceMode
import gc.david.dipole.appearance.BoardTheme
import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.Player
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsGamePreferencesTest {

    private val settings = MapSettings()

    @Test
    fun lastDifficultyDefaultsToMedium() {
        assertEquals(Difficulty.Medium, SettingsGamePreferences(settings).lastDifficulty)
    }

    @Test
    fun lastDifficultyIsRemembered() {
        SettingsGamePreferences(settings).lastDifficulty = Difficulty.Hard
        assertEquals(Difficulty.Hard, SettingsGamePreferences(settings).lastDifficulty)
    }

    @Test
    fun lastSideDefaultsToWhiteAndIsRemembered() {
        assertEquals(Player.White, SettingsGamePreferences(settings).lastSide)
        SettingsGamePreferences(settings).lastSide = Player.Black
        assertEquals(Player.Black, SettingsGamePreferences(settings).lastSide)
    }

    @Test
    fun soundIsOnUntilSwitchedOff() {
        assertEquals(true, SettingsGamePreferences(settings).soundOn)
        SettingsGamePreferences(settings).soundOn = false
        assertEquals(false, SettingsGamePreferences(settings).soundOn)
    }

    @Test
    fun appearanceDefaultsToSystemAndWoodAndIsRemembered() {
        assertEquals(AppearanceMode.System, SettingsGamePreferences(settings).appearanceMode)
        assertEquals(BoardTheme.Wood, SettingsGamePreferences(settings).boardTheme)
        SettingsGamePreferences(settings).appearanceMode = AppearanceMode.Dark
        SettingsGamePreferences(settings).boardTheme = BoardTheme.Slate
        assertEquals(AppearanceMode.Dark, SettingsGamePreferences(settings).appearanceMode)
        assertEquals(BoardTheme.Slate, SettingsGamePreferences(settings).boardTheme)
    }

    @Test
    fun tutorialIsUnseenUntilMarkedSeen() {
        assertEquals(false, SettingsGamePreferences(settings).tutorialSeen)
        SettingsGamePreferences(settings).tutorialSeen = true
        assertEquals(true, SettingsGamePreferences(settings).tutorialSeen)
    }

    @Test
    fun unknownStoredValueFallsBackToMedium() {
        settings.putString("last_difficulty", "Impossible")
        assertEquals(Difficulty.Medium, SettingsGamePreferences(settings).lastDifficulty)
    }
}
