package gc.david.dipole.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun NewGameDialogPreview() {
    DipoleTheme {
        NewGameDialog(onModeChosen = {}, onDismiss = {})
    }
}
