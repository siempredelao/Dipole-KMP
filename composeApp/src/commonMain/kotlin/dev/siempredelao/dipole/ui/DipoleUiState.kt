package dev.siempredelao.dipole.ui

import dev.siempredelao.dipole.game.GameMode
import dev.siempredelao.dipole.game.GameSession
import dev.siempredelao.dipole.game.GameState
import dev.siempredelao.dipole.game.Move
import dev.siempredelao.dipole.game.Square
import dev.siempredelao.dipole.saves.SavedGame

/** Everything [DipoleScreen] shows. */
data class DipoleUiState(
    val session: GameSession,
    val selected: Square? = null,
    val dialog: DipoleDialog? = null,
    /** A short confirmation such as "Game saved", cleared once shown. */
    val message: String? = null,
    val hasSavedGames: Boolean = false,
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
}

sealed interface DipoleDialog {
    data object NewGame : DipoleDialog
    data class Save(val defaultName: String) : DipoleDialog
    data class Load(val savedGames: List<SavedGame>) : DipoleDialog
}

sealed interface DipoleAction {
    data class SquareTapped(val square: Square) : DipoleAction
    data class BearOffChosen(val move: Move) : DipoleAction
    data object NewGameClicked : DipoleAction
    data class ModeChosen(val mode: GameMode) : DipoleAction
    data object UndoClicked : DipoleAction
    data object SaveClicked : DipoleAction
    data class SaveConfirmed(val name: String) : DipoleAction
    data object LoadClicked : DipoleAction
    data class SavedGameChosen(val game: SavedGame) : DipoleAction
    data class SavedGameDeleted(val game: SavedGame) : DipoleAction
    data object DialogDismissed : DipoleAction
    data object MessageShown : DipoleAction
}
