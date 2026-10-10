/*
 * Copyright (c) 2026 David Aguiar Gonzalez
 *
 * Dipole, the game and its rules, was designed by Mark Steere
 * (Copyright (c) May 2007 by Mark Steere, https://www.marksteeregames.com).
 * This code, an implementation of the game, was developed by David Aguiar Gonzalez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gc.david.dipole.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import gc.david.dipole.achievements.AchievementTracker
import gc.david.dipole.achievements.AchievementsClient
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
 * [reviewPrompter] shows the store's review popup and [achievements] reports to the platform's game
 * service; platforms without them keep the defaults.
 */
@Composable
fun DipoleApp(
    reviewPrompter: ReviewPrompter = ReviewPrompter.None,
    achievements: AchievementsClient = AchievementsClient.None,
) {
    KoinApplication(configuration = koinConfiguration { modules(appModules) }) {
        DipoleNavigation(reviewPrompter, achievements)
    }
}

@Composable
private fun DipoleNavigation(reviewPrompter: ReviewPrompter, achievements: AchievementsClient) {
    val preferences = koinInject<GamePreferences>()
    val reviews = koinInject<ReviewTracker>()
    // Saveable, so a rotation doesn't count as another launch.
    rememberSaveable { reviews.onAppLaunched(); true }
    val achievementTracker = koinInject<AchievementTracker>()
    DisposableEffect(achievements) {
        achievementTracker.attach(achievements)
        onDispose { achievementTracker.detach(achievements) }
    }
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
                    onAchievementsClick = if (achievements.isAvailable) achievements::showAchievements else null,
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
