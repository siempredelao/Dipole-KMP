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

package gc.david.dipole.screenshots

import gc.david.dipole.game.Player
import gc.david.dipole.game.Square
import gc.david.dipole.ui.GameUiStateMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/** Checks the screenshot games still show what their screenshots are meant to show. */
class StoreScreenshotStatesTest {

    @Test
    fun gameShowsACaptureWithUndoOn() {
        val uiState = GameUiStateMapper.map(StoreScreenshotStates.game)

        assertEquals(setOf(Square(4, 4)), uiState.captureTargets)
        assertTrue(uiState.targets.size > 1)
        assertTrue(uiState.canUndo)
        assertEquals(Player.White, uiState.state.toMove)
        assertNotNull(uiState.session.lastMove)
    }

    @Test
    fun hintIsTheCaptureAndTheBestMove() {
        val hint = assertNotNull(StoreScreenshotStates.hint().hint)

        assertEquals(Square(4, 4), hint.move.to)
        assertFalse(hint.betterMoveElsewhere)
    }

    @Test
    fun wonGameIsAWinForTheHuman() {
        val uiState = GameUiStateMapper.map(StoreScreenshotStates.won)

        assertEquals(Player.White, uiState.status.winner)
        assertEquals(uiState.session.humanSide, uiState.status.winner)
    }
}
