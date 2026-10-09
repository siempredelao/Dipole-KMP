package gc.david.dipole.review

import com.russhwolf.settings.MapSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class SettingsReviewStoreTest {

    @Test
    fun startsEmpty() {
        assertEquals(ReviewStats(), SettingsReviewStore(MapSettings()).stats)
    }

    @Test
    fun keepsWhatItIsGiven() {
        val settings = MapSettings()
        val stats = ReviewStats(
            finishedGames = 7,
            launches = 3,
            firstLaunchAt = Instant.fromEpochMilliseconds(1_000),
            lastRequestAt = Instant.fromEpochMilliseconds(2_000),
        )
        SettingsReviewStore(settings).update { stats }

        assertEquals(stats, SettingsReviewStore(settings).stats)
    }

    @Test
    fun usesItsOwnKeys() {
        val settings = MapSettings("sound_on" to true)
        SettingsReviewStore(settings).update { it.copy(finishedGames = 1, firstLaunchAt = Instant.fromEpochMilliseconds(1)) }

        assertTrue(settings.keys.minus("sound_on").all { it.startsWith("review_") })
        assertEquals(true, settings.getBoolean("sound_on", false))
    }
}
