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

import gc.david.dipole.game.DipoleRules
import gc.david.dipole.game.GameSessions
import gc.david.dipole.game.MoveKind
import gc.david.dipole.game.Player

/** Works out what the game screen shows from the session and the selected stack. */
object GameUiStateMapper {

    /** [uiState] with every field derived from its session and selection filled in. */
    fun map(uiState: GameUiState): GameUiState {
        val session = uiState.session
        val state = session.state
        val computerThinking = GameSessions.isComputerTurn(session)
        val winner = DipoleRules.winner(state)
        val fromSelected = uiState.selected?.let { DipoleRules.legalMovesFrom(state, it) }.orEmpty()
        val (onBoard, offBoard) = fromSelected.partition { it.to.isOnBoard }

        return uiState.copy(
            movable = if (computerThinking) emptySet() else DipoleRules.legalMoves(state).map { it.from }.toSet(),
            targets = onBoard.associateBy { it.to },
            captureTargets = onBoard.filter { DipoleRules.kindOf(state, it) is MoveKind.Capture }.map { it.to }.toSet(),
            bearOffs = offBoard,
            // A finished game stays finished: Undo goes away with the win.
            canUndo = winner == null && GameSessions.canUndo(session),
            canHint = !computerThinking && winner == null,
            status = GameStatus(
                winner = winner,
                computerThinking = computerThinking,
                satOut = if (GameSessions.opponentSatOut(session)) state.toMove.opponent else null,
            ),
            checkersOnBoard = Player.entries.associateWith { DipoleRules.checkersOf(state, it) },
            removedCheckers = Player.entries.associateWith { DipoleRules.removedCheckersOf(state, it) },
        )
    }
}
