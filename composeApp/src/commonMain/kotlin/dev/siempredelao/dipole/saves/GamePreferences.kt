package dev.siempredelao.dipole.saves

import com.russhwolf.settings.Settings
import dev.siempredelao.dipole.game.Difficulty

/** Choices remembered between launches. */
interface GamePreferences {
    /** The difficulty last picked for a game against the computer. */
    var lastDifficulty: Difficulty
}

class SettingsGamePreferences(
    private val settings: Settings = Settings(),
) : GamePreferences {

    override var lastDifficulty: Difficulty
        get() = settings.getStringOrNull(LAST_DIFFICULTY_KEY)
            ?.let { name -> Difficulty.entries.firstOrNull { it.name == name } }
            ?: Difficulty.Medium
        set(value) = settings.putString(LAST_DIFFICULTY_KEY, value.name)

    private companion object {
        const val LAST_DIFFICULTY_KEY = "last_difficulty"
    }
}
