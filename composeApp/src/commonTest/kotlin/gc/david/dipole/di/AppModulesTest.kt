package gc.david.dipole.di

import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import gc.david.dipole.saves.GamePreferences
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
    }
}
