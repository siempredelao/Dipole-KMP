package dev.siempredelao.dipole.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.siempredelao.dipole.game.BOARD_SIZE
import dev.siempredelao.dipole.game.Direction
import dev.siempredelao.dipole.game.GameMode
import dev.siempredelao.dipole.game.GameSession
import dev.siempredelao.dipole.game.GameState
import dev.siempredelao.dipole.game.Hint
import dev.siempredelao.dipole.game.Move
import dev.siempredelao.dipole.game.MoveKind
import dev.siempredelao.dipole.game.Player
import dev.siempredelao.dipole.game.Square
import dev.siempredelao.dipole.game.Stack
import kotlinx.coroutines.delay

private val LightSquare = Color(0xFFEBD3A8)
private val DarkSquare = Color(0xFF7A4E2D)
private val WhiteChecker = Color(0xFFF5F0E6)
private val BlackChecker = Color(0xFF26211E)
private val Highlight = Color(0xFF7FC97F)
private val CaptureHighlight = Color(0xFFE0605A)
private val LastMove = Color(0x55F2D95C)
private val HintColor = Color(0xFF5CC8F2)

/** Connects [DipoleScreen] to its [DipoleViewModel]. */
@Composable
fun DipoleScreen(viewModel: DipoleViewModel = viewModel { DipoleViewModel() }) {
    val uiState by viewModel.uiState.collectAsState()
    DipoleScreen(uiState, viewModel::onAction)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DipoleScreen(uiState: DipoleUiState, onAction: (DipoleAction) -> Unit) {
    val session = uiState.session
    val state = uiState.state
    val bearOffs = uiState.bearOffs
    val hintAlpha = blinkAlpha(uiState.hint)
    val hintedMove = uiState.hint?.move

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Dipole", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(
            if (session.mode == GameMode.VsComputer) "vs Computer · ${session.difficulty.label}" else "2 Players",
            color = Color.LightGray,
            fontSize = 14.sp,
        )
        Text(
            statusText(session),
            color = Color.White,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
        )
        val trayModifier = Modifier.widthIn(max = 560.dp).fillMaxWidth()
        PlayerTray(state, Player.Black, trayModifier)
        Board(
            state = state,
            selected = uiState.selected,
            movable = uiState.movable,
            targets = uiState.targets,
            lastMove = session.lastMove,
            hintedSquare = hintedMove?.to?.takeIf { it.isOnBoard },
            hintAlpha = hintAlpha,
            onSquareClick = { onAction(DipoleAction.SquareTapped(it)) },
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth(),
        )
        PlayerTray(state, Player.White, trayModifier)
        if (bearOffs.isNotEmpty()) {
            Text(
                "Or move checkers off the board. They leave play and go to your off-board stack:",
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                bearOffs.forEach { move ->
                    val border = if (move == hintedMove) BorderStroke(3.dp, HintColor.copy(alpha = hintAlpha)) else null
                    OutlinedButton(
                        onClick = { onAction(DipoleAction.BearOffChosen(move)) },
                        border = border ?: ButtonDefaults.outlinedButtonBorder(),
                    ) {
                        Text("Move ${move.count} off ${arrow(move.direction)}")
                    }
                }
            }
        } else if (uiState.selected != null) {
            Text("Tap a highlighted square. The number shows how many checkers move.", color = Color.LightGray)
        }
        hintText(uiState)?.let { Text(it, color = HintColor, textAlign = TextAlign.Center) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onAction(DipoleAction.NewGameClicked) }) { Text("New game") }
            OutlinedButton(onClick = { onAction(DipoleAction.UndoClicked) }, enabled = session.canUndo) { Text("Undo") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onAction(DipoleAction.SaveClicked) }) { Text("Save") }
            TextButton(onClick = { onAction(DipoleAction.LoadClicked) }, enabled = uiState.hasSavedGames) { Text("Load") }
        }
        uiState.message?.let { message ->
            Text(message, color = Color.LightGray)
            LaunchedEffect(message) {
                delay(MESSAGE_MILLIS)
                onAction(DipoleAction.MessageShown)
            }
        }
        Rules()
    }

    val dismiss = { onAction(DipoleAction.DialogDismissed) }
    when (val dialog = uiState.dialog) {
        DipoleDialog.NewGame -> NewGameDialog(
            onModeChosen = { onAction(DipoleAction.ModeChosen(it)) },
            onDismiss = dismiss,
        )
        is DipoleDialog.ChooseDifficulty -> DifficultyDialog(
            suggested = dialog.suggested,
            onDifficultyChosen = { onAction(DipoleAction.DifficultyChosen(it)) },
            onBack = { onAction(DipoleAction.BackToModeClicked) },
            onDismiss = dismiss,
        )
        is DipoleDialog.Save -> SaveGameDialog(
            defaultName = dialog.defaultName,
            onSave = { onAction(DipoleAction.SaveConfirmed(it)) },
            onDismiss = dismiss,
        )
        is DipoleDialog.Load -> LoadGameDialog(
            savedGames = dialog.savedGames,
            onLoad = { onAction(DipoleAction.SavedGameChosen(it)) },
            onDelete = { onAction(DipoleAction.SavedGameDeleted(it)) },
            onDismiss = dismiss,
        )
        null -> Unit
    }
}

