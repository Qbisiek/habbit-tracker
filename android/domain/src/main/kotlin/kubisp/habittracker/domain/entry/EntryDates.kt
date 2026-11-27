package kubisp.habittracker.domain.entry

import java.time.LocalDate

/** BHV-ENTRY-R3: entries can be added on [today] and on past dates, never on a date after today (Decision 40). */
fun canAddEntriesOn(date: LocalDate, today: LocalDate): Boolean = !date.isAfter(today)
