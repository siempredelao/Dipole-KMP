package gc.david.dipole.ui

import gc.david.dipole.game.ComputerPlayer
import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.Direction
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.GameSession
import gc.david.dipole.game.GameState
import gc.david.dipole.game.Move
import gc.david.dipole.game.Player
import gc.david.dipole.game.Stack
import gc.david.dipole.game.Square
import gc.david.dipole.saves.SavedGame
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

    private val preferences = FakeGamePreferences()

    private fun viewModel(initialSession: GameSession? = null) = DipoleViewModel(
        repository,
        preferences,
        hinter = ComputerPlayer(depth = 1),
        clock = clock,
        computeDispatcher = dispatcher,
        initialSession = initialSession
            ?: GameSession.new(GameMode.VsComputer, preferences.lastDifficulty, humanSide = preferences.lastSide),
    )

    /** A game of [mode] starting from [board], with [toMove] to play. */
    private fun endgame(mode: GameMode, toMove: Player, vararg board: Pair<Square, Stack>) =
        GameSession.new(mode, initial = GameState(mapOf(*board), toMove))

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
    fun choosingVsComputerAsksForTheDifficulty() = runTest(dispatcher) {
        preferences.lastDifficulty = Difficulty.Easy
        val vm = viewModel()
        assertEquals(Difficulty.Easy, vm.uiState.value.session.difficulty)
        vm.onAction(DipoleAction.NewGameClicked)
        vm.onAction(DipoleAction.ModeChosen(GameMode.VsComputer))
        assertEquals(DipoleDialog.ChooseDifficulty(Difficulty.Easy, Player.White), vm.uiState.value.dialog)
        vm.onAction(DipoleAction.BackToModeClicked)
        assertEquals(DipoleDialog.NewGame, vm.uiState.value.dialog)
        vm.onAction(DipoleAction.ModeChosen(GameMode.VsComputer))
        vm.onAction(DipoleAction.DifficultyChosen(Difficulty.Hard))
        assertNull(vm.uiState.value.dialog)
        assertEquals(GameMode.VsComputer, vm.uiState.value.session.mode)
        assertEquals(Difficulty.Hard, vm.uiState.value.session.difficulty)
        assertEquals(Difficulty.Hard, preferences.lastDifficulty)
    }

    @Test
    fun choosingBlackLetsTheComputerOpen() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAction(DipoleAction.NewGameClicked)
        vm.onAction(DipoleAction.ModeChosen(GameMode.VsComputer))
        vm.onAction(DipoleAction.SideChosen(Player.Black))
        assertEquals(Player.Black, (vm.uiState.value.dialog as DipoleDialog.ChooseDifficulty).side)
        vm.onAction(DipoleAction.DifficultyChosen(Difficulty.Easy))
        assertEquals(Player.Black, vm.uiState.value.session.humanSide)
        assertEquals(Player.Black, preferences.lastSide)
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.session.moves.size)
        assertEquals(Player.Black, vm.uiState.value.state.toMove)
    }

    @Test
    fun startsAsBlackWhenThatWasTheLastSide() = runTest(dispatcher) {
        preferences.lastSide = Player.Black
        val vm = viewModel()
        assertEquals(Player.Black, vm.uiState.value.session.humanSide)
        advanceUntilIdle()
        assertEquals(Player.Black, vm.uiState.value.state.toMove)
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
        assertEquals(clock.now(), dialog.savedAt)
        vm.onAction(DipoleAction.SaveConfirmed("Opening"))
        assertEquals(DipoleMessage.GameSaved, vm.uiState.value.message)
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
        assertEquals(DipoleMessage.GameLoaded, vm.uiState.value.message)
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

    @Test
    fun appearanceOpensItsDialogFromTheMenu() {
        val vm = viewModel()
        vm.onAction(DipoleAction.MenuClicked)
        vm.onAction(DipoleAction.AppearanceClicked)
        assertFalse(vm.uiState.value.menuOpen)
        assertEquals(DipoleDialog.Appearance, vm.uiState.value.dialog)
    }

    @Test
    fun choosingAMenuItemClosesTheMenu() {
        val vm = viewModel()
        vm.onAction(DipoleAction.MenuClicked)
        assertTrue(vm.uiState.value.menuOpen)
        vm.onAction(DipoleAction.MenuDismissed)
        assertFalse(vm.uiState.value.menuOpen)

        vm.onAction(DipoleAction.MenuClicked)
        vm.onAction(DipoleAction.NewGameClicked)
        assertFalse(vm.uiState.value.menuOpen)
        assertEquals(DipoleDialog.NewGame, vm.uiState.value.dialog)
    }

    @Test
    fun noHintUntilHintIsTapped() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAction(DipoleAction.SquareTapped(GameState.WHITE_START))
        advanceUntilIdle()
        assertFalse(vm.uiState.value.hintsOn)
        assertNull(vm.uiState.value.hint)
    }

    @Test
    fun hintShowsTheBestMoveFromTheSelectedStack() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAction(DipoleAction.HintClicked)
        vm.onAction(DipoleAction.SquareTapped(GameState.WHITE_START))
        assertNull(vm.uiState.value.hint) // still thinking
        advanceUntilIdle()
        val hint = vm.uiState.value.hint
        assertEquals(GameState.WHITE_START, hint?.move?.from)
        assertTrue(vm.uiState.value.state.isLegal(hint!!.move))
    }

    @Test
    fun tappingHintWithAStackSelectedShowsItsHint() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAction(DipoleAction.SquareTapped(GameState.WHITE_START))
        vm.onAction(DipoleAction.HintClicked)
        advanceUntilIdle()
        assertEquals(GameState.WHITE_START, vm.uiState.value.hint?.move?.from)
    }

    @Test
    fun hintsSwitchOffWhenTheTurnPasses() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAction(DipoleAction.HintClicked)
        play(vm, opening)
        assertEquals(Player.White, vm.uiState.value.state.toMove)
        assertFalse(vm.uiState.value.hintsOn)
        assertNull(vm.uiState.value.hint)
    }

    @Test
    fun hintCanBeTurnedOffAgain() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAction(DipoleAction.HintClicked)
        vm.onAction(DipoleAction.SquareTapped(GameState.WHITE_START))
        advanceUntilIdle()
        vm.onAction(DipoleAction.HintClicked)
        assertFalse(vm.uiState.value.hintsOn)
        assertNull(vm.uiState.value.hint)
    }

    @Test
    fun noHintsOnTheComputersTurn() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAction(DipoleAction.SquareTapped(opening.from))
        vm.onAction(DipoleAction.SquareTapped(opening.to))
        assertFalse(vm.uiState.value.canHint)
        vm.onAction(DipoleAction.HintClicked)
        assertFalse(vm.uiState.value.hintsOn)
        advanceUntilIdle()
    }

    @Test
    fun winningAgainstTheComputerIsCelebrated() = runTest(dispatcher) {
        val vm = viewModel(
            endgame(GameMode.VsComputer, Player.White, Square(2, 2) to Stack(Player.White, 1), Square(3, 3) to Stack(Player.Black, 1)),
        )
        assertEquals(0, vm.uiState.value.celebration)
        play(vm, Move(Square(2, 2), Direction.NorthEast, 1))
        assertEquals(Player.White, vm.uiState.value.state.winner)
        assertEquals(1, vm.uiState.value.celebration)
        assertTrue(vm.uiState.value.celebrating)
        // Once the confetti has started it isn't thrown again, e.g. after visiting the rules.
        vm.onAction(DipoleAction.CelebrationShown)
        assertFalse(vm.uiState.value.celebrating)
        assertEquals(1, vm.uiState.value.celebration)
    }

    @Test
    fun theComputerWinningIsNotCelebrated() = runTest(dispatcher) {
        val vm = viewModel(
            endgame(GameMode.VsComputer, Player.Black, Square(3, 3) to Stack(Player.White, 1), Square(4, 4) to Stack(Player.Black, 1)),
        )
        advanceUntilIdle()
        assertEquals(Player.Black, vm.uiState.value.state.winner)
        assertEquals(0, vm.uiState.value.celebration)
        assertFalse(vm.uiState.value.celebrating)
    }

    @Test
    fun theComputerLosingOnItsOwnMoveIsCelebrated() = runTest(dispatcher) {
        // Black's only checker can only move off the board, which loses the game.
        val vm = viewModel(
            endgame(GameMode.VsComputer, Player.Black, Square(0, 0) to Stack(Player.Black, 1), Square(7, 7) to Stack(Player.White, 1)),
        )
        advanceUntilIdle()
        assertEquals(Player.White, vm.uiState.value.state.winner)
        assertEquals(1, vm.uiState.value.celebration)
    }

    @Test
    fun eitherPlayerWinningATwoPlayerGameIsCelebrated() = runTest(dispatcher) {
        val vm = viewModel(
            endgame(GameMode.TwoPlayers, Player.Black, Square(3, 3) to Stack(Player.White, 1), Square(4, 4) to Stack(Player.Black, 1)),
        )
        play(vm, Move(Square(4, 4), Direction.SouthWest, 1))
        assertEquals(Player.Black, vm.uiState.value.state.winner)
        assertEquals(1, vm.uiState.value.celebration)
    }

    @Test
    fun undoingAWinDoesNotCelebrateAgainUntilTheNextWin() = runTest(dispatcher) {
        val win = Move(Square(2, 2), Direction.NorthEast, 1)
        val vm = viewModel(
            endgame(GameMode.TwoPlayers, Player.White, Square(2, 2) to Stack(Player.White, 1), Square(3, 3) to Stack(Player.Black, 1)),
        )
        play(vm, win)
        vm.onAction(DipoleAction.UndoClicked)
        assertEquals(null, vm.uiState.value.state.winner)
        assertEquals(1, vm.uiState.value.celebration)
        play(vm, win)
        assertEquals(2, vm.uiState.value.celebration)
    }
}
