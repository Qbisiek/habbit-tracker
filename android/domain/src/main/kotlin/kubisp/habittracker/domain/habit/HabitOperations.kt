package kubisp.habittracker.domain.habit

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kubisp.habittracker.domain.operation.DomainLock
import kubisp.habittracker.domain.port.Clock
import kubisp.habittracker.domain.port.DataOperations

/** `docs/DESIGN.md` section 3, Decision 94. */
class HabitOperations(
    private val lock: DomainLock,
    private val dataOperations: DataOperations,
    private val clock: Clock
) {
    suspend fun createHabit(draft: HabitDraft): CreateHabitResult = when (val checked = checkHabitDraft(draft)) {
        is CheckedHabitDraft.Invalid -> CreateHabitResult.Rejected(checked.problems)

        is CheckedHabitDraft.Valid -> lock.withLock {
            val habit = newHabit(draft, checked.measurement, createdAt = wallClockNow())
            dataOperations.saveHabit(habit)
            CreateHabitResult.Created(habit)
        }
    }

    private fun wallClockNow(): LocalDateTime =
        LocalDateTime.ofInstant(clock.now(), clock.timeZone()).truncatedTo(ChronoUnit.SECONDS)
}

private fun newHabit(draft: HabitDraft, measurement: Measurement, createdAt: LocalDateTime): Habit = Habit(
    id = HabitId(UUID.randomUUID()),
    name = draft.name.trim(),
    direction = draft.direction,
    measurement = measurement.withTrimmedLabels(),
    createdAt = createdAt,
    archivedOn = null
)

private fun Measurement.withTrimmedLabels(): Measurement = when (this) {
    is Measurement.Numeric -> copy(unit = unit.withTrimmedLabel())
}

private fun NumericUnit.withTrimmedLabel(): NumericUnit = when (this) {
    is NumericUnit.BuiltIn -> this
    is NumericUnit.Custom -> NumericUnit.Custom(label.trim())
}