private const val MESSAGE_MILLIS = 2_000L
private const val BLINKS = 3
private const val BLINK_HALF_MILLIS = 200

/** Fades in and out [BLINKS] times whenever a new [hint] arrives, then stays at 0. */
@Composable
private fun blinkAlpha(hint: Hint?): Float {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(hint) {
        alpha.snapTo(0f)
        if (hint == null) return@LaunchedEffect
        repeat(BLINKS) {
            alpha.animateTo(1f, tween(BLINK_HALF_MILLIS))
            alpha.animateTo(0f, tween(BLINK_HALF_MILLIS))
        }
    }
    return alpha.value
}

private fun hintText(uiState: DipoleUiState): String? {
    if (!uiState.hintsOn) return null
    val hint = uiState.hint
    return when {
        uiState.selected == null -> "Hint: tap one of your stacks to see its best move."
        hint == null -> "Thinking about a hint…"
        hint.betterMoveElsewhere -> "Hint: the blinking move is this stack's best, but a better move exists elsewhere."
        else -> "Hint: the blinking move is the best one."
    }
}

private fun statusText(session: GameSession): String {
    val state = session.state
    val vsComputer = session.mode == GameMode.VsComputer
    state.winner?.let { winner ->
        return if (vsComputer) {
            if (winner == GameSession.HUMAN_SIDE) "You win!" else "The computer wins."
        } else {
            "$winner wins!"
        }
    }
    val prefix = if (session.opponentSatOut) "${state.toMove.opponent} has no moves and sits out. " else ""
    return prefix + when {
        session.isComputerTurn -> "Computer is thinking…"
        vsComputer -> "Your move (White)"
        else -> "${state.toMove} to move"
    }
}

/** A player's checkers on the board, next to the pile of their checkers removed from play. */
@Composable
private fun PlayerTray(state: GameState, player: Player, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(16.dp).background(player.color, CircleShape).border(1.dp, Color.Gray, CircleShape))
        Text(
            "$player: ${state.checkersOf(player)} on the board",
            color = Color.White,
            modifier = Modifier.padding(start = 8.dp).weight(1f),
        )
        Text("Off the board", color = Color.LightGray, fontSize = 13.sp, modifier = Modifier.padding(end = 8.dp))
        RemovedPile(player, state.removedCheckersOf(player))
    }
}

/** Draws [count] removed checkers as a physical stack of discs seen from the side. */
@Composable
private fun RemovedPile(player: Player, count: Int) {
    val discHeight = 8.dp
    val step = 3.dp
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(width = 36.dp, height = discHeight + step * (GameState.STARTING_STACK - 1)),
            contentAlignment = Alignment.BottomCenter,
        ) {
            if (count == 0) {
                Box(Modifier.size(width = 36.dp, height = discHeight).border(1.dp, Color.Gray, DiscShape))
            }
            repeat(count) { i ->
                Box(
                    Modifier
                        .offset(y = -step * i)
                        .size(width = 36.dp, height = discHeight)
                        .background(player.color, DiscShape)
                        .border(1.dp, Color.Gray, DiscShape),
                )
            }
        }
        Text(
            "$count",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 6.dp).widthIn(min = 20.dp),
        )
    }
}

