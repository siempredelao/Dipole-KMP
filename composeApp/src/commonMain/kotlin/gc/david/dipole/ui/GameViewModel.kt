package gc.david.dipole.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import gc.david.dipole.game.ComputerPlayer
import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.GameSession
import gc.david.dipole.game.Move
import gc.david.dipole.game.Square
import gc.david.dipole.saves.GamePreferences
import gc.david.dipole.saves.SavedGame
import gc.david.dipole.saves.SavedGamesRepository
import gc.david.dipole.saves.SettingsGamePreferences
import gc.david.dipole.saves.SettingsSavedGamesRepository
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GameViewModel(
    private val repository: SavedGamesRepository = SettingsSavedGamesRepository(),
    private val preferences: GamePreferences = SettingsGamePreferences(),
    private val computerFor: (Difficulty) -> ComputerPlayer = { ComputerPlayer.forDifficulty(it) },
    /** Works out hints; always the strongest player, whatever the game's difficulty. */
    private val hinter: ComputerPlayer = ComputerPlayer.forDifficulty(Difficulty.Hard),
    private val clock: Clock = Clock.System,
    private val computeDispatcher: CoroutineDispatcher = Dispatchers.Default,
    /** The game to open with; by default a new one against the computer with the last choices. */
    initialSession: GameSession = GameSession.new(
        GameMode.VsComputer,
        preferences.lastDifficulty,
        humanSide = preferences.lastSide,
    ),
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        GameUiState(
            session = initialSession,
            hasSavedGames = repository.list().isNotEmpty(),
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
            GameAction.NewGameClicked -> _uiState.update { it.copy(menuOpen = false, dialog = GameDialog.NewGame) }
            is GameAction.ModeChosen -> when (action.mode) {
                GameMode.TwoPlayers -> startSession(GameSession.new(GameMode.TwoPlayers), message = null)
                GameMode.VsComputer -> _uiState.update {
                    it.copy(dialog = GameDialog.ChooseDifficulty(preferences.lastDifficulty, preferences.lastSide))
                }
            }
            is GameAction.SideChosen -> _uiState.update {
                val dialog = it.dialog as? GameDialog.ChooseDifficulty ?: return@update it
                it.copy(dialog = dialog.copy(side = action.side))
            }
            is GameAction.DifficultyChosen -> {
                val side = (_uiState.value.dialog as? GameDialog.ChooseDifficulty)?.side ?: preferences.lastSide
                preferences.lastDifficulty = action.difficulty
                preferences.lastSide = side
                startSession(GameSession.new(GameMode.VsComputer, action.difficulty, humanSide = side), message = null)
            }
            GameAction.BackToModeClicked -> _uiState.update { it.copy(dialog = GameDialog.NewGame) }
            GameAction.UndoClicked -> {
                if (_uiState.value.session.canUndo) startSession(_uiState.value.session.undo(), message = null)
            }
            GameAction.HintClicked -> toggleHints()
            GameAction.MenuClicked -> _uiState.update { it.copy(menuOpen = true) }
            GameAction.MenuDismissed -> _uiState.update { it.copy(menuOpen = false) }
            GameAction.AppearanceClicked -> _uiState.update {
                it.copy(menuOpen = false, dialog = GameDialog.Appearance)
            }
            GameAction.SaveClicked -> _uiState.update {
                it.copy(menuOpen = false, dialog = GameDialog.Save(clock.now()))
            }
            is GameAction.SaveConfirmed -> save(action.name)
            GameAction.LoadClicked -> _uiState.update {
                it.copy(menuOpen = false, dialog = GameDialog.Load(repository.list()))
            }
            is GameAction.SavedGameChosen -> load(action.game)
            is GameAction.SavedGameDeleted -> delete(action.game)
            GameAction.DialogDismissed -> _uiState.update { it.copy(dialog = null) }
            GameAction.MessageShown -> _uiState.update { it.copy(message = null) }
            GameAction.CelebrationShown -> _uiState.update { it.copy(celebrating = false) }
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
        _uiState.update { it.copy(selected = square, hint = null) }
        requestHint()
    }

    private fun toggleHints() {
        if (!_uiState.value.canHint) return
        _uiState.update { it.copy(hintsOn = !it.hintsOn, hint = null) }
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
            _uiState.update { it.copy(hint = hint) }
        }
    }

    private fun play(move: Move) {
        val session = _uiState.value.session
        if (session.isComputerTurn || !session.state.isLegal(move)) return
        val next = session.play(move)
        startSession(next, message = null)
        celebrateIfHumanWon(next)
    }

    /** A human just won with the move that led to [session]: in two-player games, either player. */
    private fun celebrateIfHumanWon(session: GameSession) {
        val winner = session.state.winner ?: return
        if (session.mode == GameMode.VsComputer && winner != session.humanSide) return
        _uiState.update { it.copy(celebration = it.celebration + 1, celebrating = true) }
    }

    private fun save(name: String) {
        repository.save(SavedGame.of(_uiState.value.session, name, clock.now()))
        _uiState.update { it.copy(dialog = null, message = GameMessage.GameSaved, hasSavedGames = true) }
    }

    private fun load(game: SavedGame) {
        val session = game.toSession()
        if (session == null) {
            _uiState.update { it.copy(dialog = null, message = GameMessage.SaveUnreadable) }
            return
        }
        startSession(session, message = GameMessage.GameLoaded)
    }

    private fun delete(game: SavedGame) {
        repository.delete(game.id)
        val remaining = repository.list()
        _uiState.update {
            it.copy(
                dialog = if (remaining.isEmpty()) null else GameDialog.Load(remaining),
                hasSavedGames = remaining.isNotEmpty(),
            )
        }
    }

    /** Switches to [session], closing any dialog and clearing the selection and hints. */
    private fun startSession(session: GameSession, message: GameMessage?) {
        hintJob?.cancel()
        _uiState.update {
            it.copy(session = session, selected = null, dialog = null, message = message, hintsOn = false, hint = null)
        }
        playComputerIfItsTurn()
    }

    private fun playComputerIfItsTurn() {
        computerMove?.cancel()
        val session = _uiState.value.session
        if (!session.isComputerTurn) return
        computerMove = viewModelScope.launch {
            delay(COMPUTER_DELAY_MILLIS)
            val move = withContext(computeDispatcher) { computerFor(session.difficulty).chooseMove(session.state) } ?: return@launch
            // Only apply the move if the game hasn't changed meanwhile (new game, load, ...).
            if (_uiState.value.session !== session) return@launch
            val next = session.play(move)
            _uiState.update { it.copy(session = next, selected = null, hintsOn = false, hint = null) }
            // The computer can lose on its own move, by moving its last checkers off the board.
            celebrateIfHumanWon(next)
            playComputerIfItsTurn()
        }
    }

    private companion object {
        const val COMPUTER_DELAY_MILLIS = 400L
    }
}
