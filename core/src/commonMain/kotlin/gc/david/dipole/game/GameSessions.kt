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

package gc.david.dipole.game

/** Starts, plays, undoes and replays [GameSession]s, and answers questions about whose turn it is. */
object GameSessions {

    fun new(
        mode: GameMode,
        difficulty: Difficulty = Difficulty.Medium,
        initial: GameState = DipoleRules.initial(),
        humanSide: Player = Player.White,
    ): GameSession = GameSession(mode, difficulty, humanSide, emptyList(), listOf(initial))

    /** Replays [moves] from the starting position, or returns null if any of them is illegal. */
    fun replay(
        mode: GameMode,
        moves: List<Move>,
        difficulty: Difficulty = Difficulty.Medium,
        humanSide: Player = Player.White,
        initial: GameState = DipoleRules.initial(),
    ): GameSession? =
        moves.fold(new(mode, difficulty, initial, humanSide)) { session, move ->
            if (!DipoleRules.isLegal(session.state, move)) return null
            play(session, move)
        }

    fun play(session: GameSession, move: Move): GameSession =
        session.copy(moves = session.moves + move, positions = session.positions + DipoleRules.play(session.state, move))

    fun isComputerTurn(session: GameSession): Boolean =
        session.mode == GameMode.VsComputer && session.state.toMove != session.humanSide &&
            !DipoleRules.isOver(session.state)

    /** True when the player who didn't just move had no legal move, so the mover goes again. */
    fun opponentSatOut(session: GameSession): Boolean {
        val positions = session.positions
        return positions.size > 1 && positions[positions.size - 2].toMove == session.state.toMove
    }

    fun canUndo(session: GameSession): Boolean = !isComputerTurn(session) && undoneMoveCount(session) != null

    /**
     * Takes back the last move. Against the computer it goes back to the last position where the
     * human was to move, so the computer's reply is undone too. Returns [session] unchanged when
     * there is nothing to undo, for example when only the computer's opening move has been played.
     */
    fun undo(session: GameSession): GameSession {
        val count = undoneMoveCount(session) ?: return session

        return session.copy(moves = session.moves.take(count), positions = session.positions.take(count + 1))
    }

    /** How many moves are left after an undo, or null when there is nothing to undo. */
    private fun undoneMoveCount(session: GameSession): Int? {
        if (session.moves.isEmpty()) return null

        var count = session.moves.size - 1
        if (session.mode == GameMode.VsComputer) {
            while (count > 0 && session.positions[count].toMove != session.humanSide) count--

            if (session.positions[count].toMove != session.humanSide) return null
        }

        return count
    }
}
