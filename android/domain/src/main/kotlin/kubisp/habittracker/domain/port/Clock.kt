package kubisp.habittracker.domain.port

import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow

/** The only source of time for Domain and Presentation (`docs/DESIGN.md` section 5.4, Decisions 13 and 58). */
interface Clock {
    fun now(): Instant

    /** The device time zone; calendar days are days in this zone (Decision 13). */
    fun timeZone(): ZoneId

    /** Emits the current instant every second, only while it has a subscriber. */
    fun ticks(): Flow<Instant>

    /**
     * Emits when the calendar day or the time zone changes after the subscription; a change while nothing collects is
     * not signalled, so a subscriber computes "today" again whenever it subscribes.
     */
    fun dayOrTimeZoneChanges(): Flow<Unit>

    /**
     * Returns when [instant] is reached. The remaining time is measured from the call on a monotonic clock, so moving
     * the device clock does not shorten or lengthen the wait (`docs/DESIGN.md` section 5.7).
     */
    suspend fun waitUntil(instant: Instant)
}
