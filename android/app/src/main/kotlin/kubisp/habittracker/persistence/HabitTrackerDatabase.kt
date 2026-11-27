package kubisp.habittracker.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kubisp.habittracker.persistence.entry.EntryDao
import kubisp.habittracker.persistence.entry.EntryRow
import kubisp.habittracker.persistence.habit.HabitDao
import kubisp.habittracker.persistence.habit.HabitRow

// Before v1 a schema change recreates the database without a migration (docs/PLAN.md, Conventions), so no schema is
// exported. The destructive fallback acts only when the stored version differs from `version`: every schema change
// before v1 must increase it.
@Database(entities = [HabitRow::class, EntryRow::class], version = 1, exportSchema = false)
abstract class HabitTrackerDatabase : RoomDatabase() {
    abstract fun habitDao(): HabitDao

    abstract fun entryDao(): EntryDao

    companion object {
        private const val FILE_NAME = "habit-tracker.db"

        fun open(context: Context, fileName: String = FILE_NAME): HabitTrackerDatabase =
            Room.databaseBuilder(context, HabitTrackerDatabase::class.java, fileName)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
