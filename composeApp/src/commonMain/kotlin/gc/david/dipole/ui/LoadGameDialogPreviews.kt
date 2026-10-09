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
import gc.david.dipole.game.DipoleRules
import gc.david.dipole.game.Direction
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.Move
import gc.david.dipole.saves.SavedGame

@Preview
@Composable
private fun LoadGameDialogPreview() {
    val opening = Move(DipoleRules.WHITE_START, Direction.NorthEast, 3)
    DipoleTheme {
        LoadGameDialog(
            savedGames = listOf(
                SavedGame("1", "Close game", 1_790_000_000_000, GameMode.VsComputer, listOf(opening)),
                SavedGame("2", "With Ana", 1_789_000_000_000, GameMode.TwoPlayers, emptyList()),
            ),
            onLoad = {},
            onDelete = {},
            onDismiss = {},
        )
    }
}
