package gc.david.dipole.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import gc.david.dipole.resources.Res
import gc.david.dipole.resources.cancel
import gc.david.dipole.resources.save
import gc.david.dipole.resources.save_game_name
import gc.david.dipole.resources.save_game_title
import kotlin.time.Instant
import org.jetbrains.compose.resources.stringResource

/** Asks for a name for the save, pre-filled with the date and time it is [savedAt]. */
@Composable
fun SaveGameDialog(
    savedAt: Instant,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val defaultName = formatDate(savedAt)
    var name by remember(defaultName) { mutableStateOf(defaultName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.save_game_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(Res.string.save_game_name)) },
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(name.ifBlank { defaultName }) }) { Text(stringResource(Res.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) }
        },
    )
}
