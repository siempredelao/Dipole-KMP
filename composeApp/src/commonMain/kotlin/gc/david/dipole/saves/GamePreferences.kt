package gc.david.dipole.saves

import com.russhwolf.settings.Settings
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

    private companion object {
        const val LAST_DIFFICULTY_KEY = "last_difficulty"
        const val LAST_SIDE_KEY = "last_side"
        const val SOUND_ON_KEY = "sound_on"
    }
}
