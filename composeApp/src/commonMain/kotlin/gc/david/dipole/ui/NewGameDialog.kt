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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gc.david.dipole.game.GameMode
import gc.david.dipole.resources.Res
import gc.david.dipole.resources.cancel
import gc.david.dipole.resources.mode_two_players
import gc.david.dipole.resources.mode_vs_computer
import gc.david.dipole.resources.new_game
import org.jetbrains.compose.resources.stringResource

/** Asks which mode to play before starting a new game. Cancelling keeps the current game. */
@Composable
fun NewGameDialog(
    onModeChosen: (GameMode) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.new_game)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onModeChosen(GameMode.VsComputer) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.mode_vs_computer))
                }

                Button(
                    onClick = { onModeChosen(GameMode.TwoPlayers) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.mode_two_players))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) }
        },
    )
}
