package gc.david.dipole.ui

import gc.david.dipole.game.Difficulty
import gc.david.dipole.game.GameMode
import gc.david.dipole.game.GameSession
import gc.david.dipole.game.GameState
import gc.david.dipole.game.Hint
import gc.david.dipole.game.Move
import gc.david.dipole.game.Square
import gc.david.dipole.saves.SavedGame

/** Everything [DipoleScreen] shows. */
data class DipoleUiState(
    val session: GameSession,
    val selected: Square? = null,
    val dialog: DipoleDialog? = null,
    /** A short confirmation such as "Game saved", cleared once shown. */
    val message: String? = null,
    val hasSavedGames: Boolean = false,
    /** True once the player tapped Hint this turn; switched off when the turn passes. */
    val hintsOn: Boolean = false,
    /** The recommended move from the selected stack, once worked out. */
    val hint: Hint? = null,
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

sealed interface DipoleDialog {
    data object NewGame : DipoleDialog

    /** Second step of New game against the computer, with [suggested] picked last time. */
    data class ChooseDifficulty(val suggested: Difficulty) : DipoleDialog

    data class Save(val defaultName: String) : DipoleDialog
    data class Load(val savedGames: List<SavedGame>) : DipoleDialog
}

sealed interface DipoleAction {
    data class SquareTapped(val square: Square) : DipoleAction
    data class BearOffChosen(val move: Move) : DipoleAction
    data object NewGameClicked : DipoleAction
    data class ModeChosen(val mode: GameMode) : DipoleAction
    data class DifficultyChosen(val difficulty: Difficulty) : DipoleAction
    data object BackToModeClicked : DipoleAction
    data object UndoClicked : DipoleAction
    data object HintClicked : DipoleAction
    data object SaveClicked : DipoleAction
    data class SaveConfirmed(val name: String) : DipoleAction
    data object LoadClicked : DipoleAction
    data class SavedGameChosen(val game: SavedGame) : DipoleAction
    data class SavedGameDeleted(val game: SavedGame) : DipoleAction
    data object DialogDismissed : DipoleAction
    data object MessageShown : DipoleAction
}
