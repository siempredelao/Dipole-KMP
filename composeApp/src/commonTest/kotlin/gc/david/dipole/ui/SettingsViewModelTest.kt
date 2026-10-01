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
