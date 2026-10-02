package gc.david.dipole.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.Player
import gc.david.dipole.resources.Res
import gc.david.dipole.resources.back
import gc.david.dipole.resources.cancel
import gc.david.dipole.resources.mode_vs_computer
import gc.david.dipole.resources.side_title
import gc.david.dipole.resources.side_white_first
import org.jetbrains.compose.resources.stringResource

/**
 * Second step of New game against the computer: pick a [side], then a difficulty, which starts the
 * game. [suggested] (last time's pick) is highlighted.
 */
@Composable
fun DifficultyDialog(
    suggested: Difficulty,
    side: Player,
    onSideChosen: (Player) -> Unit,
    onDifficultyChosen: (Difficulty) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.mode_vs_computer)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.side_title), style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Player.entries.forEach { player ->
                        FilterChip(
                            selected = player == side,
                            onClick = { onSideChosen(player) },
                            label = { Text(stringResource(player.label)) },
                        )
                    }
                }
                Text(stringResource(Res.string.side_white_first), style = MaterialTheme.typography.bodySmall)

                HorizontalDivider(Modifier.padding(vertical = 4.dp))

                Difficulty.entries.forEach { difficulty ->
                    val label = "${stringResource(difficulty.label)} · ${stringResource(difficulty.description)}"
                    if (difficulty == suggested) {
                        Button(onClick = { onDifficultyChosen(difficulty) }, modifier = Modifier.fillMaxWidth()) {
                            Text(label)
                        }
                    } else {
                        OutlinedButton(onClick = { onDifficultyChosen(difficulty) }, modifier = Modifier.fillMaxWidth()) {
                            Text(label)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) }
        },
        dismissButton = {
            TextButton(onClick = onBack) { Text(stringResource(Res.string.back)) }
        },
    )
}