private val DiscShape = RoundedCornerShape(50)

@Composable
private fun Board(
    state: GameState,
    selected: Square?,
    movable: Set<Square>,
    targets: Map<Square, Move>,
    lastMove: Move?,
    onSquareClick: (Square) -> Unit,
    hintedSquare: Square? = null,
    hintAlpha: Float = 0f,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.aspectRatio(1f).border(3.dp, Color(0xFF4A2E1A))) {
        val cell = maxWidth / BOARD_SIZE
        Column {
            // Row 8 (Black's home row) at the top, like a printed diagram.
            for (row in BOARD_SIZE - 1 downTo 0) {
                Row {
                    for (col in 0 until BOARD_SIZE) {
                        val square = Square(row, col)
                        val target = targets[square]
                        val highlighted = square == lastMove?.from || square == lastMove?.to
                        Box(
                            modifier = Modifier
                                .size(cell)
                                .background(if (square.isDark) DarkSquare else LightSquare)
                                .background(if (highlighted) LastMove else Color.Transparent)
                                .then(if (square.isDark) Modifier.clickable { onSquareClick(square) } else Modifier),
                            contentAlignment = Alignment.Center,
                        ) {
                            state.board[square]?.let { stack ->
                                Checker(
                                    stack = stack,
                                    isSelected = square == selected,
                                    isMovable = square in movable,
                                    modifier = Modifier.fillMaxSize(0.8f),
                                )
                            }
                            if (target != null) {
                                val capture = state.kindOf(target) is MoveKind.Capture
                                val color = if (capture) CaptureHighlight else Highlight
                                Box(
                                    Modifier
                                        .fillMaxSize(if (state.board[square] == null) 0.45f else 0.9f)
                                        .border(3.dp, color, CircleShape)
                                        .background(color.copy(alpha = 0.35f), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text("${target.count}", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (square == hintedSquare) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(HintColor.copy(alpha = 0.45f * hintAlpha))
                                        .border(4.dp, HintColor.copy(alpha = hintAlpha)),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Checker(stack: Stack, isSelected: Boolean, isMovable: Boolean, modifier: Modifier = Modifier) {
    val ring = when {
        isSelected -> Color(0xFFF2D95C)
        isMovable -> Color(0xFFB8A27A)
        else -> Color.Gray
    }
    Box(
        modifier
            .background(stack.owner.color, CircleShape)
            .border(if (isSelected) 4.dp else 2.dp, ring, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "${stack.size}",
            color = if (stack.owner == Player.White) Color.Black else Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
        )
    }
}

@Composable
private fun Rules() {
    Text(
        "How to play: tap one of your stacks, then a highlighted square. A stack (or part of it) moves " +
            "exactly as many squares as checkers are moved, jumping over anything in between. Plain moves " +
            "and merges go forward or diagonally forward; straight moves need an even number of checkers. " +
            "Captures (red) go in any direction and take a whole enemy stack no bigger than the moving one. " +
            "A plain move that would go past the edge of the board is allowed: that stack is removed from " +
            "play. Remove all enemy checkers to win.",
        color = Color.LightGray,
        fontSize = 13.sp,
        modifier = Modifier.widthIn(max = 560.dp),
    )
}

private val Player.color: Color get() = if (this == Player.White) WhiteChecker else BlackChecker

/** Arrow for a direction as seen on screen, with White at the bottom. */
private fun arrow(direction: Direction): String = when (direction) {
    Direction.North -> "↑"
    Direction.NorthEast -> "↗"
    Direction.NorthWest -> "↖"
    Direction.South -> "↓"
    Direction.SouthEast -> "↘"
    Direction.SouthWest -> "↙"
    Direction.East -> "→"
    Direction.West -> "←"
}
