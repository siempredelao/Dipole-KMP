package dev.siempredelao.dipole.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun SaveGameDialogPreview() {
    DipoleTheme {
        SaveGameDialog(defaultName = "30 Sep 2026 21:42", onSave = {}, onDismiss = {})
    }
}
