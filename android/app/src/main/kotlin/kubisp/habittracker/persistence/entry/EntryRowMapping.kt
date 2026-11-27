package kubisp.habittracker.persistence.entry

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID
import kubisp.habittracker.domain.entry.Entry
import kubisp.habittracker.domain.entry.EntryId
import kubisp.habittracker.domain.habit.HabitId
import kubisp.habittracker.persistence.NOT_YET_ADDED
import kubisp.habittracker.persistence.exactDecimalFromStored
import kubisp.habittracker.persistence.toStored

fun Entry.toRow(addedOrder: Long = NOT_YET_ADDED): EntryRow = when (this) {
    is Entry.Numeric -> EntryRow(
        addedOrder = addedOrder,
        id = id.value.toString(),
        habitId = habitId.value.toString(),
        value = value.toStored(),
        date = dateTime.toLocalDate().toString(),
        time = dateTime.toLocalTime().toString()
    )
}

fun EntryRow.toDomain(): Entry = Entry.Numeric(
    id = EntryId(UUID.fromString(id)),
    habitId = HabitId(UUID.fromString(habitId)),
    value = exactDecimalFromStored(value),
    dateTime = LocalDateTime.of(LocalDate.parse(date), LocalTime.parse(time))
)
