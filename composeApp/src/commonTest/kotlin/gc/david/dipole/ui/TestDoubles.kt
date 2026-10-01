package gc.david.dipole.ui

import gc.david.dipole.appearance.AppearanceMode
import gc.david.dipole.appearance.BoardTheme
import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.Player
import gc.david.dipole.saves.GamePreferences
import gc.david.dipole.saves.SavedGame
import gc.david.dipole.saves.SavedGamesRepository

/** In-memory preferences, shared by the ViewModels under test like the real ones are. */
class FakeGamePreferences : GamePreferences {
    override var lastDifficulty = Difficulty.Medium
    override var lastSide = Player.White
    override var soundOn = true
    override var appearanceMode = AppearanceMode.System
    override var boardTheme = BoardTheme.Wood
    override var tutorialSeen = false
}

class FakeSavedGamesRepository : SavedGamesRepository {
    private val games = mutableMapOf<String, SavedGame>()
    override fun list() = games.values.sortedByDescending { it.savedAtEpochMillis }
    override fun save(game: SavedGame) { games[game.id] = game }
    override fun delete(id: String) { games.remove(id) }
}
