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
