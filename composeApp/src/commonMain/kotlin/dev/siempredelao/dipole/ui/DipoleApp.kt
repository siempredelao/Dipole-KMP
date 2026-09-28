package dev.siempredelao.dipole.ui

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.siempredelao.dipole.game.BOARD_SIZE
import dev.siempredelao.dipole.game.ComputerPlayer
import dev.siempredelao.dipole.game.Direction
import dev.siempredelao.dipole.game.GameState
import dev.siempredelao.dipole.game.Move
import dev.siempredelao.dipole.game.MoveKind
import dev.siempredelao.dipole.game.Player
import dev.siempredelao.dipole.game.Square
import dev.siempredelao.dipole.game.Stack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private val LightSquare = Color(0xFFEBD3A8)
private val DarkSquare = Color(0xFF7A4E2D)
private val WhiteChecker = Color(0xFFF5F0E6)
private val BlackChecker = Color(0xFF26211E)
private val Highlight = Color(0xFF7FC97F)
private val CaptureHighlight = Color(0xFFE0605A)
private val LastMove = Color(0x55F2D95C)

enum class Opponent { Computer, Human }

private val HumanSide = Player.White

@Composable
fun DipoleApp() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF1E1A17)) {
            DipoleScreen()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DipoleScreen() {
    var opponent by remember { mutableStateOf(Opponent.Computer) }
    var history by remember { mutableStateOf(listOf(GameState.initial())) }
    var lastMove by remember { mutableStateOf<Move?>(null) }
    var selected by remember { mutableStateOf<Square?>(null) }
    val state = history.last()
    val computerTurn = opponent == Opponent.Computer && state.toMove != HumanSide && !state.isOver

    fun play(move: Move) {
        history = history + state.play(move)
        lastMove = move
        selected = null
    }

    fun newGame() {
        history = listOf(GameState.initial())
        lastMove = null
        selected = null
    }

    fun undo() {
        var h = history.dropLast(1)
        if (opponent == Opponent.Computer) {
            while (h.size > 1 && h.last().toMove != HumanSide) h = h.dropLast(1)
        }
        if (h.isNotEmpty()) history = h
        lastMove = null
        selected = null
    }

    LaunchedEffect(history, opponent) {
        if (!computerTurn) return@LaunchedEffect
        delay(400)
        val move = withContext(Dispatchers.Default) { ComputerPlayer().chooseMove(state) }
        if (move != null) play(move)
    }

    val movesFromSelected = selected?.let { state.legalMovesFrom(it) }.orEmpty()
    val targets = movesFromSelected.filter { it.to.isOnBoard }.associateBy { it.to }
    val bearOffs = movesFromSelected.filter { !it.to.isOnBoard }
    val movable = if (computerTurn) emptySet() else state.legalMoves().map { it.from }.toSet()

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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = opponent == Opponent.Computer,
                onClick = { opponent = Opponent.Computer; newGame() },
                label = { Text("vs Computer") },
            )
            FilterChip(
                selected = opponent == Opponent.Human,
                onClick = { opponent = Opponent.Human; newGame() },
                label = { Text("2 Players") },
            )
        }
        Text(
            statusText(history, opponent, computerTurn),
            color = Color.White,
            fontSize = 18.sp,
            textAlign = TextAlign.Center,
        )
        val trayModifier = Modifier.widthIn(max = 560.dp).fillMaxWidth()
        PlayerTray(state, Player.Black, trayModifier)
        Board(
            state = state,
            selected = selected,
            movable = movable,
            targets = targets,
            lastMove = lastMove,
            onSquareClick = { square ->
                val target = targets[square]
                when {
                    target != null -> play(target)
                    square in movable -> selected = if (selected == square) null else square
                    else -> selected = null
                }
            },
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
                    OutlinedButton(onClick = { play(move) }) {
                        Text("Move ${move.count} off ${arrow(move.direction)}")
                    }
                }
            }
        } else if (selected != null) {
            Text("Tap a highlighted square. The number shows how many checkers move.", color = Color.LightGray)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = ::newGame) { Text("New game") }
            OutlinedButton(onClick = ::undo, enabled = history.size > 1 && !computerTurn) { Text("Undo") }
        }
        Rules()
    }
}

private fun statusText(history: List<GameState>, opponent: Opponent, computerTurn: Boolean): String {
    val state = history.last()
    state.winner?.let { winner ->
        return if (opponent == Opponent.Computer) {
            if (winner == HumanSide) "You win!" else "The computer wins."
        } else {
            "$winner wins!"
        }
    }
    val previous = history.getOrNull(history.size - 2)
    val sitOut = previous != null && previous.toMove == state.toMove
    val prefix = if (sitOut) "${state.toMove.opponent} has no moves and sits out. " else ""
    return prefix + when {
        computerTurn -> "Computer is thinking…"
        opponent == Opponent.Computer -> "Your move (White)"
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
