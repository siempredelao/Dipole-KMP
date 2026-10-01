package gc.david.dipole.saves

import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.DipoleRules
import gc.david.dipole.game.Direction
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.GameSessions
import gc.david.dipole.game.Move
import gc.david.dipole.game.Player
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

class SavedGameTest {

    private val savedAt = Instant.fromEpochMilliseconds(1_790_000_000_000) // 21 Sep 2026 14:13:20 UTC
    private val session = GameSessions.replay(
        GameMode.VsComputer,
        listOf(Move(DipoleRules.WHITE_START, Direction.NorthEast, 3), Move(DipoleRules.BLACK_START, Direction.SouthWest, 2)),
        Difficulty.Hard,
    )!!

    @Test
    fun encodeAndDecodeRoundTrip() {
        val saved = SavedGame.of(session, "My game", savedAt)
        assertEquals(saved, SavedGameCodec.decode(saved.id, SavedGameCodec.encode(saved)))
    }

    @Test
    fun emptyGameRoundTrips() {
        val saved = SavedGame.of(GameSessions.new(GameMode.TwoPlayers), "Fresh", savedAt)
        assertEquals(saved, SavedGameCodec.decode(saved.id, SavedGameCodec.encode(saved)))
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
        val asBlack = GameSessions.play(
            GameSessions.new(GameMode.VsComputer, humanSide = Player.Black),
            Move(DipoleRules.WHITE_START, Direction.NorthEast, 3),
        )
        val saved = SavedGame.of(asBlack, "As Black", savedAt)
        assertEquals(saved, SavedGameCodec.decode(saved.id, SavedGameCodec.encode(saved)))
        assertEquals(Player.Black, saved.toSession()?.humanSide)
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
