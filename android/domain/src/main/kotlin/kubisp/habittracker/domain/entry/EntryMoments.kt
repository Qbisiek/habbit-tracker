package kubisp.habittracker.domain.entry

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * The default moment of the numeric entry form (`docs/DESIGN.md` section 5.8): now cut to the whole minute on today's
 * date, 12:00 on another date.
 */
fun defaultMomentOfNewEntry(selectedDate: LocalDate, now: Instant, timeZone: ZoneId): LocalDateTime =
    momentOnDate(selectedDate, now, timeZone, ChronoUnit.MINUTES)

/** "+" stores now with its seconds on today's date and 12:00:00 on another date (`docs/DESIGN.md` section 5.8). */
internal fun plusStepMoment(selectedDate: LocalDate, now: Instant, timeZone: ZoneId): LocalDateTime =
    momentOnDate(selectedDate, now, timeZone, ChronoUnit.SECONDS)

/**
 * BHV-ENTRY-R6b: the moment to store for [draft]. A time whose hour and minute equal those of [stored] keeps its
 * stored seconds, also when the date changed; any other time is to the minute.
 */
internal fun momentOfDraft(draft: NumericEntryDraft, stored: Entry.Numeric?): LocalDateTime {
    val storedTime = stored?.dateTime?.toLocalTime()
    val time = if (storedTime != null && hasSameHourAndMinute(draft.time, storedTime)) {
        storedTime
    } else {
        draft.time.truncatedTo(ChronoUnit.MINUTES)
    }
    return draft.date.atTime(time)
}

/** BHV-ENTRY-R5: a new moment, or a moment whose date, hour or minute differ from the stored ones. */
internal fun isMomentChanged(draft: NumericEntryDraft, stored: Entry.Numeric?): Boolean {
    if (stored == null) return true
    val storedDate = stored.dateTime.toLocalDate()
    return draft.date != storedDate || !hasSameHourAndMinute(draft.time, stored.dateTime.toLocalTime())
}

/**
 * A wall-clock moment becomes an instant only to be compared with now, by the default rule of `java.time` (a time in
 * a gap moves forward by the gap, a doubled time takes the earlier offset; `docs/DESIGN.md` section 5.8).
 */
internal fun isAfterNow(moment: LocalDateTime, now: Instant, timeZone: ZoneId): Boolean =
    moment.atZone(timeZone).toInstant().isAfter(now)

private fun momentOnDate(date: LocalDate, now: Instant, timeZone: ZoneId, precision: ChronoUnit): LocalDateTime {
    val current = LocalDateTime.ofInstant(now, timeZone)
    if (date != current.toLocalDate()) return date.atTime(LocalTime.NOON)
    return current.truncatedTo(precision)
}

private fun hasSameHourAndMinute(first: LocalTime, second: LocalTime): Boolean =
    first.hour == second.hour && first.minute == second.minute
