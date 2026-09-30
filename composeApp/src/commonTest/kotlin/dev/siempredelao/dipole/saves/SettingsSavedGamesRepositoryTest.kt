package dev.siempredelao.dipole.saves

import com.russhwolf.settings.MapSettings
import dev.siempredelao.dipole.game.GameMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsSavedGamesRepositoryTest {

    private val settings = MapSettings()
    private val repository = SettingsSavedGamesRepository(settings)

    private fun game(id: String, savedAt: Long) = SavedGame(id, "Game $id", savedAt, GameMode.TwoPlayers, emptyList())

    @Test
    fun listsSavesNewestFirst() {
        repository.save(game("a", 1))
        repository.save(game("b", 3))
        repository.save(game("c", 2))
        assertEquals(listOf("b", "c", "a"), repository.list().map { it.id })
    }

    @Test
    fun savingTheSameIdReplacesIt() {
        repository.save(game("a", 1))
        repository.save(game("a", 1).copy(name = "Renamed"))
        assertEquals(listOf("Renamed"), repository.list().map { it.name })
    }

    @Test
    fun deleteRemovesTheSave() {
        repository.save(game("a", 1))
        repository.save(game("b", 2))
        repository.delete("a")
        assertEquals(listOf("b"), repository.list().map { it.id })
        assertTrue(settings.keys.none { it.endsWith("_a") })
    }

    @Test
    fun corruptedSavesAreSkipped() {
        repository.save(game("a", 1))
        settings.putString("saved_game_a", "garbage")
        assertTrue(repository.list().isEmpty())
    }
}
