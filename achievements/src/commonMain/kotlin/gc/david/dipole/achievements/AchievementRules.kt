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


package gc.david.dipole.achievements

import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.DipoleRules
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.MoveKind
import gc.david.dipole.game.Player

/** Which achievements a finished game earns. Pure: the stores ignore an achievement unlocked twice. */
object AchievementRules {

    const val BIG_CAPTURE = 6
    const val SWIFT_VICTORY_MOVES = 30

    /**
     * The achievements [game] earns, given the [totals] that already include it. The incremental ones
     * ([Achievement.steps]) are in the set once complete; [progress] reports them along the way.
     */
    fun earned(game: FinishedGame, totals: PlayerTotals): Set<Achievement> = buildSet {
        val session = game.session
        val winner = DipoleRules.winner(session.state) ?: return@buildSet

        if (session.mode == GameMode.TwoPlayers) {
            add(Achievement.FriendlyMatch)
            return@buildSet
        }

        val human = session.humanSide
        if (biggestCapture(game, human) >= BIG_CAPTURE) add(Achievement.BigCapture)
        if (winner != human) return@buildSet

        val humanMoves = humanMoves(game, human)
        add(Achievement.FirstVictory)
        if (session.difficulty >= Difficulty.Medium) add(Achievement.RisingPlayer)
        if (session.difficulty == Difficulty.Hard) add(Achievement.Grandmaster)
        if (human == Player.Black) add(Achievement.BothSides)
        if (capturedCheckers(game, human) == 0) add(Achievement.Flawless)
        if (session.difficulty == Difficulty.Hard && !game.usedHint && !game.usedUndo) add(Achievement.NoHelpNeeded)
        if (humanMoves.size <= SWIFT_VICTORY_MOVES) add(Achievement.SwiftVictory)
        if (humanMoves.all { it in game.hintedMoves }) add(Achievement.AiImpostor)
        for (achievement in listOf(Achievement.Regular, Achievement.DipoleMaster)) {
            if (totals.winsVsComputer >= achievement.steps!!) add(achievement)
        }
    }

    /** How far along the incremental achievements are, capped at their step count. */
    fun progress(totals: PlayerTotals): Map<Achievement, Int> =
        Achievement.entries.mapNotNull { a -> a.steps?.let { a to minOf(totals.winsVsComputer, it) } }.toMap()

    /** Indexes in the session's moves of the moves [player] made. */
    private fun humanMoves(game: FinishedGame, player: Player): List<Int> =
        game.session.moves.indices.filter { game.session.positions[it].toMove == player }

    /** The biggest enemy stack [player] took in one move. */
    private fun biggestCapture(game: FinishedGame, player: Player): Int =
        captures(game).filter { (mover, _) -> mover == player }.maxOfOrNull { it.second } ?: 0

    /** How many of [player]'s checkers the opponent captured. */
    private fun capturedCheckers(game: FinishedGame, player: Player): Int =
        captures(game).filter { (mover, _) -> mover == player.opponent }.sumOf { it.second }

    /** Every capture in the game, as the player who made it and the checkers taken. */
    private fun captures(game: FinishedGame): List<Pair<Player, Int>> =
        game.session.moves.mapIndexedNotNull { i, move ->
            val before = game.session.positions[i]
            (DipoleRules.kindOf(before, move) as? MoveKind.Capture)?.let { before.toMove to it.captured }
        }
}
