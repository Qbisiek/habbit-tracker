package kubisp.habittracker.domain.fake

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kubisp.habittracker.domain.decimal.ExactDecimal
import kubisp.habittracker.domain.entry.Entry
import kubisp.habittracker.domain.entry.EntryId
import kubisp.habittracker.domain.habit.BuiltInUnit
import kubisp.habittracker.domain.habit.Direction
import kubisp.habittracker.domain.habit.Habit
import kubisp.habittracker.domain.habit.HabitId
import kubisp.habittracker.domain.habit.Measurement
import kubisp.habittracker.domain.habit.NumericUnit

fun decimal(value: String): ExactDecimal = ExactDecimal.of(BigDecimal(value))

fun aNumericMeasurement(
    unit: NumericUnit = NumericUnit.BuiltIn(BuiltInUnit.LITRES),
    plusStepSetByUser: ExactDecimal? = null
): Measurement.Numeric = Measurement.Numeric(unit = unit, plusStepSetByUser = plusStepSetByUser)

fun aHabit(
    id: HabitId = HabitId(UUID.randomUUID()),
    name: String = "Water",
    direction: Direction = Direction.BUILD,
    measurement: Measurement = aNumericMeasurement(),
    createdAt: LocalDateTime = LocalDateTime.of(2026, 11, 1, 8, 0),
    archivedOn: LocalDate? = null
): Habit = Habit(
    id = id,
    name = name,
    direction = direction,
    measurement = measurement,
    createdAt = createdAt,
    archivedOn = archivedOn
)

fun aNumericEntry(
    habitId: HabitId,
    id: EntryId = EntryId(UUID.randomUUID()),
    value: ExactDecimal = decimal("1"),
    dateTime: LocalDateTime = LocalDateTime.of(2026, 11, 10, 12, 0)
): Entry.Numeric = Entry.Numeric(id = id, habitId = habitId, value = value, dateTime = dateTime)
