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

import com.russhwolf.settings.MapSettings
import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.Direction
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.GameSessions
import gc.david.dipole.game.GameState
import gc.david.dipole.game.Move
import gc.david.dipole.game.Player
import gc.david.dipole.game.Square
import gc.david.dipole.game.Stack
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AchievementTrackerTest {

    private class RecordingClient : AchievementsClient {
        override val isAvailable = true
        val unlocked = mutableListOf<Achievement>()
        val progress = mutableMapOf<Achievement, Int>()
        var signIn: (() -> Unit)? = null
        override fun start(onSignedIn: () -> Unit) { signIn = onSignedIn }
        override fun unlock(achievement: Achievement) { unlocked += achievement }
        override fun setProgress(achievement: Achievement, steps: Int) { progress[achievement] = steps }
        override fun showAchievements() = Unit
    }

    private val settings = MapSettings()
    private val store = SettingsAchievementStore(settings)
    private val tracker = AchievementTracker(store)
    private val client = RecordingClient()

    private fun finishedGame(mode: GameMode = GameMode.VsComputer, humanSide: Player = Player.White): FinishedGame {
        val start = GameState(mapOf(Square(2, 2) to Stack(Player.White, 1), Square(3, 3) to Stack(Player.Black, 1)), Player.White)
        val session = GameSessions.play(
            GameSessions.new(mode, Difficulty.Easy, start, humanSide),
            Move(Square(2, 2), Direction.NorthEast, 1),
        )
        return FinishedGame(session, usedHint = false, usedUndo = false, hintedMoves = emptySet())
    }

    @Test
    fun aWinIsCountedKeptAndReported() {
        tracker.attach(client)
        tracker.onGameFinished(finishedGame())

        assertEquals(1, store.totals.winsVsComputer)
        assertTrue(Achievement.FirstVictory in store.earned)
        assertTrue(Achievement.FirstVictory in client.unlocked)
        assertEquals(1, client.progress[Achievement.Regular])
    }

    @Test
    fun anAchievementIsReportedOnceAsItIsEarned() {
        tracker.attach(client)
        tracker.onGameFinished(finishedGame())
        tracker.onGameFinished(finishedGame())

        assertEquals(1, client.unlocked.count { it == Achievement.FirstVictory })
        assertEquals(2, store.totals.winsVsComputer)
    }

    @Test
    fun lossesAndTwoPlayerGamesDontCountAsWins() {
        tracker.onGameFinished(finishedGame(humanSide = Player.Black))
        tracker.onGameFinished(finishedGame(mode = GameMode.TwoPlayers))

        assertEquals(0, store.totals.winsVsComputer)
        assertEquals(setOf(Achievement.FriendlyMatch), store.earned)
    }

    @Test
    fun whatWasEarnedWithoutAClientIsSentOnceSignedIn() {
        tracker.onTutorialFinished()
        tracker.onGameFinished(finishedGame())
        assertTrue(client.unlocked.isEmpty())

        tracker.attach(client)
        assertTrue(client.unlocked.isEmpty())
        client.signIn!!()

        assertTrue(client.unlocked.containsAll(listOf(Achievement.FirstSteps, Achievement.FirstVictory)))
        assertEquals(1, client.progress[Achievement.DipoleMaster])
    }

    @Test
    fun aDetachedClientGetsNothing() {
        tracker.attach(client)
        tracker.detach(client)
        tracker.onTutorialFinished()

        assertTrue(client.unlocked.isEmpty())
        assertTrue(Achievement.FirstSteps in store.earned)
    }

    @Test
    fun theStoreKeepsItsOwnKeys() {
        tracker.onTutorialFinished()
        tracker.onGameFinished(finishedGame())

        assertTrue(settings.keys.all { it.startsWith("achievements_") })
        assertEquals(store.earned, SettingsAchievementStore(settings).earned)
    }
}
