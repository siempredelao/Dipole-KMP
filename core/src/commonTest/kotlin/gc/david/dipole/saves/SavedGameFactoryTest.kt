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
import gc.david.dipole.game.GameSessions
import gc.david.dipole.game.Move
import gc.david.dipole.game.Player
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Clock
import kotlin.time.Instant

class SavedGameFactoryTest {

    private val clock = object : Clock {
        override fun now() = Instant.fromEpochMilliseconds(1_790_000_000_000)
    }
    private val opening = Move(DipoleRules.WHITE_START, Direction.NorthEast, 3)
    private val session = GameSessions.play(GameSessions.new(GameMode.VsComputer, Difficulty.Hard, humanSide = Player.Black), opening)

    private fun factory(seed: Int = 7) = SavedGameFactory(Random(seed), clock)

    @Test
    fun keepsWhatIsNeededToReplayTheGame() {
        val saved = factory().create(session, "My game")
        assertEquals("My game", saved.name)
        assertEquals(GameMode.VsComputer, saved.mode)
        assertEquals(listOf(opening), saved.moves)
        assertEquals(Difficulty.Hard, saved.difficulty)
        assertEquals(Player.Black, saved.humanSide)
        assertEquals(1_790_000_000_000, saved.savedAtEpochMillis)
    }

    @Test
    fun loadingReplaysTheSameGame() {
        val saved = factory().create(session, "My game")
        val restored = GameSessions.replay(saved.mode, saved.moves, saved.difficulty, saved.humanSide)
        assertEquals(session, restored)
    }

    @Test
    fun aGameFromAnOldSaveKeepsItsOldStartWhenSavedAgain() {
        val oldStart = DipoleRules.initial(DipoleRules.OLD_WHITE_START)
        val old = GameSessions.replay(GameMode.TwoPlayers, listOf(Move(DipoleRules.OLD_WHITE_START, Direction.NorthEast, 3)), initial = oldStart)!!
        val saved = factory().create(old, "Old")
        assertEquals(DipoleRules.OLD_WHITE_START, saved.whiteStart)
        assertEquals(old, GameSessions.replay(saved.mode, saved.moves, initial = DipoleRules.initial(saved.whiteStart)))
    }

    @Test
    fun idsComeFromTheTimeAndTheRandomSource() {
        val expected = "1790000000000-${Random(7).nextInt(1_000_000)}"
        assertEquals(expected, factory(seed = 7).create(session, "A").id)
    }

    @Test
    fun namesAreKeptOnOneLine() {
        assertEquals("a b", factory().create(session, "  a\nb ").name)
    }

    @Test
    fun aBlankNameIsRejected() {
        assertFailsWith<IllegalArgumentException> { factory().create(session, " \n ") }
    }
}
