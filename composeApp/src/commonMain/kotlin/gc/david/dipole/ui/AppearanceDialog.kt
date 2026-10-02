package gc.david.dipole.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import gc.david.dipole.appearance.AppearanceMode
import gc.david.dipole.appearance.BoardTheme
import gc.david.dipole.resources.Res
import gc.david.dipole.resources.appearance
import gc.david.dipole.resources.appearance_board
import gc.david.dipole.resources.appearance_mode
import gc.david.dipole.resources.board_high_contrast
import gc.david.dipole.resources.board_marble
import gc.david.dipole.resources.board_slate
import gc.david.dipole.resources.board_tournament
import gc.david.dipole.resources.board_wood
import gc.david.dipole.resources.done
import gc.david.dipole.resources.mode_dark
import gc.david.dipole.resources.mode_light
import gc.david.dipole.resources.mode_system
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Picks light or dark mode and the board theme. Choices apply straight away. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppearanceDialog(
    mode: AppearanceMode,
    board: BoardTheme,
    onModeChosen: (AppearanceMode) -> Unit,
    onBoardChosen: (BoardTheme) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.appearance)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(Res.string.appearance_mode), style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppearanceMode.entries.forEach { option ->
                        FilterChip(
                            selected = option == mode,
                            onClick = { onModeChosen(option) },
                            label = { Text(stringResource(option.label)) },
                        )
                    }
                }

                Text(
                    stringResource(Res.string.appearance_board),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    BoardTheme.entries.forEach { option ->
                        BoardSwatch(option, selected = option == board, onClick = { onBoardChosen(option) })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.done)) }
        },
    )
}

/** A 2x2 corner of [theme]'s board with its name underneath; [selected] gets a thick outline. */
@Composable
private fun BoardSwatch(theme: BoardTheme, selected: Boolean, onClick: () -> Unit) {
    val colors = theme.colors
    val shape = RoundedCornerShape(8.dp)
    Column(
        Modifier.width(72.dp).clickable(role = Role.RadioButton, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Canvas(
            Modifier
                .size(48.dp)
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray,
                    shape = shape,
                )
                .padding(4.dp),
        ) {
            val half = Size(size.width / 2, size.height / 2)
            drawRect(colors.lightSquare, Offset.Zero, half)
            drawRect(colors.darkSquare, Offset(half.width, 0f), half)
            drawRect(colors.darkSquare, Offset(0f, half.height), half)
            drawRect(colors.lightSquare, Offset(half.width, half.height), half)
            // A target and a capture mark, so the highlight colours show too.
            drawCircle(colors.target, radius = half.width / 4, center = Offset(half.width * 1.5f, half.height / 2))
            drawCircle(colors.capture, radius = half.width / 4, center = Offset(half.width / 2, half.height * 1.5f))
        }

        Text(
            stringResource(theme.label),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
    }
}

private val AppearanceMode.label: StringResource
    get() = when (this) {
        AppearanceMode.System -> Res.string.mode_system
        AppearanceMode.Light -> Res.string.mode_light
        AppearanceMode.Dark -> Res.string.mode_dark
    }

val BoardTheme.label: StringResource
    get() = when (this) {
        BoardTheme.Wood -> Res.string.board_wood
        BoardTheme.Tournament -> Res.string.board_tournament
        BoardTheme.Slate -> Res.string.board_slate
        BoardTheme.Marble -> Res.string.board_marble
        BoardTheme.HighContrast -> Res.string.board_high_contrast
    }
