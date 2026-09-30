package dev.siempredelao.dipole.ui

import dev.siempredelao.dipole.game.Direction
import dev.siempredelao.dipole.game.GameMode
import dev.siempredelao.dipole.game.GameState
import dev.siempredelao.dipole.game.Move
import dev.siempredelao.dipole.game.Player
import dev.siempredelao.dipole.saves.SavedGame
import dev.siempredelao.dipole.saves.SavedGamesRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class DipoleViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeSavedGamesRepository()
    private val clock = object : Clock {
        override fun now() = Instant.fromEpochMilliseconds(1_790_000_000_000)
    }
    private val opening = Move(GameState.WHITE_START, Direction.NorthEast, 3)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = DipoleViewModel(repository, clock = clock, computeDispatcher = dispatcher)

    private fun TestScope.play(vm: DipoleViewModel, move: Move) {
        vm.onAction(DipoleAction.SquareTapped(move.from))
        vm.onAction(DipoleAction.SquareTapped(move.to))
        advanceUntilIdle()
    }

    @Test
    fun startsAGameAgainstTheComputer() {
        val state = viewModel().uiState.value
        assertEquals(GameMode.VsComputer, state.session.mode)
        assertEquals(GameState.initial(), state.state)
        assertFalse(state.hasSavedGames)
    }

    @Test
    fun tappingAStackThenATargetPlaysTheMoveAndTheComputerReplies() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAction(DipoleAction.SquareTapped(opening.from))
        assertEquals(opening.from, vm.uiState.value.selected)
        vm.onAction(DipoleAction.SquareTapped(opening.to))
        assertEquals(opening, vm.uiState.value.session.moves.first())
        assertNull(vm.uiState.value.selected)
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.session.moves.size)
        assertEquals(Player.White, vm.uiState.value.state.toMove)
    }

    @Test
    fun newGameAsksForTheModeAndStartsIt() = runTest(dispatcher) {
        val vm = viewModel()
        play(vm, opening)
        vm.onAction(DipoleAction.NewGameClicked)
        assertEquals(DipoleDialog.NewGame, vm.uiState.value.dialog)
        vm.onAction(DipoleAction.ModeChosen(GameMode.TwoPlayers))
        assertNull(vm.uiState.value.dialog)
        assertEquals(GameMode.TwoPlayers, vm.uiState.value.session.mode)
        assertTrue(vm.uiState.value.session.moves.isEmpty())
    }

    @Test
    fun undoTakesBackTheComputerReplyToo() = runTest(dispatcher) {
        val vm = viewModel()
        play(vm, opening)
        vm.onAction(DipoleAction.UndoClicked)
        assertTrue(vm.uiState.value.session.moves.isEmpty())
    }

    @Test
    fun saveAndLoadRestoresTheGame() = runTest(dispatcher) {
        val vm = viewModel()
        play(vm, opening)
        val saved = vm.uiState.value.session

        vm.onAction(DipoleAction.SaveClicked)
        val dialog = assertIs<DipoleDialog.Save>(vm.uiState.value.dialog)
        assertEquals(SavedGame.defaultName(clock.now()), dialog.defaultName)
        vm.onAction(DipoleAction.SaveConfirmed("Opening"))
        assertEquals("Game saved", vm.uiState.value.message)
        assertTrue(vm.uiState.value.hasSavedGames)

        vm.onAction(DipoleAction.NewGameClicked)
        vm.onAction(DipoleAction.ModeChosen(GameMode.TwoPlayers))
        vm.onAction(DipoleAction.LoadClicked)
        val load = assertIs<DipoleDialog.Load>(vm.uiState.value.dialog)
        assertEquals(listOf("Opening"), load.savedGames.map { it.name })

        vm.onAction(DipoleAction.SavedGameChosen(load.savedGames.single()))
        assertEquals(saved.state, vm.uiState.value.state)
        assertEquals(saved.moves, vm.uiState.value.session.moves)
        assertEquals(GameMode.VsComputer, vm.uiState.value.session.mode)
        assertEquals("Game loaded", vm.uiState.value.message)
        assertNull(vm.uiState.value.dialog)
    }

    @Test
    fun deletingTheLastSaveClosesTheList() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAction(DipoleAction.SaveConfirmed("Only"))
        vm.onAction(DipoleAction.LoadClicked)
        val load = assertIs<DipoleDialog.Load>(vm.uiState.value.dialog)
        vm.onAction(DipoleAction.SavedGameDeleted(load.savedGames.single()))
        assertNull(vm.uiState.value.dialog)
        assertFalse(vm.uiState.value.hasSavedGames)
    }
}

private class FakeSavedGamesRepository : SavedGamesRepository {
    private val games = mutableMapOf<String, SavedGame>()
    override fun list() = games.values.sortedByDescending { it.savedAtEpochMillis }
    override fun save(game: SavedGame) { games[game.id] = game }
    override fun delete(id: String) { games.remove(id) }
}
