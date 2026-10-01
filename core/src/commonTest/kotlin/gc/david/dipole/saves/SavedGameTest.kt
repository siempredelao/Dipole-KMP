package gc.david.dipole.saves

import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.DipoleRules
import gc.david.dipole.game.Direction
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.GameSession
import gc.david.dipole.game.Move
import gc.david.dipole.game.Player
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

class SavedGameTest {

    private val savedAt = Instant.fromEpochMilliseconds(1_790_000_000_000) // 21 Sep 2026 14:13:20 UTC
    private val session = GameSession.new(GameMode.VsComputer, Difficulty.Hard)
        .play(Move(DipoleRules.WHITE_START, Direction.NorthEast, 3))
        .play(Move(DipoleRules.BLACK_START, Direction.SouthWest, 2))

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
    fun theHumansSideIsSaved() {
        val asBlack = GameSession.new(GameMode.VsComputer, humanSide = Player.Black)
            .play(Move(DipoleRules.WHITE_START, Direction.NorthEast, 3))
        val saved = SavedGame.of(asBlack, "As Black", savedAt)
        assertEquals(saved, SavedGame.decode(saved.id, saved.encode()))
        assertEquals(Player.Black, saved.toSession()?.humanSide)
    }

    @Test
    fun savesFromBeforeSideChoiceLoadAsWhite() {
        val v2 = "dipole-save 2\nVsComputer\nHard\n1790000000000\nOld game\n0,2,NorthEast,3"
        val saved = SavedGame.decode("old", v2)
        assertEquals(Player.White, saved?.humanSide)
        assertEquals(Difficulty.Hard, saved?.difficulty)
        assertEquals("Old game", saved?.name)
    }

    @Test
    fun savesFromBeforeDifficultyLevelsLoadAsMedium() {
        val v1 = "dipole-save 1\nVsComputer\n1790000000000\nOld game\n0,2,NorthEast,3"
        val saved = SavedGame.decode("old", v1)
        assertEquals(Difficulty.Medium, saved?.difficulty)
        assertEquals(Player.White, saved?.humanSide)
        assertEquals("Old game", saved?.name)
        assertEquals(listOf(Move(DipoleRules.WHITE_START, Direction.NorthEast, 3)), saved?.moves)
    }

    @Test
    fun invalidTextIsRejected() {
        assertNull(SavedGame.decode("x", "not a save"))
        assertNull(SavedGame.decode("x", "dipole-save 1\nChess\n0\nName\n"))
        assertNull(SavedGame.decode("x", "dipole-save 2\nVsComputer\nImpossible\n0\nName\n"))
        assertNull(SavedGame.decode("x", "dipole-save 3\nVsComputer\nHard\nRed\n0\nName\n"))
        assertNull(SavedGame.decode("x", "dipole-save 1\nTwoPlayers\n0\nName\n0,2,Up,3"))
    }

    @Test
    fun illegalMovesDoNotLoad() {
        val saved = SavedGame("x", "Broken", 0, GameMode.TwoPlayers, listOf(Move(DipoleRules.BLACK_START, Direction.South, 2)))
        assertNull(saved.toSession())
    }

    @Test
    fun namesAreKeptOnOneLineAndDefaultToTheDate() {
        assertEquals("a b", SavedGame.of(session, "a\nb", savedAt).name)
        assertEquals("21 Sep 2026 14:13", SavedGame.defaultName(savedAt, TimeZone.UTC))
    }
}
