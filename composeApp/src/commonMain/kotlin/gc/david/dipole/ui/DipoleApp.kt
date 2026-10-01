package gc.david.dipole.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import gc.david.dipole.saves.SettingsGamePreferences

/**
 * The app: the theme from the settings, and navigation between the game, the rules and the
 * tutorial. Each screen gets its own ViewModel, all sharing the same preferences.
 */
@Composable
fun DipoleApp() {
    val preferences = remember { SettingsGamePreferences() }
    val settingsViewModel = viewModel { SettingsViewModel(preferences) }
    val settings by settingsViewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val start: Any = remember { if (TutorialViewModel.opensOnLaunch(preferences)) TutorialRoute else GameRoute }

    DipoleTheme(settings.appearanceMode, settings.boardTheme) {
        NavHost(navController, startDestination = start) {
            composable<GameRoute> {
                val viewModel = viewModel { GameViewModel(preferences = preferences) }
                val uiState by viewModel.uiState.collectAsState()
                GameScreen(
                    uiState = uiState,
                    settings = settings,
                    onAction = viewModel::onAction,
                    onSettingsAction = settingsViewModel::onAction,
                    onRulesClick = { navController.navigate(RulesRoute) },
                )
            }
            composable<RulesRoute> {
                RulesScreen(
                    onBack = { navController.popBackStack() },
                    onShowTutorial = { navController.navigate(TutorialRoute) },
                )
            }
            composable<TutorialRoute> {
                val viewModel = viewModel { TutorialViewModel(preferences) }
                val uiState by viewModel.uiState.collectAsState()
                LaunchedEffect(uiState.finished) {
                    if (uiState.finished) navController.leaveTutorial()
                }
                TutorialScreen(uiState.page, viewModel::onAction)
            }
        }
    }
}

/**
 * Back to where the tutorial was opened from (the rules); on the first launch, when it is the
 * only screen, on to the game instead.
 */
private fun NavHostController.leaveTutorial() {
    if (previousBackStackEntry != null) {
        popBackStack()
    } else {
        navigate(GameRoute) { popUpTo<TutorialRoute> { inclusive = true } }
    }
}
