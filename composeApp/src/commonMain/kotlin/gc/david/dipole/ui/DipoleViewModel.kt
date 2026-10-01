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

class DipoleViewModel(
    private val repository: SavedGamesRepository = SettingsSavedGamesRepository(),
    private val preferences: GamePreferences = SettingsGamePreferences(),
    private val computerFor: (Difficulty) -> ComputerPlayer = { ComputerPlayer.forDifficulty(it) },
    /** Works out hints; always the strongest player, whatever the game's difficulty. */
    private val hinter: ComputerPlayer = ComputerPlayer.forDifficulty(Difficulty.Hard),
    private val clock: Clock = Clock.System,
    private val computeDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DipoleUiState(
            session = GameSession.new(GameMode.VsComputer, preferences.lastDifficulty),
            hasSavedGames = repository.list().isNotEmpty(),
        ),
    )
    val uiState: StateFlow<DipoleUiState> = _uiState.asStateFlow()

    private var computerMove: Job? = null
    private var hintJob: Job? = null

    fun onAction(action: DipoleAction) {
        when (action) {
            is DipoleAction.SquareTapped -> onSquareTapped(action)
            is DipoleAction.BearOffChosen -> play(action.move)
            DipoleAction.NewGameClicked -> _uiState.update { it.copy(dialog = DipoleDialog.NewGame) }
            is DipoleAction.ModeChosen -> when (action.mode) {
                GameMode.TwoPlayers -> startSession(GameSession.new(GameMode.TwoPlayers), message = null)
                GameMode.VsComputer -> _uiState.update {
                    it.copy(dialog = DipoleDialog.ChooseDifficulty(preferences.lastDifficulty))
                }
            }
            is DipoleAction.DifficultyChosen -> {
                preferences.lastDifficulty = action.difficulty
                startSession(GameSession.new(GameMode.VsComputer, action.difficulty), message = null)
            }
            DipoleAction.BackToModeClicked -> _uiState.update { it.copy(dialog = DipoleDialog.NewGame) }
            DipoleAction.UndoClicked -> {
                if (_uiState.value.session.canUndo) startSession(_uiState.value.session.undo(), message = null)
            }
            DipoleAction.HintClicked -> toggleHints()
            DipoleAction.SaveClicked -> _uiState.update {
                it.copy(dialog = DipoleDialog.Save(clock.now()))
            }
            is DipoleAction.SaveConfirmed -> save(action.name)
            DipoleAction.LoadClicked -> _uiState.update { it.copy(dialog = DipoleDialog.Load(repository.list())) }
            is DipoleAction.SavedGameChosen -> load(action.game)
            is DipoleAction.SavedGameDeleted -> delete(action.game)
            DipoleAction.DialogDismissed -> _uiState.update { it.copy(dialog = null) }
            DipoleAction.MessageShown -> _uiState.update { it.copy(message = null) }
        }
    }

    private fun onSquareTapped(action: DipoleAction.SquareTapped) {
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
        startSession(session.play(move), message = null)
    }

    private fun save(name: String) {
        repository.save(SavedGame.of(_uiState.value.session, name, clock.now()))
        _uiState.update { it.copy(dialog = null, message = DipoleMessage.GameSaved, hasSavedGames = true) }
    }

    private fun load(game: SavedGame) {
        val session = game.toSession()
        if (session == null) {
            _uiState.update { it.copy(dialog = null, message = DipoleMessage.SaveUnreadable) }
            return
        }
        startSession(session, message = DipoleMessage.GameLoaded)
    }

    private fun delete(game: SavedGame) {
        repository.delete(game.id)
        val remaining = repository.list()
        _uiState.update {
            it.copy(
                dialog = if (remaining.isEmpty()) null else DipoleDialog.Load(remaining),
                hasSavedGames = remaining.isNotEmpty(),
            )
        }
    }

    /** Switches to [session], closing any dialog and clearing the selection and hints. */
    private fun startSession(session: GameSession, message: DipoleMessage?) {
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
            _uiState.update { it.copy(session = session.play(move), selected = null, hintsOn = false, hint = null) }
            playComputerIfItsTurn()
        }
    }

    private companion object {
        const val COMPUTER_DELAY_MILLIS = 400L
    }
}
