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
