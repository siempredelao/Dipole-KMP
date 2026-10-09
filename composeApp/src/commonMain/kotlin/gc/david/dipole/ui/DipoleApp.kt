package gc.david.dipole.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import gc.david.dipole.di.appModules
import gc.david.dipole.review.ReviewPrompter
import gc.david.dipole.review.ReviewTracker
import gc.david.dipole.saves.GamePreferences
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.dsl.koinConfiguration

/**
 * The app: starts Koin with [appModules], then the theme from the settings and navigation between
 * the game, the rules and the tutorial. Each screen gets its own ViewModel from Koin.
 *
 * [reviewPrompter] shows the store's review popup; platforms without a store keep the default.
 */
@Composable
fun DipoleApp(reviewPrompter: ReviewPrompter = ReviewPrompter.None) {
    KoinApplication(configuration = koinConfiguration { modules(appModules) }) {
        DipoleNavigation(reviewPrompter)
    }
}

@Composable
private fun DipoleNavigation(reviewPrompter: ReviewPrompter) {
    val preferences = koinInject<GamePreferences>()
    val reviews = koinInject<ReviewTracker>()
    // Saveable, so a rotation doesn't count as another launch.
    rememberSaveable { reviews.onAppLaunched(); true }
    val settingsViewModel = koinViewModel<SettingsViewModel>()
    val settings by settingsViewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val start: Any = remember { if (TutorialViewModel.opensOnLaunch(preferences)) TutorialRoute else GameRoute }

    DipoleTheme(settings.appearanceMode, settings.boardTheme) {
        NavHost(navController, startDestination = start) {
            composable<GameRoute> {
                val viewModel = koinViewModel<GameViewModel>()
                val uiState by viewModel.uiState.collectAsState()
                LaunchedEffect(uiState.askForReview) {
                    if (!uiState.askForReview) return@LaunchedEffect
                    // The tracker decides whether it's time to ask.
                    reviews.onHappyMoment(reviewPrompter)
                    viewModel.onAction(GameAction.ReviewAsked)
                }
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
                val viewModel = koinViewModel<TutorialViewModel>()
                val uiState by viewModel.uiState.collectAsState()
                LaunchedEffect(uiState.finished) {
                    if (uiState.finished) navController.leaveTutorial()
                }
                TutorialScreen(uiState, viewModel::onAction)
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
