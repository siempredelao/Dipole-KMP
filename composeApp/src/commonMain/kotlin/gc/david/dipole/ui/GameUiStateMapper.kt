package gc.david.dipole.ui

import gc.david.dipole.game.DipoleRules
import gc.david.dipole.game.GameSessions
import gc.david.dipole.game.MoveKind
import gc.david.dipole.game.Player

/** Works out what the game screen shows from the session and the selected stack. */
object GameUiStateMapper {

    /** [uiState] with every field derived from its session and selection filled in. */
    fun map(uiState: GameUiState): GameUiState {
        val session = uiState.session
        val state = session.state
        val computerThinking = GameSessions.isComputerTurn(session)
        val winner = DipoleRules.winner(state)
        val fromSelected = uiState.selected?.let { DipoleRules.legalMovesFrom(state, it) }.orEmpty()
        val (onBoard, offBoard) = fromSelected.partition { it.to.isOnBoard }

        return uiState.copy(
            movable = if (computerThinking) emptySet() else DipoleRules.legalMoves(state).map { it.from }.toSet(),
            targets = onBoard.associateBy { it.to },
            captureTargets = onBoard.filter { DipoleRules.kindOf(state, it) is MoveKind.Capture }.map { it.to }.toSet(),
            bearOffs = offBoard,
            canUndo = GameSessions.canUndo(session),
            canHint = !computerThinking && winner == null,
            status = GameStatus(
                winner = winner,
                computerThinking = computerThinking,
                satOut = if (GameSessions.opponentSatOut(session)) state.toMove.opponent else null,
            ),
            checkersOnBoard = Player.entries.associateWith { DipoleRules.checkersOf(state, it) },
            removedCheckers = Player.entries.associateWith { DipoleRules.removedCheckersOf(state, it) },
        )
    }
}
