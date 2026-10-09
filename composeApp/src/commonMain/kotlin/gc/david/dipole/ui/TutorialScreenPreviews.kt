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

package gc.david.dipole.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import gc.david.dipole.appearance.AppearanceMode
import gc.david.dipole.tutorial.TutorialPage

@Composable
private fun TutorialPreview(page: TutorialPage, mode: AppearanceMode = AppearanceMode.Dark) {
    DipoleTheme(mode = mode) {
        TutorialScreen(TutorialUiState(page, captureTargets = TutorialViewModel.captureTargets()), onAction = {})
    }
}

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun TutorialGoalPreview() = TutorialPreview(TutorialPage.Goal)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun TutorialMovingPreview() = TutorialPreview(TutorialPage.Moving)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun TutorialPlainMovesPreview() = TutorialPreview(TutorialPage.PlainMoves)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun TutorialCapturesPreview() = TutorialPreview(TutorialPage.Captures)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun TutorialOffBoardPreview() = TutorialPreview(TutorialPage.OffBoard)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun TutorialSittingOutPreview() = TutorialPreview(TutorialPage.SittingOut)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun TutorialTipsPreview() = TutorialPreview(TutorialPage.Tips)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun TutorialLightPreview() = TutorialPreview(TutorialPage.Captures, AppearanceMode.Light)
