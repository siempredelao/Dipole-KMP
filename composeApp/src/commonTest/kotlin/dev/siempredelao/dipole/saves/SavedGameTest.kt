package dev.siempredelao.dipole.saves

import dev.siempredelao.dipole.game.Difficulty
import dev.siempredelao.dipole.game.Direction
import dev.siempredelao.dipole.game.GameMode
import dev.siempredelao.dipole.game.GameSession
import dev.siempredelao.dipole.game.GameState
import dev.siempredelao.dipole.game.Move
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

class SavedGameTest {

    private val savedAt = Instant.fromEpochMilliseconds(1_790_000_000_000) // 21 Sep 2026 14:13:20 UTC
    private val session = GameSession.new(GameMode.VsComputer, Difficulty.Hard)
        .play(Move(GameState.WHITE_START, Direction.NorthEast, 3))
        .play(Move(GameState.BLACK_START, Direction.SouthWest, 2))

    @Test
    fun encodeAndDecodeRoundTrip() {
        val saved = SavedGame.of(session, "My game", savedAt)
        assertEquals(saved, SavedGame.decode(saved.id, saved.encode()))
    }

    @Test
    fun emptyGameRoundTrips() {
        val saved = SavedGame.of(GameSession.new(GameMode.TwoPlayers), "Fresh", savedAt)
        assertEquals(saved, SavedGame.decode(saved.id, saved.encode()))
    }

    @Test
    fun loadingReplaysTheGame() {
        val restored = SavedGame.of(session, "My game", savedAt).toSession()
        assertEquals(session.state, restored?.state)
        assertEquals(GameMode.VsComputer, restored?.mode)
        assertEquals(Difficulty.Hard, restored?.difficulty)
    }

    @Test
    fun savesFromBeforeDifficultyLevelsLoadAsMedium() {
        val v1 = "dipole-save 1\nVsComputer\n1790000000000\nOld game\n0,2,NorthEast,3"
        val saved = SavedGame.decode("old", v1)
        assertEquals(Difficulty.Medium, saved?.difficulty)
        assertEquals("Old game", saved?.name)
        assertEquals(listOf(Move(GameState.WHITE_START, Direction.NorthEast, 3)), saved?.moves)
    }

    @Test
    fun invalidTextIsRejected() {
        assertNull(SavedGame.decode("x", "not a save"))
        assertNull(SavedGame.decode("x", "dipole-save 1\nChess\n0\nName\n"))
        assertNull(SavedGame.decode("x", "dipole-save 2\nVsComputer\nImpossible\n0\nName\n"))
        assertNull(SavedGame.decode("x", "dipole-save 1\nTwoPlayers\n0\nName\n0,2,Up,3"))
    }

    @Test
    fun illegalMovesDoNotLoad() {
        val saved = SavedGame("x", "Broken", 0, GameMode.TwoPlayers, listOf(Move(GameState.BLACK_START, Direction.South, 2)))
        assertNull(saved.toSession())
    }

    @Test
    fun namesAreKeptOnOneLineAndDefaultToTheDate() {
        assertEquals("a b", SavedGame.of(session, "a\nb", savedAt).name)
        assertEquals("21 Sep 2026 14:13", SavedGame.defaultName(savedAt, TimeZone.UTC))
    }
}
