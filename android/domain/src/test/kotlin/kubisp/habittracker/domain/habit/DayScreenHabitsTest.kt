package kubisp.habittracker.domain.habit

import java.time.LocalDate
import java.time.LocalDateTime
import kubisp.habittracker.domain.fake.aHabit
import org.junit.Assert.assertEquals
import org.junit.Test

class DayScreenHabitsTest {
    @Test
    fun `BHV-HABIT-2 two habits with the same name are both listed, the older one first`() {
        val newer = aHabit(name = "Reading", createdAt = LocalDateTime.of(2026, 11, 10, 8, 0))
        val older = aHabit(name = "Reading", createdAt = LocalDateTime.of(2026, 11, 9, 8, 0))

        val shown = habitsOnDayScreen(listOf(newer, older), languageTag = "en")

        assertEquals(listOf(older, newer), shown)
    }

    @Test
    fun `BHV-HABIT-3 habits are ordered by name ignoring case`() {
        val habits = listOf(aHabit(name = "banana"), aHabit(name = "Apple"), aHabit(name = "cherry"))

        val shown = habitsOnDayScreen(habits, languageTag = "en")

        assertEquals(listOf("Apple", "banana", "cherry"), shown.map { it.name })
    }

    @Test
    fun `BHV-HABIT-16 in Polish a name starting with Ł comes before Zumba`() {
        val habits = listOf(aHabit(name = "Zumba"), aHabit(name = "Łyżwy"))

        val shown = habitsOnDayScreen(habits, languageTag = "pl")

        assertEquals(listOf("Łyżwy", "Zumba"), shown.map { it.name })
    }

    @Test
    fun `BHV-HABIT-R4 an archived habit is not listed`() {
        val active = aHabit(name = "Water")
        val archived = aHabit(name = "Running", archivedOn = LocalDate.of(2026, 11, 11))

        val shown = habitsOnDayScreen(listOf(archived, active), languageTag = "en")

        assertEquals(listOf(active), shown)
    }
}
