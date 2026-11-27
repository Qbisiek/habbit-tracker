package kubisp.habittracker.persistence.entry

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kubisp.habittracker.persistence.habit.HabitRow

/**
 * The stored Numeric entry (Decision 35); the other shapes come with the full schema (T3.2). [addedOrder] is the
 * order of addition, the last tie-break of BHV-ENTRY-R6b. [date] and [time] are the wall-clock date and time in ISO
 * text: ISO dates compare as text in calendar order, so the date range of a read is a text comparison.
 */
@Entity(
    tableName = "entry",
    foreignKeys = [
        ForeignKey(
            entity = HabitRow::class,
            parentColumns = ["id"],
            childColumns = ["habit_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["id"], unique = true), Index(value = ["habit_id", "date"])]
)
data class EntryRow(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "added_order")
    val addedOrder: Long,
    val id: String,
    @ColumnInfo(name = "habit_id")
    val habitId: String,
    val value: String,
    val date: String,
    val time: String
)
