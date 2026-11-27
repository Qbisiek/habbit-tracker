package kubisp.habittracker.persistence.habit

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import kubisp.habittracker.domain.habit.BuiltInUnit
import kubisp.habittracker.domain.habit.Direction
import kubisp.habittracker.domain.habit.Habit
import kubisp.habittracker.domain.habit.HabitId
import kubisp.habittracker.domain.habit.Measurement
import kubisp.habittracker.domain.habit.NumericUnit
import kubisp.habittracker.persistence.NOT_YET_ADDED
import kubisp.habittracker.persistence.exactDecimalFromStored
import kubisp.habittracker.persistence.toStored

// Stored codes are fixed strings, not enum names, so renaming a Kotlin constant does not change stored data.
private const val BUILD = "build"
private const val QUIT = "quit"
private const val NUMERIC = "numeric"

fun Habit.toRow(addedOrder: Long = NOT_YET_ADDED): HabitRow = when (val measurement = measurement) {
    is Measurement.Numeric -> HabitRow(
        addedOrder = addedOrder,
        id = id.value.toString(),
        name = name,
        direction = direction.toStored(),
        measurementType = NUMERIC,
        builtInUnit = (measurement.unit as? NumericUnit.BuiltIn)?.unit?.id,
        customUnit = (measurement.unit as? NumericUnit.Custom)?.label,
        plusStepSetByUser = measurement.plusStepSetByUser?.toStored(),
        createdAt = createdAt.toString(),
        archivedOn = archivedOn?.toString()
    )
}

fun HabitRow.toDomain(): Habit = Habit(
    id = HabitId(UUID.fromString(id)),
    name = name,
    direction = directionFromStored(direction),
    measurement = measurement(),
    createdAt = LocalDateTime.parse(createdAt),
    archivedOn = archivedOn?.let(LocalDate::parse)
)

private fun Direction.toStored(): String = when (this) {
    Direction.BUILD -> BUILD
    Direction.QUIT -> QUIT
}

private fun directionFromStored(stored: String): Direction = when (stored) {
    BUILD -> Direction.BUILD
    QUIT -> Direction.QUIT
    else -> error("Unknown stored direction: $stored")
}

private fun HabitRow.measurement(): Measurement = when (measurementType) {
    NUMERIC -> Measurement.Numeric(
        unit = numericUnit(),
        plusStepSetByUser = plusStepSetByUser?.let(::exactDecimalFromStored)
    )

    else -> error("Unknown stored measurement type: $measurementType")
}

private fun HabitRow.numericUnit(): NumericUnit {
    if (builtInUnit != null) return NumericUnit.BuiltIn(builtInUnitFromStored(builtInUnit))
    return NumericUnit.Custom(checkNotNull(customUnit) { "Habit $id has neither a built-in nor a custom unit" })
}

private fun builtInUnitFromStored(stored: String): BuiltInUnit =
    BuiltInUnit.entries.find { it.id == stored } ?: error("Unknown stored built-in unit: $stored")
