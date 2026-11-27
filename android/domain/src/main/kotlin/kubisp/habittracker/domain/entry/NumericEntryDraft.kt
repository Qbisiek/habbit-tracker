package kubisp.habittracker.domain.entry

import java.time.LocalDate
import java.time.LocalTime

/** [value] as typed; the seconds of [time] are ignored, a typed time is to the minute (BHV-ENTRY-R6b). */
data class NumericEntryDraft(val value: String, val date: LocalDate, val time: LocalTime)
