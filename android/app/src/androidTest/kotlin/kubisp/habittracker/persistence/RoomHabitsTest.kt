package kubisp.habittracker.persistence

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kubisp.habittracker.domain.fake.aHabit
import kubisp.habittracker.domain.fake.aNumericMeasurement
import kubisp.habittracker.domain.fake.decimal
import kubisp.habittracker.domain.habit.BuiltInUnit
import kubisp.habittracker.domain.habit.Direction
import kubisp.habittracker.domain.habit.NumericUnit
import kubisp.habittracker.persistence.habit.RoomHabits
import kubisp.habittracker.persistence.operation.RoomDataOperations
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomHabitsTest {
    private val database = anInMemoryDatabase()
    private val habits = RoomHabits(database.habitDao())
    private val dataOperations = RoomDataOperations(database)

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun `a habit with a built-in unit and the default step is read back unchanged`() = runTest {
        val water = aHabit(
            name = "Water",
            measurement = aNumericMeasurement(unit = NumericUnit.BuiltIn(BuiltInUnit.LITRES)),
            createdAt = LocalDateTime.of(2026, 11, 1, 8, 15, 42)
        )

        dataOperations.saveHabit(water)

        assertEquals(listOf(water), habits.habits().first())
    }

    @Test
    fun `a quit habit with a custom unit and a step set by the user and an archiving day is read back unchanged`() =
        runTest {
            val sweets = aHabit(
                name = "Sweets",
                direction = Direction.QUIT,
                measurement = aNumericMeasurement(
                    unit = NumericUnit.Custom("bars"),
                    plusStepSetByUser = decimal("0.125")
                ),
                archivedOn = LocalDate.of(2026, 11, 20)
            )

            dataOperations.saveHabit(sweets)

            assertEquals(sweets, habits.habit(sweets.id).first())
        }

    @Test
    fun `every built-in unit is read back as the same unit`() = runTest {
        val stored = BuiltInUnit.entries.map { aHabit(measurement = aNumericMeasurement(NumericUnit.BuiltIn(it))) }

        stored.forEach { dataOperations.saveHabit(it) }

        assertEquals(stored, habits.habits().first())
    }

    @Test
    fun `habits come in the order of addition archived ones too`() = runTest {
        val water = aHabit(name = "Water")
        val archived = aHabit(name = "Apples", archivedOn = LocalDate.of(2026, 11, 5))
        val walk = aHabit(name = "Walk")

        listOf(water, archived, walk).forEach { dataOperations.saveHabit(it) }

        assertEquals(listOf(water, archived, walk), habits.habits().first())
    }

    @Test
    fun `saving a stored habit replaces it and keeps its place in the order`() = runTest {
        val water = aHabit(name = "Water")
        val walk = aHabit(name = "Walk")
        dataOperations.saveHabit(water)
        dataOperations.saveHabit(walk)
        val changed = water.copy(name = "Tea", archivedOn = LocalDate.of(2026, 11, 22))

        dataOperations.saveHabit(changed)

        assertEquals(listOf(changed, walk), habits.habits().first())
    }

    @Test
    fun `a habit is read by its identity and is null before it is saved and the read follows its changes`() = runTest {
        val water = aHabit(name = "Water")

        habits.habit(water.id).test {
            assertNull(awaitItem())
            dataOperations.saveHabit(water)
            assertEquals(water, awaitItem())
            dataOperations.saveHabit(water.copy(name = "Tea"))
            assertEquals(water.copy(name = "Tea"), awaitItem())
        }
    }

    @Test
    fun `a write of another habit does not repeat an unchanged habit`() = runTest {
        val water = aHabit(name = "Water")
        dataOperations.saveHabit(water)

        habits.habit(water.id).test {
            assertEquals(water, awaitItem())
            dataOperations.saveHabit(aHabit(name = "Walk"))
            dataOperations.saveHabit(water.copy(name = "Tea"))
            assertEquals(water.copy(name = "Tea"), awaitItem())
        }
    }
}
