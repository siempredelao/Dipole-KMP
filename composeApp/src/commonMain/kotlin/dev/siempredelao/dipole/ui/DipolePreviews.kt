package dev.siempredelao.dipole.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.siempredelao.dipole.game.GameState
import dev.siempredelao.dipole.game.Player
import dev.siempredelao.dipole.game.Square
import dev.siempredelao.dipole.game.Stack

@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun DipoleScreenPreview() {
    DipoleTheme {
        DipoleScreen()
    }
}

/** A position part way through a game, so the off-board stacks have checkers in them. */
@Preview(widthDp = 420, heightDp = 900)
@Composable
private fun DipoleScreenMidGamePreview() {
    DipoleTheme {
        DipoleScreen(
            initialState = GameState(
                board = mapOf(
                    Square(0, 2) to Stack(Player.White, 5), // c1
                    Square(3, 5) to Stack(Player.White, 3), // f4
                    Square(7, 3) to Stack(Player.Black, 4), // d8
                    Square(4, 4) to Stack(Player.Black, 2), // e5
                    Square(5, 1) to Stack(Player.Black, 1), // b6
                ),
                toMove = Player.White,
            ),
        )
    }
}
