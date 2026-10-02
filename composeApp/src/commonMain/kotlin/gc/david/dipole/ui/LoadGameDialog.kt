package gc.david.dipole.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gc.david.dipole.game.GameMode
import gc.david.dipole.resources.Res
import gc.david.dipole.resources.cancel
import gc.david.dipole.resources.delete
import gc.david.dipole.resources.load_game_title
import gc.david.dipole.resources.mode_two_players
import gc.david.dipole.resources.mode_vs_computer
import gc.david.dipole.resources.moves
import gc.david.dipole.saves.SavedGame
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

/** Lists the saved games, newest first. Tap one to load it. */
@Composable
fun LoadGameDialog(
    savedGames: List<SavedGame>,
    onLoad: (SavedGame) -> Unit,
    onDelete: (SavedGame) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.load_game_title)) },
        text = {
            LazyColumn(Modifier.heightIn(max = 400.dp)) {
                items(savedGames, key = { it.id }) { game ->
                    SavedGameRow(game, onLoad = { onLoad(game) }, onDelete = { onDelete(game) })
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) }
        },
    )
}

@Composable
private fun SavedGameRow(game: SavedGame, onLoad: () -> Unit, onDelete: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onLoad).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(game.name, style = MaterialTheme.typography.bodyLarge)
            Text(details(game), style = MaterialTheme.typography.bodySmall)
        }

        TextButton(onClick = onDelete) { Text(stringResource(Res.string.delete)) }
    }
}

@Composable
private fun details(game: SavedGame): String {
    val mode = if (game.mode == GameMode.VsComputer) {
        "${stringResource(Res.string.mode_vs_computer)} (${stringResource(game.difficulty.label)})"
    } else {
        stringResource(Res.string.mode_two_players)
    }
    val moveCount = pluralStringResource(Res.plurals.moves, game.moves.size, game.moves.size)
    val date = formatDate(Instant.fromEpochMilliseconds(game.savedAtEpochMillis))

    return "$mode · $moveCount · $date"
}
