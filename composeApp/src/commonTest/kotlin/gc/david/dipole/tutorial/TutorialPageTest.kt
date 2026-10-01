package gc.david.dipole.tutorial

import gc.david.dipole.game.MoveKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TutorialPageTest {

    @Test
    fun everyExampleIsLegalFromTheSelectedStack() {
        TutorialPage.entries.forEach { page ->
            page.examples.forEach { move ->
                assertEquals(page.selected, move.from, "$page $move")
                assertTrue(page.position.isLegal(move), "$page $move")
            }
        }
    }

    @Test
    fun movingShowsEveryDistanceUpToTheStackSize() {
        assertEquals(listOf(1, 2, 3, 4), TutorialPage.Moving.examples.map { it.count })
    }

    @Test
    fun eachPageShowsTheKindOfMoveItExplains() {
        assertTrue(TutorialPage.PlainMoves.exampleKinds.any { it is MoveKind.Merge })
        assertTrue(TutorialPage.PlainMoves.exampleKinds.any { it is MoveKind.Step })
        assertTrue(TutorialPage.Captures.exampleKinds.all { it is MoveKind.Capture })
        assertTrue(TutorialPage.OffBoard.exampleKinds.any { it is MoveKind.BearOff })
    }

    @Test
    fun theCaptureExampleIncludesABackwardsCapture() {
        assertTrue(TutorialPage.Captures.examples.any { !it.direction.isForwardFor(TutorialPage.Captures.position.toMove) })
    }

    @Test
    fun blackSitsOutInTheSittingOutPage() {
        val position = TutorialPage.SittingOut.position
        assertTrue(position.legalMoves().isEmpty())
    }

    @Test
    fun theTipsHintIsALegalCapture() {
        val page = TutorialPage.Tips
        assertTrue(page.position.legalMovesFrom(page.selected!!).any { it.to == page.hinted })
    }
}
