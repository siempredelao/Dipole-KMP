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
