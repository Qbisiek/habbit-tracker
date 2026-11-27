package kubisp.habittracker.persistence.habit

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * The stored habit (Decision 35). [addedOrder] is the order of addition (`docs/DESIGN.md` section 4.6): an
 * AUTOINCREMENT key, so a number is never reused; [id] is the UUID of the domain habit.
 */
@Entity(tableName = "habit", indices = [Index(value = ["id"], unique = true)])
data class HabitRow(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "added_order")
    val addedOrder: Long,
    val id: String,
    val name: String,
    val direction: String,
    @ColumnInfo(name = "measurement_type")
    val measurementType: String,
    @ColumnInfo(name = "built_in_unit")
    val builtInUnit: String?,
    @ColumnInfo(name = "custom_unit")
    val customUnit: String?,
    @ColumnInfo(name = "plus_step_set_by_user")
    val plusStepSetByUser: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: String,
    @ColumnInfo(name = "archived_on")
    val archivedOn: String?
)
