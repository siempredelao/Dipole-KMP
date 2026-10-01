package gc.david.dipole.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kotlin.time.Instant

@Preview
@Composable
private fun SaveGameDialogPreview() {
    DipoleTheme {
        SaveGameDialog(savedAt = Instant.fromEpochMilliseconds(1_790_000_000_000), onSave = {}, onDismiss = {})
    }
}
