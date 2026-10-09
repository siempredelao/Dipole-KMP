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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import gc.david.dipole.appearance.AppearanceMode
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.GameSessions

/** The game screen with the confetti frozen [seconds] into a celebration. */
@Composable
private fun CelebrationPreview(mode: AppearanceMode, seconds: Float) {
    DipoleTheme(mode = mode) {
        Box(Modifier.fillMaxSize()) {
            PreviewScreen(GameUiState(GameSessions.new(GameMode.VsComputer)))
            ConfettiOverlay(celebration = 1, celebrating = false, onStart = {}, previewSeconds = seconds)
        }
    }
}

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun ConfettiRisingPreview() = CelebrationPreview(AppearanceMode.Dark, seconds = 0.4f)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun ConfettiFallingPreview() = CelebrationPreview(AppearanceMode.Dark, seconds = 1.6f)

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun ConfettiLightPreview() = CelebrationPreview(AppearanceMode.Light, seconds = 1f)
