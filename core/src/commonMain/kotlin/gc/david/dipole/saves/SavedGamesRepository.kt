package gc.david.dipole.saves

import com.russhwolf.settings.Settings

interface SavedGamesRepository {
    /** All saved games, newest first. */
    fun list(): List<SavedGame>

    /** Stores [game], replacing any save with the same id. */
    fun save(game: SavedGame)

    fun delete(id: String)
}

/**
 * Keeps saved games in the platform's key-value store: SharedPreferences on Android,
 * NSUserDefaults on iOS, java.util.prefs on desktop and localStorage on the web.
 */
class SettingsSavedGamesRepository(
    private val settings: Settings,
) : SavedGamesRepository {

    override fun list(): List<SavedGame> =
        ids().mapNotNull { id -> settings.getStringOrNull(key(id))?.let { SavedGame.decode(id, it) } }
            .sortedByDescending { it.savedAtEpochMillis }

    override fun save(game: SavedGame) {
        settings.putString(key(game.id), game.encode())
        val ids = ids()
        if (game.id !in ids) setIds(ids + game.id)
    }

    override fun delete(id: String) {
        settings.remove(key(id))
        setIds(ids() - id)
    }

    private fun ids(): List<String> = settings.getString(IDS_KEY, "").split(",").filter { it.isNotEmpty() }

    private fun setIds(ids: List<String>) = settings.putString(IDS_KEY, ids.joinToString(","))

    private fun key(id: String) = "saved_game_$id"

    private companion object {
        const val IDS_KEY = "saved_game_ids"
    }
}
