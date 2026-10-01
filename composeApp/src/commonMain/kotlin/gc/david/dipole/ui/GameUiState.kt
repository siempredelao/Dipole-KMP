package gc.david.dipole.ui

import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.GameSession
import gc.david.dipole.game.GameState
import gc.david.dipole.game.Hint
import gc.david.dipole.game.Move
import gc.david.dipole.game.Player
import gc.david.dipole.game.Square
import gc.david.dipole.saves.SavedGame
import kotlin.time.Instant

/** Everything [GameScreen] shows. */
data class GameUiState(
    val session: GameSession,
    val selected: Square? = null,
    val dialog: GameDialog? = null,
    /** A short confirmation such as "Game saved", cleared once shown. */
    val message: GameMessage? = null,
    val hasSavedGames: Boolean = false,
    /** True once the player tapped Hint this turn; switched off when the turn passes. */
    val hintsOn: Boolean = false,
    /** The recommended move from the selected stack, once worked out. */
    val hint: Hint? = null,
    /** Goes up by one each time a human wins, telling one celebration from the next. */
    val celebration: Int = 0,
    /** True from a human win until the screen starts its confetti, so it is thrown only once. */
    val celebrating: Boolean = false,
    /** True while the settings menu (New game, Save, Load) is open. */
    val menuOpen: Boolean = false,
) {
    val state: GameState get() = session.state

    private val movesFromSelected: List<Move> get() = selected?.let { state.legalMovesFrom(it) }.orEmpty()

    /** Moves from the selected stack that stay on the board, by destination square. */
    val targets: Map<Square, Move> get() = movesFromSelected.filter { it.to.isOnBoard }.associateBy { it.to }

    /** Moves from the selected stack that leave the board. */
    val bearOffs: List<Move> get() = movesFromSelected.filter { !it.to.isOnBoard }

    /** Squares holding a stack the player can move now. */
    val movable: Set<Square>
        get() = if (session.isComputerTurn) emptySet() else state.legalMoves().map { it.from }.toSet()

    /** Hints are offered whenever a human is to move. */
    val canHint: Boolean get() = !session.isComputerTurn && !state.isOver
}

sealed interface GameDialog {
    data object NewGame : GameDialog

    /**
     * Second step of New game against the computer: the human picks a [side] (initially the one
     * picked last time) and a difficulty, with [suggested] picked last time.
     */
    data class ChooseDifficulty(val suggested: Difficulty, val side: Player) : GameDialog

    /** Asks for a save name; the default name comes from [savedAt], formatted for the user's language. */
    data class Save(val savedAt: Instant) : GameDialog
    data class Load(val savedGames: List<SavedGame>) : GameDialog
    data object Appearance : GameDialog
}

enum class GameMessage { GameSaved, GameLoaded, SaveUnreadable }

sealed interface GameAction {
    data class SquareTapped(val square: Square) : GameAction
    data class BearOffChosen(val move: Move) : GameAction
    data object NewGameClicked : GameAction
    data class ModeChosen(val mode: GameMode) : GameAction
    data class SideChosen(val side: Player) : GameAction
    data class DifficultyChosen(val difficulty: Difficulty) : GameAction
    data object BackToModeClicked : GameAction
    data object UndoClicked : GameAction
    data object HintClicked : GameAction
    data object MenuClicked : GameAction
    data object MenuDismissed : GameAction
    data object AppearanceClicked : GameAction
    data object SaveClicked : GameAction
    data class SaveConfirmed(val name: String) : GameAction
    data object LoadClicked : GameAction
    data class SavedGameChosen(val game: SavedGame) : GameAction
    data class SavedGameDeleted(val game: SavedGame) : GameAction
    data object DialogDismissed : GameAction
    data object MessageShown : GameAction
    data object CelebrationShown : GameAction
}
