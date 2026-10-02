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
