package dev.siempredelao.dipole.saves

import com.russhwolf.settings.MapSettings
import dev.siempredelao.dipole.game.Difficulty
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
    fun unknownStoredValueFallsBackToMedium() {
        settings.putString("last_difficulty", "Impossible")
        assertEquals(Difficulty.Medium, SettingsGamePreferences(settings).lastDifficulty)
    }
}
