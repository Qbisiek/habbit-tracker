package kubisp.habittracker.persistence

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry

fun anInMemoryDatabase(): HabitTrackerDatabase {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    return Room.inMemoryDatabaseBuilder(context, HabitTrackerDatabase::class.java).build()
}
