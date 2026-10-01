package gc.david.dipole.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gc.david.dipole.game.Difficulty

/** Second step of New game against the computer. [suggested] (last time's pick) is highlighted. */
@Composable
fun DifficultyDialog(
    suggested: Difficulty,
    onDifficultyChosen: (Difficulty) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("vs Computer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Difficulty.entries.forEach { difficulty ->
                    val label = "${difficulty.label} · ${difficulty.description}"
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
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        dismissButton = {
            TextButton(onClick = onBack) { Text("Back") }
        },
    )
}

val Difficulty.label: String
    get() = when (this) {
        Difficulty.Easy -> "Easy"
        Difficulty.Medium -> "Medium"
        Difficulty.Hard -> "Hard"
    }

private val Difficulty.description: String
    get() = when (this) {
        Difficulty.Easy -> "makes mistakes"
        Difficulty.Medium -> "plays solidly"
        Difficulty.Hard -> "thinks ahead"
    }
