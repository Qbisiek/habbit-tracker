package kubisp.habittracker.domain.habit

import java.time.LocalDate
import java.time.LocalDateTime

/** [archivedOn] is null while the habit is not archived (Decision 43). */
data class Habit(
    val id: HabitId,
    val name: String,
    val direction: Direction,
    val measurement: Measurement,
    val createdAt: LocalDateTime,
    val archivedOn: LocalDate?
)
