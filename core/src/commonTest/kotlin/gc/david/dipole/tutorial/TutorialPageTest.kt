package gc.david.dipole.tutorial

import gc.david.dipole.game.DipoleRules
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
                assertTrue(DipoleRules.isLegal(page.position, move), "$page $move")
            }
        }
    }

    @Test
    fun movingShowsEveryDistanceUpToTheStackSize() {
        assertEquals(listOf(1, 2, 3, 4), TutorialPage.Moving.examples.map { it.count })
    }

    @Test
    fun eachPageShowsTheKindOfMoveItExplains() {
        assertTrue(exampleKinds(TutorialPage.PlainMoves).any { it is MoveKind.Merge })
        assertTrue(exampleKinds(TutorialPage.PlainMoves).any { it is MoveKind.Step })
        assertTrue(exampleKinds(TutorialPage.Captures).all { it is MoveKind.Capture })
        assertTrue(exampleKinds(TutorialPage.OffBoard).any { it is MoveKind.BearOff })
    }

    @Test
    fun theCaptureExampleIncludesABackwardsCapture() {
        assertTrue(TutorialPage.Captures.examples.any { !it.direction.isForwardFor(TutorialPage.Captures.position.toMove) })
    }

    @Test
    fun blackSitsOutInTheSittingOutPage() {
        val position = TutorialPage.SittingOut.position
        assertTrue(DipoleRules.legalMoves(position).isEmpty())
    }

    @Test
    fun theTipsHintIsALegalCapture() {
        val page = TutorialPage.Tips
        assertTrue(DipoleRules.legalMovesFrom(page.position, page.selected!!).any { it.to == page.hinted })
    }

    private fun exampleKinds(page: TutorialPage): List<MoveKind?> = page.examples.map { DipoleRules.kindOf(page.position, it) }
}
