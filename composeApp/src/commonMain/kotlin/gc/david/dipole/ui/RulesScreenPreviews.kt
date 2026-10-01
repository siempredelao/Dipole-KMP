package gc.david.dipole.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun RulesScreenPreview() {
    DipoleTheme {
        RulesScreen(onBack = {}, onShowTutorial = {})
    }
}
