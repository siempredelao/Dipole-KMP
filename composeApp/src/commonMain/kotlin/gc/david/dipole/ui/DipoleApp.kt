package gc.david.dipole.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DipoleApp() {
    val viewModel = viewModel { DipoleViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    DipoleTheme(uiState.appearanceMode, uiState.boardTheme) {
        DipoleScreen(uiState, viewModel::onAction)
    }
}
