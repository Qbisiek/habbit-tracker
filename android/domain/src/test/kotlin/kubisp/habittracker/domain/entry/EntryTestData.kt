package kubisp.habittracker.domain.entry

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kubisp.habittracker.domain.fake.FakeClock

val WARSAW: ZoneId = ZoneId.of("Europe/Warsaw")

/** "Today" of the scenarios: Wednesday 11 Nov 2026 (`docs/REQUIREMENTS.md` section 7). */
val TODAY: LocalDate = LocalDate.of(2026, 11, 11)

fun nov(day: Int): LocalDate = LocalDate.of(2026, 11, day)

fun nov(day: Int, hour: Int, minute: Int, second: Int = 0): LocalDateTime = nov(day).atTime(hour, minute, second)

fun clockAt(moment: LocalDateTime, timeZone: ZoneId = WARSAW): FakeClock =
    FakeClock(start = moment.atZone(timeZone).toInstant(), timeZone = timeZone)

fun draft(value: String, date: LocalDate, hour: Int, minute: Int): NumericEntryDraft =
    NumericEntryDraft(value = value, date = date, time = LocalTime.of(hour, minute))
