package kubisp.habittracker.domain.habit

import java.time.LocalDate
import kubisp.habittracker.domain.fake.aHabit
import org.junit.Assert.assertEquals
import org.junit.Test

class HabitsScreenHabitsTest {
    @Test
    fun `BHV-HABIT-R4 the list shows the habits that are not archived, by name for the app language`() {
        val skating = aHabit(name = "Łyżwy")
        val ice = aHabit(name = "Lody")
        val archived = aHabit(name = "Archived", archivedOn = LocalDate.of(2026, 11, 9))

        val listed = habitsOnHabitsScreen(listOf(skating, archived, ice), languageTag = "pl")

        assertEquals(listOf(ice, skating), listed)
    }
}
