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
        ids().mapNotNull { id -> settings.getStringOrNull(key(id))?.let { SavedGameCodec.decode(id, it) } }
            .sortedByDescending { it.savedAtEpochMillis }

    override fun save(game: SavedGame) {
        settings.putString(key(game.id), SavedGameCodec.encode(game))
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
