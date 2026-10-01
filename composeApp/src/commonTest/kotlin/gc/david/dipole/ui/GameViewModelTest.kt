package gc.david.dipole.ui

import gc.david.dipole.game.ComputerPlayer
import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.DipoleRules
import gc.david.dipole.game.Direction
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.GameSession
import gc.david.dipole.game.GameSessions
import gc.david.dipole.game.GameState
import gc.david.dipole.game.Move
import gc.david.dipole.game.Player
import gc.david.dipole.game.Square
import gc.david.dipole.game.Stack
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
class GameViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeSavedGamesRepository()
    private val clock = object : Clock {
        override fun now() = Instant.fromEpochMilliseconds(1_790_000_000_000)
    }
    private val opening = Move(DipoleRules.WHITE_START, Direction.NorthEast, 3)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private val preferences = FakeGamePreferences()

    private fun viewModel(initialSession: GameSession? = null) = GameViewModel(
        repository,
        preferences,
        computerPlayers = { ComputerPlayer.forDifficulty(it) },
        hinter = ComputerPlayer(depth = 1),
        clock = clock,
        computeDispatcher = dispatcher,
        initialSession = initialSession
            ?: GameSessions.new(GameMode.VsComputer, preferences.lastDifficulty, humanSide = preferences.lastSide),
    )

    /** A game of [mode] starting from [board], with [toMove] to play. */
    private fun endgame(mode: GameMode, toMove: Player, vararg board: Pair<Square, Stack>) =
        GameSessions.new(mode, initial = GameState(mapOf(*board), toMove))

    private fun TestScope.play(vm: GameViewModel, move: Move) {
        vm.onAction(GameAction.SquareTapped(move.from))
        vm.onAction(GameAction.SquareTapped(move.to))
        advanceUntilIdle()
    }

    @Test
    fun startsAGameAgainstTheComputer() {
        val state = viewModel().uiState.value
        assertEquals(GameMode.VsComputer, state.session.mode)
        assertEquals(DipoleRules.initial(), state.state)
        assertFalse(state.hasSavedGames)
    }

    @Test
    fun tappingAStackThenATargetPlaysTheMoveAndTheComputerReplies() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAction(GameAction.SquareTapped(opening.from))
        assertEquals(opening.from, vm.uiState.value.selected)
        vm.onAction(GameAction.SquareTapped(opening.to))
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
        vm.onAction(GameAction.NewGameClicked)
        assertEquals(GameDialog.NewGame, vm.uiState.value.dialog)
        vm.onAction(GameAction.ModeChosen(GameMode.TwoPlayers))
        assertNull(vm.uiState.value.dialog)
        assertEquals(GameMode.TwoPlayers, vm.uiState.value.session.mode)
        assertTrue(vm.uiState.value.session.moves.isEmpty())
    }

    @Test
    fun choosingVsComputerAsksForTheDifficulty() = runTest(dispatcher) {
        preferences.lastDifficulty = Difficulty.Easy
        val vm = viewModel()
        assertEquals(Difficulty.Easy, vm.uiState.value.session.difficulty)
        vm.onAction(GameAction.NewGameClicked)
        vm.onAction(GameAction.ModeChosen(GameMode.VsComputer))
        assertEquals(GameDialog.ChooseDifficulty(Difficulty.Easy, Player.White), vm.uiState.value.dialog)
        vm.onAction(GameAction.BackToModeClicked)
        assertEquals(GameDialog.NewGame, vm.uiState.value.dialog)
        vm.onAction(GameAction.ModeChosen(GameMode.VsComputer))
        vm.onAction(GameAction.DifficultyChosen(Difficulty.Hard))
        assertNull(vm.uiState.value.dialog)
        assertEquals(GameMode.VsComputer, vm.uiState.value.session.mode)
        assertEquals(Difficulty.Hard, vm.uiState.value.session.difficulty)
        assertEquals(Difficulty.Hard, preferences.lastDifficulty)
    }

    @Test
    fun choosingBlackLetsTheComputerOpen() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAction(GameAction.NewGameClicked)
        vm.onAction(GameAction.ModeChosen(GameMode.VsComputer))
        vm.onAction(GameAction.SideChosen(Player.Black))
        assertEquals(Player.Black, (vm.uiState.value.dialog as GameDialog.ChooseDifficulty).side)
        vm.onAction(GameAction.DifficultyChosen(Difficulty.Easy))
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
        vm.onAction(GameAction.UndoClicked)
        assertTrue(vm.uiState.value.session.moves.isEmpty())
    }

    @Test
    fun saveAndLoadRestoresTheGame() = runTest(dispatcher) {
        val vm = viewModel()
        play(vm, opening)
        val saved = vm.uiState.value.session

        vm.onAction(GameAction.SaveClicked)
        val dialog = assertIs<GameDialog.Save>(vm.uiState.value.dialog)
        assertEquals(clock.now(), dialog.savedAt)
        vm.onAction(GameAction.SaveConfirmed("Opening"))
        assertEquals(GameMessage.GameSaved, vm.uiState.value.message)
        assertTrue(vm.uiState.value.hasSavedGames)

        vm.onAction(GameAction.NewGameClicked)
        vm.onAction(GameAction.ModeChosen(GameMode.TwoPlayers))
        vm.onAction(GameAction.LoadClicked)
        val load = assertIs<GameDialog.Load>(vm.uiState.value.dialog)
        assertEquals(listOf("Opening"), load.savedGames.map { it.name })

        vm.onAction(GameAction.SavedGameChosen(load.savedGames.single()))
        assertEquals(saved.state, vm.uiState.value.state)
        assertEquals(saved.moves, vm.uiState.value.session.moves)
        assertEquals(GameMode.VsComputer, vm.uiState.value.session.mode)
        assertEquals(GameMessage.GameLoaded, vm.uiState.value.message)
        assertNull(vm.uiState.value.dialog)
    }

    @Test
    fun deletingTheLastSaveClosesTheList() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAction(GameAction.SaveConfirmed("Only"))
        vm.onAction(GameAction.LoadClicked)
        val load = assertIs<GameDialog.Load>(vm.uiState.value.dialog)
        vm.onAction(GameAction.SavedGameDeleted(load.savedGames.single()))
        assertNull(vm.uiState.value.dialog)
        assertFalse(vm.uiState.value.hasSavedGames)
    }

    @Test
    fun appearanceOpensItsDialogFromTheMenu() {
        val vm = viewModel()
        vm.onAction(GameAction.MenuClicked)
        vm.onAction(GameAction.AppearanceClicked)
        assertFalse(vm.uiState.value.menuOpen)
        assertEquals(GameDialog.Appearance, vm.uiState.value.dialog)
    }

    @Test
    fun choosingAMenuItemClosesTheMenu() {
        val vm = viewModel()
        vm.onAction(GameAction.MenuClicked)
        assertTrue(vm.uiState.value.menuOpen)
        vm.onAction(GameAction.MenuDismissed)
        assertFalse(vm.uiState.value.menuOpen)

        vm.onAction(GameAction.MenuClicked)
        vm.onAction(GameAction.NewGameClicked)
        assertFalse(vm.uiState.value.menuOpen)
        assertEquals(GameDialog.NewGame, vm.uiState.value.dialog)
    }

    @Test
    fun noHintUntilHintIsTapped() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAction(GameAction.SquareTapped(DipoleRules.WHITE_START))
        advanceUntilIdle()
        assertFalse(vm.uiState.value.hintsOn)
        assertNull(vm.uiState.value.hint)
    }

    @Test
    fun hintShowsTheBestMoveFromTheSelectedStack() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAction(GameAction.HintClicked)
        vm.onAction(GameAction.SquareTapped(DipoleRules.WHITE_START))
        assertNull(vm.uiState.value.hint) // still thinking
        advanceUntilIdle()
        val hint = vm.uiState.value.hint
        assertEquals(DipoleRules.WHITE_START, hint?.move?.from)
        assertTrue(DipoleRules.isLegal(vm.uiState.value.state, hint!!.move))
    }

    @Test
    fun tappingHintWithAStackSelectedShowsItsHint() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAction(GameAction.SquareTapped(DipoleRules.WHITE_START))
        vm.onAction(GameAction.HintClicked)
        advanceUntilIdle()
        assertEquals(DipoleRules.WHITE_START, vm.uiState.value.hint?.move?.from)
    }

    @Test
    fun hintsSwitchOffWhenTheTurnPasses() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAction(GameAction.HintClicked)
        play(vm, opening)
        assertEquals(Player.White, vm.uiState.value.state.toMove)
        assertFalse(vm.uiState.value.hintsOn)
        assertNull(vm.uiState.value.hint)
    }

    @Test
    fun hintCanBeTurnedOffAgain() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAction(GameAction.HintClicked)
        vm.onAction(GameAction.SquareTapped(DipoleRules.WHITE_START))
        advanceUntilIdle()
        vm.onAction(GameAction.HintClicked)
        assertFalse(vm.uiState.value.hintsOn)
        assertNull(vm.uiState.value.hint)
    }

    @Test
    fun noHintsOnTheComputersTurn() = runTest(dispatcher) {
        val vm = viewModel()
        vm.onAction(GameAction.SquareTapped(opening.from))
        vm.onAction(GameAction.SquareTapped(opening.to))
        assertFalse(vm.uiState.value.canHint)
        vm.onAction(GameAction.HintClicked)
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
        assertEquals(Player.White, DipoleRules.winner(vm.uiState.value.state))
        assertEquals(1, vm.uiState.value.celebration)
        assertTrue(vm.uiState.value.celebrating)
        // Once the confetti has started it isn't thrown again, e.g. after visiting the rules.
        vm.onAction(GameAction.CelebrationShown)
        assertFalse(vm.uiState.value.celebrating)
        assertEquals(1, vm.uiState.value.celebration)
    }

    @Test
    fun theComputerWinningIsNotCelebrated() = runTest(dispatcher) {
        val vm = viewModel(
            endgame(GameMode.VsComputer, Player.Black, Square(3, 3) to Stack(Player.White, 1), Square(4, 4) to Stack(Player.Black, 1)),
        )
        advanceUntilIdle()
        assertEquals(Player.Black, DipoleRules.winner(vm.uiState.value.state))
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
        assertEquals(Player.White, DipoleRules.winner(vm.uiState.value.state))
        assertEquals(1, vm.uiState.value.celebration)
    }

    @Test
    fun eitherPlayerWinningATwoPlayerGameIsCelebrated() = runTest(dispatcher) {
        val vm = viewModel(
            endgame(GameMode.TwoPlayers, Player.Black, Square(3, 3) to Stack(Player.White, 1), Square(4, 4) to Stack(Player.Black, 1)),
        )
        play(vm, Move(Square(4, 4), Direction.SouthWest, 1))
        assertEquals(Player.Black, DipoleRules.winner(vm.uiState.value.state))
        assertEquals(1, vm.uiState.value.celebration)
    }

    @Test
    fun undoingAWinDoesNotCelebrateAgainUntilTheNextWin() = runTest(dispatcher) {
        val win = Move(Square(2, 2), Direction.NorthEast, 1)
        val vm = viewModel(
            endgame(GameMode.TwoPlayers, Player.White, Square(2, 2) to Stack(Player.White, 1), Square(3, 3) to Stack(Player.Black, 1)),
        )
        play(vm, win)
        vm.onAction(GameAction.UndoClicked)
        assertEquals(null, DipoleRules.winner(vm.uiState.value.state))
        assertEquals(1, vm.uiState.value.celebration)
        play(vm, win)
        assertEquals(2, vm.uiState.value.celebration)
    }
}
