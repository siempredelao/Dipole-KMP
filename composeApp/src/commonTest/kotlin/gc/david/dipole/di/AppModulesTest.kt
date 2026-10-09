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

import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import gc.david.dipole.review.ReviewTracker
import gc.david.dipole.saves.GamePreferences
import gc.david.dipole.saves.SavedGameFactory
import gc.david.dipole.saves.SavedGamesRepository
import gc.david.dipole.ui.GameViewModel
import gc.david.dipole.ui.SettingsViewModel
import gc.david.dipole.ui.TutorialViewModel
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertSame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.dsl.koinApplication
import org.koin.dsl.module

/** Builds the real dependency graph, with in-memory storage, so a missing binding fails here. */
@OptIn(ExperimentalCoroutinesApi::class)
class AppModulesTest {

    private val koin = koinApplication {
        allowOverride(true)
        modules(appModules + module { single<Settings> { MapSettings() } })
    }.koin

    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() {
        koin.close()
        Dispatchers.resetMain()
    }

    @Test
    fun everyViewModelCanBeBuilt() {
        koin.get<SettingsViewModel>()
        koin.get<TutorialViewModel>()
        koin.get<GameViewModel>()
    }

    @Test
    fun storageIsSharedAcrossTheApp() {
        assertSame(koin.get<GamePreferences>(), koin.get<GamePreferences>())
        assertSame(koin.get<SavedGamesRepository>(), koin.get<SavedGamesRepository>())
        assertSame(koin.get<SavedGameFactory>(), koin.get<SavedGameFactory>())
        assertSame(koin.get<ReviewTracker>(), koin.get<ReviewTracker>())
    }
}
