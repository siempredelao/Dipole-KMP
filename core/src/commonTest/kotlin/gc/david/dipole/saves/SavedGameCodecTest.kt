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

import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.DipoleRules
import gc.david.dipole.game.Direction
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.Move
import gc.david.dipole.game.Player
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SavedGameCodecTest {

    private val game = SavedGame(
        id = "1790000000000-42",
        name = "My game",
        savedAtEpochMillis = 1_790_000_000_000,
        mode = GameMode.VsComputer,
        moves = listOf(Move(DipoleRules.WHITE_START, Direction.NorthEast, 3), Move(DipoleRules.BLACK_START, Direction.SouthWest, 2)),
        difficulty = Difficulty.Hard,
        humanSide = Player.Black,
    )

    /** Pins the stored format: changing it would make existing saves unreadable. */
    private val stored = "dipole-save 3\nVsComputer\nHard\nBlack\n1790000000000\nMy game\n0,2,NorthEast,3;7,3,SouthWest,2"

    @Test
    fun encodesTheCurrentFormat() {
        assertEquals(stored, SavedGameCodec.encode(game))
    }

    @Test
    fun decodesTheCurrentFormat() {
        assertEquals(game, SavedGameCodec.decode(game.id, stored))
    }

    @Test
    fun aGameWithoutMovesRoundTrips() {
        val fresh = game.copy(moves = emptyList())
        assertEquals(fresh, SavedGameCodec.decode(fresh.id, SavedGameCodec.encode(fresh)))
    }

    @Test
    fun savesFromBeforeSideChoiceLoadAsWhite() {
        val v2 = "dipole-save 2\nVsComputer\nHard\n1790000000000\nOld game\n0,2,NorthEast,3"
        val saved = SavedGameCodec.decode("old", v2)
        assertEquals(Player.White, saved?.humanSide)
        assertEquals(Difficulty.Hard, saved?.difficulty)
        assertEquals("Old game", saved?.name)
    }

    @Test
    fun savesFromBeforeDifficultyLevelsLoadAsMedium() {
        val v1 = "dipole-save 1\nVsComputer\n1790000000000\nOld game\n0,2,NorthEast,3"
        val saved = SavedGameCodec.decode("old", v1)
        assertEquals(Difficulty.Medium, saved?.difficulty)
        assertEquals(Player.White, saved?.humanSide)
        assertEquals("Old game", saved?.name)
        assertEquals(listOf(Move(DipoleRules.WHITE_START, Direction.NorthEast, 3)), saved?.moves)
    }

    @Test
    fun invalidTextIsRejected() {
        assertNull(SavedGameCodec.decode("x", "not a save"))
        assertNull(SavedGameCodec.decode("x", "dipole-save 1\nChess\n0\nName\n"))
        assertNull(SavedGameCodec.decode("x", "dipole-save 2\nVsComputer\nImpossible\n0\nName\n"))
        assertNull(SavedGameCodec.decode("x", "dipole-save 3\nVsComputer\nHard\nRed\n0\nName\n"))
        assertNull(SavedGameCodec.decode("x", "dipole-save 1\nTwoPlayers\n0\nName\n0,2,Up,3"))
    }
}
