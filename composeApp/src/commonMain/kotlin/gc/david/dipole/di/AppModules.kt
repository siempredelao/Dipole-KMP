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

package gc.david.dipole.di

import com.russhwolf.settings.Settings
import gc.david.dipole.achievements.AchievementStore
import gc.david.dipole.achievements.AchievementTracker
import gc.david.dipole.achievements.SettingsAchievementStore
import gc.david.dipole.game.ComputerPlayer
import gc.david.dipole.game.ComputerPlayerFactory
import gc.david.dipole.game.Difficulty
import gc.david.dipole.review.ReviewStore
import gc.david.dipole.review.ReviewTracker
import gc.david.dipole.review.SettingsReviewStore
import gc.david.dipole.saves.GamePreferences
import gc.david.dipole.saves.SavedGameFactory
import gc.david.dipole.saves.SavedGamesRepository
import gc.david.dipole.saves.SettingsGamePreferences
import gc.david.dipole.saves.SettingsSavedGamesRepository
import gc.david.dipole.ui.GameViewModel
import gc.david.dipole.ui.SettingsViewModel
import gc.david.dipole.ui.TutorialViewModel
import kotlin.random.Random
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** The computer player that works out hints: always the strongest, whatever the game's difficulty. */
val HINTER = named("hinter")

/** Where the computer thinks, off the main thread. */
val COMPUTE = named("compute")

/** Storage: one [Settings] store shared by the preferences, the saved games, the review counters and the achievements. */
val dataModule = module {
    single<Settings> { Settings() }
    single<GamePreferences> { SettingsGamePreferences(get()) }
    single<SavedGamesRepository> { SettingsSavedGamesRepository(get()) }
    single<Clock> { Clock.System }
    single { SavedGameFactory(Random.Default, get()) }
    single<ReviewStore> { SettingsReviewStore(get()) }
    single { ReviewTracker(get(), get()) }
    single<AchievementStore> { SettingsAchievementStore(get()) }
    single { AchievementTracker(get()) }
}

val gameModule = module {
    single<ComputerPlayerFactory> { ComputerPlayerFactory { ComputerPlayer.forDifficulty(it) } }
    single(HINTER) { ComputerPlayer.forDifficulty(Difficulty.Hard) }
    single<CoroutineDispatcher>(COMPUTE) { Dispatchers.Default }
}

val viewModelModule = module {
    viewModel { SettingsViewModel(get()) }
    viewModel { TutorialViewModel(get(), get()) }
    viewModel {
        GameViewModel(
            repository = get(),
            savedGames = get(),
            preferences = get(),
            computerPlayers = get(),
            hinter = get(HINTER),
            clock = get(),
            reviews = get(),
            achievements = get(),
            computeDispatcher = get(COMPUTE),
        )
    }
}

val appModules = listOf(dataModule, gameModule, viewModelModule)
