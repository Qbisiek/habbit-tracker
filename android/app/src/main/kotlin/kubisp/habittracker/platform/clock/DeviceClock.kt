package kubisp.habittracker.platform.clock

import android.os.SystemClock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.Duration as KotlinDuration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kubisp.habittracker.domain.port.Clock

/**
 * The Clock port on the device (`docs/DESIGN.md` section 5.4): the only place that reads the device time and time
 * zone. The constructor parameter `systemTimeChanges` signals the moments after which the day or the time zone may
 * have changed; in production it is the stream of the function `systemTimeChanges` with the application context.
 */
class DeviceClock(
    private val systemTimeChanges: Flow<Unit>,
    private val wallClock: () -> Instant = Instant::now,
    private val deviceTimeZone: () -> ZoneId = ZoneId::systemDefault,
    private val elapsedRealtimeNanos: () -> Long = SystemClock::elapsedRealtimeNanos
) : Clock {
    override fun now(): Instant = wallClock()

    override fun timeZone(): ZoneId = deviceTimeZone()

    // Each tick is placed on the one-second grid of the first one, so the time a subscriber spends on a tick does not
    // add up to a drift, and a late tick is followed by the next one on the grid, not by a burst of missed ticks.
    override fun ticks(): Flow<Instant> = flow {
        val firstTick = elapsedRealtimeNanos()
        while (true) {
            emit(now())
            delay(timeUntilNextTick(firstTick))
        }
    }

    override fun dayOrTimeZoneChanges(): Flow<Unit> = systemTimeChanges
        .onStart { emit(Unit) }
        .map { currentCalendarDay() }
        .distinctUntilChanged()
        .drop(1)
        .map { }

    // `delay` runs on the monotonic clock of the dispatcher, not on the device clock, so moving the device clock does
    // not shorten or lengthen the wait (`docs/DESIGN.md` section 5.7).
    override suspend fun waitUntil(instant: Instant) {
        delay(Duration.between(now(), instant).toKotlinDuration())
    }

    private fun timeUntilNextTick(firstTick: Long): KotlinDuration {
        val intoCurrentSecond = (elapsedRealtimeNanos() - firstTick) % TICK_INTERVAL.inWholeNanoseconds
        return TICK_INTERVAL - intoCurrentSecond.nanoseconds
    }

    private fun currentCalendarDay(): CalendarDay {
        val timeZone = timeZone()
        return CalendarDay(now().atZone(timeZone).toLocalDate(), timeZone)
    }

    private data class CalendarDay(val date: LocalDate, val timeZone: ZoneId)

    private companion object {
        val TICK_INTERVAL = 1.seconds
    }
}
