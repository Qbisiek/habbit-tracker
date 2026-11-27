package kubisp.habittracker.domain.fake

import app.cash.turbine.test
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FakeClockTest {
    private val start = Instant.parse("2026-11-10T22:00:00Z")
    private val clock = FakeClock(start = start, timeZone = ZoneId.of("Europe/Warsaw"))

    @Test
    fun `the time moves only when the test moves it`() {
        clock.advanceBy(Duration.ofMinutes(5))

        assertEquals(start.plus(Duration.ofMinutes(5)), clock.now())
    }

    @Test
    fun `a wait ends when the time reaches its instant, not before`() = runTest {
        var hasEnded = false
        launch {
            clock.waitUntil(start.plusSeconds(10))
            hasEnded = true
        }
        clock.advanceBy(Duration.ofSeconds(9))
        runCurrent()
        val hasEndedEarly = hasEnded

        clock.advanceBy(Duration.ofSeconds(1))
        runCurrent()

        assertFalse(hasEndedEarly)
        assertTrue(hasEnded)
    }

    @Test
    fun `a wait for an instant that has passed ends at once`() = runTest {
        clock.waitUntil(start.minusSeconds(1))

        assertEquals(start, clock.now())
    }

    @Test
    fun `moving the time past midnight of the time zone signals a change of day`() = runTest {
        clock.dayOrTimeZoneChanges().test {
            clock.advanceBy(Duration.ofMinutes(59))
            expectNoEvents()

            clock.advanceBy(Duration.ofMinutes(1))

            awaitItem()
        }
    }

    @Test
    fun `a change of the time zone is signalled`() = runTest {
        val newYork = ZoneId.of("America/New_York")

        clock.dayOrTimeZoneChanges().test {
            clock.changeTimeZone(newYork)

            awaitItem()
            assertEquals(newYork, clock.timeZone())
        }
    }

    @Test
    fun `a tick emits the current instant to its subscribers`() = runTest {
        clock.ticks().test {
            clock.advanceBy(Duration.ofSeconds(1))
            clock.tick()

            assertEquals(start.plusSeconds(1), awaitItem())
            assertEquals(1, clock.tickSubscriberCount)
        }
    }

    @Test
    fun `ticks have no subscriber until one collects them`() {
        val subscribers = clock.tickSubscriberCount

        assertEquals(0, subscribers)
    }
}
