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
