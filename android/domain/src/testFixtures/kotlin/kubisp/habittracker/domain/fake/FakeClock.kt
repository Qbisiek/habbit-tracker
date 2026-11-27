package kubisp.habittracker.domain.fake

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kubisp.habittracker.domain.port.Clock

/**
 * A clock whose time moves only when the test moves it (Decision 13). Like the real clock, it signals a change of
 * day when the time passes midnight of its time zone, and a wait ends when the time reaches its instant.
 */
class FakeClock(start: Instant = Instant.parse("2026-11-10T09:00:00Z"), timeZone: ZoneId = ZoneId.of("Europe/Warsaw")) :
    Clock {
    private val currentInstant = MutableStateFlow(start)
    private var currentTimeZone = timeZone
    private val tickEvents = MutableSharedFlow<Instant>(extraBufferCapacity = EVENT_BUFFER)
    private val dayOrTimeZoneChangeEvents = MutableSharedFlow<Unit>(extraBufferCapacity = EVENT_BUFFER)

    /** Lets a test check that ticks are collected only while they are needed (`docs/DESIGN.md` section 5.4). */
    val tickSubscriberCount: Int get() = tickEvents.subscriptionCount.value

    override fun now(): Instant = currentInstant.value

    override fun timeZone(): ZoneId = currentTimeZone

    override fun ticks(): Flow<Instant> = tickEvents

    override fun dayOrTimeZoneChanges(): Flow<Unit> = dayOrTimeZoneChangeEvents

    override suspend fun waitUntil(instant: Instant) {
        currentInstant.first { !it.isBefore(instant) }
    }

    fun setNow(instant: Instant) {
        val dayBefore = today()
        currentInstant.value = instant
        if (today() != dayBefore) emit(dayOrTimeZoneChangeEvents, Unit)
    }

    fun advanceBy(duration: Duration) {
        setNow(now().plus(duration))
    }

    fun changeTimeZone(zone: ZoneId) {
        currentTimeZone = zone
        emit(dayOrTimeZoneChangeEvents, Unit)
    }

    fun tick() {
        emit(tickEvents, now())
    }

    private fun today(): LocalDate = LocalDate.ofInstant(now(), currentTimeZone)

    private fun <T> emit(events: MutableSharedFlow<T>, event: T) {
        check(events.tryEmit(event)) { "A subscriber of the fake clock does not collect its events" }
    }

    private companion object {
        const val EVENT_BUFFER = 64
    }
}
