package kubisp.habittracker.domain.entry

import java.time.LocalDateTime
import kubisp.habittracker.domain.decimal.ExactDecimal
import kubisp.habittracker.domain.habit.HabitId

/** The shape of an entry follows the measurement of its habit (Decision 92); Binary and Duration come in T3.1. */
sealed interface Entry {
    val id: EntryId
    val habitId: HabitId

    /** [dateTime]: wall-clock date and time, kept as entered after a time zone change (BHV-ENTRY-R2, Decision 80). */
    data class Numeric(
        override val id: EntryId,
        override val habitId: HabitId,
        val value: ExactDecimal,
        val dateTime: LocalDateTime
    ) : Entry
}
