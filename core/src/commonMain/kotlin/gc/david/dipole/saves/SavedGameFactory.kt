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

import gc.david.dipole.game.DipoleRules
import gc.david.dipole.game.GameSession
import gc.david.dipole.game.Player
import gc.david.dipole.game.Square
import kotlin.random.Random
import kotlin.time.Clock

/** Creates a [SavedGame] from the game being played, with a new id and the time it was saved. */
class SavedGameFactory(
    private val random: Random,
    private val clock: Clock,
) {

    /** Saves [session] as [name], which is kept on one line and must not be blank. */
    fun create(session: GameSession, name: String): SavedGame {
        val oneLineName = name.lines().joinToString(" ").trim()
        require(oneLineName.isNotEmpty()) { "A saved game needs a name" }
        val savedAt = clock.now().toEpochMilliseconds()

        return SavedGame(
            id = "$savedAt-${random.nextInt(1_000_000)}",
            name = oneLineName,
            savedAtEpochMillis = savedAt,
            mode = session.mode,
            moves = session.moves,
            difficulty = session.difficulty,
            humanSide = session.humanSide,
            whiteStart = whiteStartOf(session),
        )
    }

    /** A game loaded from an old save keeps its old start, so it still loads once saved again. */
    private fun whiteStartOf(session: GameSession): Square =
        session.positions.first().board.entries
            .singleOrNull { it.value.owner == Player.White }
            ?.key
            ?: DipoleRules.WHITE_START
}
