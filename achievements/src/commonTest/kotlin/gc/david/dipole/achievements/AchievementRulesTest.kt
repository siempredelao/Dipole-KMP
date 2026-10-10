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

import gc.david.dipole.achievements.Achievement.AiImpostor
import gc.david.dipole.achievements.Achievement.BigCapture
import gc.david.dipole.achievements.Achievement.BothSides
import gc.david.dipole.achievements.Achievement.DipoleMaster
import gc.david.dipole.achievements.Achievement.FirstVictory
import gc.david.dipole.achievements.Achievement.Flawless
import gc.david.dipole.achievements.Achievement.FriendlyMatch
import gc.david.dipole.achievements.Achievement.Grandmaster
import gc.david.dipole.achievements.Achievement.NoHelpNeeded
import gc.david.dipole.achievements.Achievement.Regular
import gc.david.dipole.achievements.Achievement.RisingPlayer
import gc.david.dipole.achievements.Achievement.SwiftVictory
import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.Direction
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.GameSession
import gc.david.dipole.game.GameSessions
import gc.david.dipole.game.GameState
import gc.david.dipole.game.Move
import gc.david.dipole.game.Player
import gc.david.dipole.game.Square
import gc.david.dipole.game.Stack
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AchievementRulesTest {

    /** White, one checker each side: White's capture wins at once. */
    private val oneMoveWin = Move(Square(2, 2), Direction.NorthEast, 1)

    private fun game(
        mode: GameMode = GameMode.VsComputer,
        difficulty: Difficulty = Difficulty.Easy,
        humanSide: Player = Player.White,
        toMove: Player = Player.White,
        board: Map<Square, Stack> = mapOf(Square(2, 2) to Stack(Player.White, 1), Square(3, 3) to Stack(Player.Black, 1)),
        moves: List<Move> = listOf(oneMoveWin),
    ): GameSession = moves.fold(GameSessions.new(mode, difficulty, GameState(board, toMove), humanSide), GameSessions::play)

    private fun earned(
        session: GameSession,
        usedHint: Boolean = false,
        usedUndo: Boolean = false,
        hintedMoves: Set<Int> = emptySet(),
        wins: Int = 1,
    ) = AchievementRules.earned(FinishedGame(session, usedHint, usedUndo, hintedMoves), PlayerTotals(wins))

    @Test
    fun anUnfinishedGameEarnsNothing() {
        assertEquals(emptySet(), earned(game(moves = emptyList())))
    }

    @Test
    fun anEasyWinEarnsTheBasics() {
        assertEquals(setOf(FirstVictory, Flawless, SwiftVictory), earned(game()))
    }

    @Test
    fun harderDifficultiesEarnMore() {
        assertTrue(RisingPlayer in earned(game(difficulty = Difficulty.Medium)))
        assertFalse(Grandmaster in earned(game(difficulty = Difficulty.Medium)))
        assertTrue(setOf(RisingPlayer, Grandmaster, NoHelpNeeded).all { it in earned(game(difficulty = Difficulty.Hard)) })
    }

    @Test
    fun noHelpNeededMeansNoHintAndNoUndo() {
        val hard = game(difficulty = Difficulty.Hard)
        assertFalse(NoHelpNeeded in earned(hard, usedHint = true))
        assertFalse(NoHelpNeeded in earned(hard, usedUndo = true))
    }

    @Test
    fun winningAsBlackEarnsBothSides() {
        val asBlack = game(
            humanSide = Player.Black,
            toMove = Player.Black,
            moves = listOf(Move(Square(3, 3), Direction.SouthWest, 1)),
        )
        assertTrue(BothSides in earned(asBlack))
    }

    @Test
    fun aLossEarnsNoVictory() {
        val lost = game(humanSide = Player.Black)
        assertEquals(emptySet(), earned(lost))
    }

    @Test
    fun losingACheckerBreaksFlawless() {
        // Black takes a White checker, then White wins.
        val session = game(
            toMove = Player.Black,
            board = mapOf(
                Square(3, 3) to Stack(Player.White, 1),
                Square(2, 2) to Stack(Player.White, 1),
                Square(4, 4) to Stack(Player.Black, 1),
            ),
            moves = listOf(Move(Square(4, 4), Direction.SouthWest, 1), oneMoveWin),
        )
        val earned = earned(session, hintedMoves = setOf(1))
        assertTrue(FirstVictory in earned)
        assertFalse(Flawless in earned)
    }

    @Test
    fun aBigCaptureCountsEvenInALoss() {
        val big = game(
            board = mapOf(Square(0, 0) to Stack(Player.White, 6), Square(6, 6) to Stack(Player.Black, 6)),
            moves = listOf(Move(Square(0, 0), Direction.NorthEast, 6)),
        )
        assertTrue(BigCapture in earned(big))
        val fiveOnly = game(
            board = mapOf(Square(0, 0) to Stack(Player.White, 5), Square(5, 5) to Stack(Player.Black, 5)),
            moves = listOf(Move(Square(0, 0), Direction.NorthEast, 5)),
        )
        assertFalse(BigCapture in earned(fiveOnly))
    }

    @Test
    fun aTwoPlayerGameOnlyEarnsFriendlyMatch() {
        assertEquals(setOf(FriendlyMatch), earned(game(mode = GameMode.TwoPlayers, difficulty = Difficulty.Hard)))
    }

    @Test
    fun aiImpostorNeedsEveryHumanMoveHinted() {
        val session = game(
            toMove = Player.Black,
            board = mapOf(
                Square(3, 3) to Stack(Player.White, 1),
                Square(2, 2) to Stack(Player.White, 1),
                Square(4, 4) to Stack(Player.Black, 1),
            ),
            moves = listOf(Move(Square(4, 4), Direction.SouthWest, 1), oneMoveWin),
        )
        // Move 0 was the computer's; only the human's move 1 has to be hinted.
        assertTrue(AiImpostor in earned(session, usedHint = true, hintedMoves = setOf(1)))
        assertFalse(AiImpostor in earned(session, usedHint = true, hintedMoves = emptySet()))
    }

    @Test
    fun winCountersCompleteTheIncrementalOnes() {
        assertFalse(Regular in earned(game(), wins = 9))
        assertTrue(Regular in earned(game(), wins = 10))
        assertFalse(DipoleMaster in earned(game(), wins = 49))
        assertTrue(DipoleMaster in earned(game(), wins = 50))
    }

    @Test
    fun progressIsCappedAtEachAchievementsSteps() {
        assertEquals(mapOf(Regular to 10, DipoleMaster to 12), AchievementRules.progress(PlayerTotals(12)))
    }
}
