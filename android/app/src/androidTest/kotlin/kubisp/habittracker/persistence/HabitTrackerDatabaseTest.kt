package kubisp.habittracker.persistence

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kubisp.habittracker.domain.fake.aHabit
import kubisp.habittracker.domain.fake.aNumericEntry
import kubisp.habittracker.persistence.entry.RoomEntries
import kubisp.habittracker.persistence.habit.RoomHabits
import kubisp.habittracker.persistence.operation.RoomDataOperations
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HabitTrackerDatabaseTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val fileName = "habit-tracker-test.db"

    @Before
    @After
    fun deleteDatabaseFile() {
        context.deleteDatabase(fileName)
    }

    @Test
    fun `REQ-SC1 habits and entries are kept after the database file is closed and opened again`() = runTest {
        val water = aHabit(name = "Water")
        val entry = aNumericEntry(water.id)
        val day = LocalDate.of(2026, 11, 10)
        val written = HabitTrackerDatabase.open(context, fileName)
        RoomDataOperations(written).saveHabit(water)
        RoomEntries(written).add(entry)
        written.close()

        val reopened = HabitTrackerDatabase.open(context, fileName)
        val habits = RoomHabits(reopened.habitDao()).habits().first()
        val entries = RoomEntries(reopened).entriesOverlapping(water.id, day..day, ZoneId.of("Europe/Warsaw")).first()
        reopened.close()

        assertEquals(listOf(water), habits)
        assertEquals(listOf(entry), entries)
    }
}
