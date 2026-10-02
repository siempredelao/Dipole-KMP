package gc.david.dipole.di

import com.russhwolf.settings.Settings
import gc.david.dipole.game.ComputerPlayer
import gc.david.dipole.game.ComputerPlayerFactory
import gc.david.dipole.game.Difficulty
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

/** Storage: one [Settings] store shared by the preferences and the saved games. */
val dataModule = module {
    single<Settings> { Settings() }
    single<GamePreferences> { SettingsGamePreferences(get()) }
    single<SavedGamesRepository> { SettingsSavedGamesRepository(get()) }
    single<Clock> { Clock.System }
    single { SavedGameFactory(Random.Default, get()) }
}

val gameModule = module {
    single<ComputerPlayerFactory> { ComputerPlayerFactory { ComputerPlayer.forDifficulty(it) } }
    single(HINTER) { ComputerPlayer.forDifficulty(Difficulty.Hard) }
    single<CoroutineDispatcher>(COMPUTE) { Dispatchers.Default }
}

val viewModelModule = module {
    viewModel { SettingsViewModel(get()) }
    viewModel { TutorialViewModel(get()) }
    viewModel {
        GameViewModel(
            repository = get(),
            savedGames = get(),
            preferences = get(),
            computerPlayers = get(),
            hinter = get(HINTER),
            clock = get(),
            computeDispatcher = get(COMPUTE),
        )
    }
}

val appModules = listOf(dataModule, gameModule, viewModelModule)
