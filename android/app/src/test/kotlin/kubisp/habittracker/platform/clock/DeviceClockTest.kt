package kubisp.habittracker.platform.clock

import app.cash.turbine.test
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceClockTest {
    private val start = Instant.parse("2026-11-10T22:30:00Z")
    private var deviceClockShift = Duration.ZERO
    private var deviceTimeZone = ZoneId.of("Europe/Warsaw")
    private val systemTimeChanges = MutableSharedFlow<Unit>()

    private fun TestScope.deviceClock() = DeviceClock(
        systemTimeChanges = systemTimeChanges,
        wallClock = { start.plusMillis(testScheduler.currentTime).plus(deviceClockShift) },
        deviceTimeZone = { deviceTimeZone },
        elapsedRealtimeNanos = { Duration.ofMillis(testScheduler.currentTime).toNanos() }
    )

    @Test
    fun `now and the time zone are those of the device`() = runTest {
        val clock = deviceClock()
        deviceClockShift = Duration.ofHours(1)
        deviceTimeZone = ZoneId.of("Europe/London")

        val now = clock.now()
        val timeZone = clock.timeZone()

        assertEquals(start.plus(Duration.ofHours(1)), now)
        assertEquals(ZoneId.of("Europe/London"), timeZone)
    }

    @Test
    fun `ticks emit the current instant at once and then every second`() = runTest {
        val clock = deviceClock()

        clock.ticks().test {
            assertEquals(start, awaitItem())
            assertEquals(start.plusSeconds(1), awaitItem())
            assertEquals(start.plusSeconds(2), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a slow subscriber gets the next tick on the one-second grid, not a burst of missed ticks`() = runTest {
        val clock = deviceClock()
        val tickTimes = mutableListOf<Long>()

        clock.ticks().take(3).collect {
            tickTimes += currentTime
            delay(SLOW_SUBSCRIBER_MILLIS)
        }

        assertEquals(listOf(0L, 2_000L, 4_000L), tickTimes)
    }

    @Test
    fun `a wait ends when its instant is reached, not before`() = runTest {
        val clock = deviceClock()
        var hasEnded = false
        launch {
            clock.waitUntil(start.plusSeconds(10))
            hasEnded = true
        }

        advanceTimeBy(9_999)
        runCurrent()
        val hasEndedEarly = hasEnded
        advanceTimeBy(1)
        runCurrent()

        assertFalse(hasEndedEarly)
        assertTrue(hasEnded)
    }

    @Test
    fun `moving the device clock forward during a wait does not shorten it`() = runTest {
        val clock = deviceClock()
        var hasEnded = false
        launch {
            clock.waitUntil(start.plusSeconds(10))
            hasEnded = true
        }
        advanceTimeBy(1_000)

        deviceClockShift = Duration.ofHours(1)
        advanceTimeBy(8_999)
        runCurrent()
        val hasEndedEarly = hasEnded
        advanceTimeBy(1)
        runCurrent()

        assertFalse(hasEndedEarly)
        assertTrue(hasEnded)
    }

    @Test
    fun `a wait for an instant that has passed ends at once`() = runTest {
        val clock = deviceClock()

        clock.waitUntil(start.minusSeconds(1))

        assertEquals(0L, currentTime)
    }

    @Test
    fun `a system time event on the same day and in the same time zone signals nothing`() = runTest {
        val clock = deviceClock()

        clock.dayOrTimeZoneChanges().test {
            advanceTimeBy(Duration.ofMinutes(29).toMillis())
            systemTimeChanges.emit(Unit)

            expectNoEvents()
        }
    }

    @Test
    fun `the first system time event after midnight signals a change of day, once`() = runTest {
        val clock = deviceClock()

        clock.dayOrTimeZoneChanges().test {
            advanceTimeBy(Duration.ofMinutes(30).toMillis())
            systemTimeChanges.emit(Unit)
            awaitItem()
            systemTimeChanges.emit(Unit)

            expectNoEvents()
        }
    }

    @Test
    fun `a change of the time zone signals a change, also when the date stays the same`() = runTest {
        val clock = deviceClock()

        clock.dayOrTimeZoneChanges().test {
            deviceTimeZone = ZoneId.of("Europe/London")
            systemTimeChanges.emit(Unit)

            awaitItem()
        }
    }

    @Test
    fun `setting the device clock back to the previous day signals a change of day`() = runTest {
        val clock = deviceClock()

        clock.dayOrTimeZoneChanges().test {
            deviceClockShift = Duration.ofDays(-1)
            systemTimeChanges.emit(Unit)

            awaitItem()
        }
    }

    private companion object {
        const val SLOW_SUBSCRIBER_MILLIS = 1_500L
    }
}
