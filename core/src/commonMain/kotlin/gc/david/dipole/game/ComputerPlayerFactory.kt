package gc.david.dipole.game

/** Gives the computer player for a difficulty; lets the game be tested with a weaker, faster one. */
fun interface ComputerPlayerFactory {

    fun forDifficulty(difficulty: Difficulty): ComputerPlayer
}
