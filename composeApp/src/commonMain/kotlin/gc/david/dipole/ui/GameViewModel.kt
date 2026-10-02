package gc.david.dipole.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gc.david.dipole.game.ComputerPlayer
import gc.david.dipole.game.ComputerPlayerFactory
import gc.david.dipole.game.DipoleRules
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.GameSession
import gc.david.dipole.game.GameSessions
import gc.david.dipole.game.Move
import gc.david.dipole.game.Square
import gc.david.dipole.saves.GamePreferences
import gc.david.dipole.saves.SavedGame
import gc.david.dipole.saves.SavedGameFactory
import gc.david.dipole.saves.SavedGamesRepository
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameViewModel(
    private val repository: SavedGamesRepository,
    private val savedGames: SavedGameFactory,
    private val preferences: GamePreferences,
    private val computerPlayers: ComputerPlayerFactory,
    /** Works out hints; always the strongest player, whatever the game's difficulty. */
    private val hinter: ComputerPlayer,
    private val clock: Clock,
    private val computeDispatcher: CoroutineDispatcher,
    /** The game to open with; by default a new one against the computer with the last choices. */
    initialSession: GameSession = GameSessions.new(
        GameMode.VsComputer,
        preferences.lastDifficulty,
        humanSide = preferences.lastSide,
    ),
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GameUiStateMapper.map(
            GameUiState(
                session = initialSession,
                hasSavedGames = repository.list().isNotEmpty(),
            ),
        ),
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var computerMove: Job? = null
    private var hintJob: Job? = null

    init {
        // When the human last played Black, the computer opens the game.
        playComputerIfItsTurn()
    }

    fun onAction(action: GameAction) {
        when (action) {
            is GameAction.SquareTapped -> onSquareTapped(action)
            is GameAction.BearOffChosen -> play(action.move)
            GameAction.NewGameClicked -> updateUi { it.copy(menuOpen = false, dialog = GameDialog.NewGame) }
            is GameAction.ModeChosen -> when (action.mode) {
                GameMode.TwoPlayers -> startSession(GameSessions.new(GameMode.TwoPlayers), message = null)
                GameMode.VsComputer -> updateUi {
                    it.copy(dialog = GameDialog.ChooseDifficulty(preferences.lastDifficulty, preferences.lastSide))
                }
            }
            is GameAction.SideChosen -> updateUi {
                val dialog = it.dialog as? GameDialog.ChooseDifficulty ?: return@updateUi it
                it.copy(dialog = dialog.copy(side = action.side))
            }
            is GameAction.DifficultyChosen -> {
                val side = (_uiState.value.dialog as? GameDialog.ChooseDifficulty)?.side ?: preferences.lastSide
                preferences.lastDifficulty = action.difficulty
                preferences.lastSide = side
                startSession(GameSessions.new(GameMode.VsComputer, action.difficulty, humanSide = side), message = null)
            }
            GameAction.BackToModeClicked -> updateUi { it.copy(dialog = GameDialog.NewGame) }
            GameAction.UndoClicked -> {
                if (_uiState.value.canUndo) startSession(GameSessions.undo(_uiState.value.session), message = null)
            }
            GameAction.HintClicked -> toggleHints()
            GameAction.MenuClicked -> updateUi { it.copy(menuOpen = true) }
            GameAction.MenuDismissed -> updateUi { it.copy(menuOpen = false) }
            GameAction.AppearanceClicked -> updateUi {
                it.copy(menuOpen = false, dialog = GameDialog.Appearance)
            }
            GameAction.SaveClicked -> updateUi {
                it.copy(menuOpen = false, dialog = GameDialog.Save(clock.now()))
            }
            is GameAction.SaveConfirmed -> save(action.name)
            GameAction.LoadClicked -> updateUi {
                it.copy(menuOpen = false, dialog = GameDialog.Load(repository.list()))
            }
            is GameAction.SavedGameChosen -> load(action.game)
            is GameAction.SavedGameDeleted -> delete(action.game)
            GameAction.DialogDismissed -> updateUi { it.copy(dialog = null) }
            GameAction.MessageShown -> updateUi { it.copy(message = null) }
            GameAction.CelebrationShown -> updateUi { it.copy(celebrating = false) }
        }
    }

    private fun onSquareTapped(action: GameAction.SquareTapped) {
        val current = _uiState.value
        val target = current.targets[action.square]
        when {
            target != null -> play(target)
            action.square in current.movable -> select(if (current.selected == action.square) null else action.square)
            else -> select(null)
        }
    }

    private fun select(square: Square?) {
        updateUi { it.copy(selected = square, hint = null) }
        requestHint()
    }

    private fun toggleHints() {
        if (!_uiState.value.canHint) return
        updateUi { it.copy(hintsOn = !it.hintsOn, hint = null) }
        requestHint()
    }

    /** Works out the hint for the selected stack in the background, if hints are on. */
    private fun requestHint() {
        hintJob?.cancel()
        val current = _uiState.value
        val square = current.selected
        if (!current.hintsOn || square == null) return
        val session = current.session
        hintJob = viewModelScope.launch {
            val hint = withContext(computeDispatcher) { hinter.hint(session.state, square) } ?: return@launch
            // Only show it if the player is still looking at the same stack in the same position.
            val latest = _uiState.value
            if (latest.session !== session || latest.selected != square || !latest.hintsOn) return@launch
            updateUi { it.copy(hint = hint) }
        }
    }

    private fun play(move: Move) {
        val session = _uiState.value.session
        if (GameSessions.isComputerTurn(session) || !DipoleRules.isLegal(session.state, move)) return
        val next = GameSessions.play(session, move)
        startSession(next, message = null)
        celebrateIfHumanWon(next)
    }

    /** A human just won with the move that led to [session]: in two-player games, either player. */
    private fun celebrateIfHumanWon(session: GameSession) {
        val winner = DipoleRules.winner(session.state) ?: return
        if (session.mode == GameMode.VsComputer && winner != session.humanSide) return
        updateUi { it.copy(celebration = it.celebration + 1, celebrating = true) }
    }

    private fun save(name: String) {
        repository.save(savedGames.create(_uiState.value.session, name))
        updateUi { it.copy(dialog = null, message = GameMessage.GameSaved, hasSavedGames = true) }
    }

    private fun load(game: SavedGame) {
        val session = GameSessions.replay(game.mode, game.moves, game.difficulty, game.humanSide)
        if (session == null) {
            updateUi { it.copy(dialog = null, message = GameMessage.SaveUnreadable) }
            return
        }
        startSession(session, message = GameMessage.GameLoaded)
    }

    private fun delete(game: SavedGame) {
        repository.delete(game.id)
        val remaining = repository.list()
        updateUi {
            it.copy(
                dialog = if (remaining.isEmpty()) null else GameDialog.Load(remaining),
                hasSavedGames = remaining.isNotEmpty(),
            )
        }
    }

    /** Switches to [session], closing any dialog and clearing the selection and hints. */
    private fun startSession(session: GameSession, message: GameMessage?) {
        hintJob?.cancel()
        updateUi {
            it.copy(session = session, selected = null, dialog = null, message = message, hintsOn = false, hint = null)
        }
        playComputerIfItsTurn()
    }

    private fun playComputerIfItsTurn() {
        computerMove?.cancel()
        val session = _uiState.value.session
        if (!GameSessions.isComputerTurn(session)) return
        computerMove = viewModelScope.launch {
            delay(COMPUTER_DELAY_MILLIS)
            val move = withContext(computeDispatcher) { computerPlayers.forDifficulty(session.difficulty).chooseMove(session.state) } ?: return@launch
            // Only apply the move if the game hasn't changed meanwhile (new game, load, ...).
            if (_uiState.value.session !== session) return@launch
            val next = GameSessions.play(session, move)
            updateUi { it.copy(session = next, selected = null, hintsOn = false, hint = null) }
            // The computer can lose on its own move, by moving its last checkers off the board.
            celebrateIfHumanWon(next)
            playComputerIfItsTurn()
        }
    }

    /** Updates the UI state, working out the derived fields again when the game or selection changed. */
    private fun updateUi(transform: (GameUiState) -> GameUiState) {
        _uiState.update { old ->
            val new = transform(old)
            if (new.session === old.session && new.selected == old.selected) new else GameUiStateMapper.map(new)
        }
    }

    private companion object {
        const val COMPUTER_DELAY_MILLIS = 400L
    }
}
