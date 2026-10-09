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

import com.russhwolf.settings.MapSettings
import gc.david.dipole.game.GameMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsSavedGamesRepositoryTest {

    private val settings = MapSettings()
    private val repository = SettingsSavedGamesRepository(settings)

    private fun game(id: String, savedAt: Long) = SavedGame(id, "Game $id", savedAt, GameMode.TwoPlayers, emptyList())

    @Test
    fun listsSavesNewestFirst() {
        repository.save(game("a", 1))
        repository.save(game("b", 3))
        repository.save(game("c", 2))
        assertEquals(listOf("b", "c", "a"), repository.list().map { it.id })
    }

    @Test
    fun savingTheSameIdReplacesIt() {
        repository.save(game("a", 1))
        repository.save(game("a", 1).copy(name = "Renamed"))
        assertEquals(listOf("Renamed"), repository.list().map { it.name })
    }

    @Test
    fun deleteRemovesTheSave() {
        repository.save(game("a", 1))
        repository.save(game("b", 2))
        repository.delete("a")
        assertEquals(listOf("b"), repository.list().map { it.id })
        assertTrue(settings.keys.none { it.endsWith("_a") })
    }

    @Test
    fun corruptedSavesAreSkipped() {
        repository.save(game("a", 1))
        settings.putString("saved_game_a", "garbage")
        assertTrue(repository.list().isEmpty())
    }
}
