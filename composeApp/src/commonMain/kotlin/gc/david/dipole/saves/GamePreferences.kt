package gc.david.dipole.saves

import com.russhwolf.settings.Settings
import gc.david.dipole.appearance.AppearanceMode
import gc.david.dipole.appearance.BoardTheme
import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.Player

/** Choices remembered between launches. */
interface GamePreferences {
    /** The difficulty last picked for a game against the computer. */
    var lastDifficulty: Difficulty

    /** The side last picked for a game against the computer. */
    var lastSide: Player

    /** Whether moves make a sound and vibrate. */
    var soundOn: Boolean

    var appearanceMode: AppearanceMode

    var boardTheme: BoardTheme
}

class SettingsGamePreferences(
    private val settings: Settings = Settings(),
) : GamePreferences {

    override var lastDifficulty: Difficulty
        get() = settings.getStringOrNull(LAST_DIFFICULTY_KEY)
            ?.let { name -> Difficulty.entries.firstOrNull { it.name == name } }
            ?: Difficulty.Medium
        set(value) = settings.putString(LAST_DIFFICULTY_KEY, value.name)

    override var lastSide: Player
        get() = settings.getStringOrNull(LAST_SIDE_KEY)
            ?.let { name -> Player.entries.firstOrNull { it.name == name } }
            ?: Player.White
        set(value) = settings.putString(LAST_SIDE_KEY, value.name)

    override var soundOn: Boolean
        get() = settings.getBoolean(SOUND_ON_KEY, true)
        set(value) = settings.putBoolean(SOUND_ON_KEY, value)

    override var appearanceMode: AppearanceMode
        get() = settings.getStringOrNull(APPEARANCE_MODE_KEY)
            ?.let { name -> AppearanceMode.entries.firstOrNull { it.name == name } }
            ?: AppearanceMode.System
        set(value) = settings.putString(APPEARANCE_MODE_KEY, value.name)

    override var boardTheme: BoardTheme
        get() = settings.getStringOrNull(BOARD_THEME_KEY)
            ?.let { name -> BoardTheme.entries.firstOrNull { it.name == name } }
            ?: BoardTheme.Wood
        set(value) = settings.putString(BOARD_THEME_KEY, value.name)

    private companion object {
        const val LAST_DIFFICULTY_KEY = "last_difficulty"
        const val LAST_SIDE_KEY = "last_side"
        const val SOUND_ON_KEY = "sound_on"
        const val APPEARANCE_MODE_KEY = "appearance_mode"
        const val BOARD_THEME_KEY = "board_theme"
    }
}
