package dev.siempredelao.dipole.ui

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

/** Asks which mode to play before starting a new game. Cancelling keeps the current game. */
@Composable
fun NewGameDialog(
    onModeChosen: (Opponent) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New game") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onModeChosen(Opponent.Computer) }, modifier = Modifier.fillMaxWidth()) {
                    Text("vs Computer")
                }
                Button(onClick = { onModeChosen(Opponent.Human) }, modifier = Modifier.fillMaxWidth()) {
                    Text("2 Players")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
