package gc.david.dipole.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import gc.david.dipole.appearance.AppearanceMode
import gc.david.dipole.appearance.BoardTheme

@Preview
@Composable
private fun AppearanceDialogPreview() {
    DipoleTheme(mode = AppearanceMode.Dark) {
        AppearanceDialog(
            mode = AppearanceMode.System,
            board = BoardTheme.HighContrast,
            onModeChosen = {},
            onBoardChosen = {},
            onDismiss = {},
        )
    }
}
