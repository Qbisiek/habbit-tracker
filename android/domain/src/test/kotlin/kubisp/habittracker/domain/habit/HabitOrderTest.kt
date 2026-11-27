package kubisp.habittracker.domain.habit

import java.time.LocalDate
import java.time.LocalDateTime
import kubisp.habittracker.domain.fake.aHabit
import org.junit.Assert.assertEquals
import org.junit.Test

class HabitOrderTest {
    @Test
    fun `BHV-HABIT-R4 in Polish Ł comes after L and before M`() {
        val habits = listOf(aHabit(name = "Ma"), aHabit(name = "Łyżwy"), aHabit(name = "Lody"))

        val ordered = habitsInDisplayOrder(habits, languageTag = "pl")

        assertEquals(listOf("Lody", "Łyżwy", "Ma"), ordered.map { it.name })
    }

    @Test
    fun `BHV-HABIT-R4 the letter order follows the app language`() {
        val habits = listOf(aHabit(name = "Ćwiczenia"), aHabit(name = "Czytanie"))

        val inPolish = habitsInDisplayOrder(habits, languageTag = "pl")
        val inEnglish = habitsInDisplayOrder(habits, languageTag = "en")

        assertEquals(listOf("Czytanie", "Ćwiczenia"), inPolish.map { it.name })
        assertEquals(listOf("Ćwiczenia", "Czytanie"), inEnglish.map { it.name })
    }

    @Test
    fun `BHV-HABIT-R4 names that differ only in case are equal names, ordered by creation`() {
        val newer = aHabit(name = "reading", createdAt = LocalDateTime.of(2026, 11, 10, 8, 0))
        val older = aHabit(name = "Reading", createdAt = LocalDateTime.of(2026, 11, 9, 8, 0))

        val ordered = habitsInDisplayOrder(listOf(newer, older), languageTag = "en")

        assertEquals(listOf(older, newer), ordered)
    }

    @Test
    fun `BHV-HABIT-R4 equal names created on the same day are ordered by creation time`() {
        val evening = aHabit(name = "Reading", createdAt = LocalDateTime.of(2026, 11, 9, 20, 0))
        val morning = aHabit(name = "Reading", createdAt = LocalDateTime.of(2026, 11, 9, 7, 30))

        val ordered = habitsInDisplayOrder(listOf(evening, morning), languageTag = "en")

        assertEquals(listOf(morning, evening), ordered)
    }

    @Test
    fun `BHV-HABIT-R4 equal names and creation times keep the order of addition, existing before imported`() {
        val createdAt = LocalDateTime.of(2026, 11, 9, 8, 0)
        val existing = aHabit(name = "Reading", createdAt = createdAt)
        val imported = aHabit(name = "Reading", createdAt = createdAt)

        val ordered = habitsInDisplayOrder(listOf(existing, imported), languageTag = "en")

        assertEquals(listOf(existing, imported), ordered)
    }

    @Test
    fun `BHV-HABIT-R4 archived habits are ordered by the same rule`() {
        val archivedOn = LocalDate.of(2026, 11, 11)
        val habits =
            listOf(aHabit(name = "banana", archivedOn = archivedOn), aHabit(name = "Apple", archivedOn = archivedOn))

        val ordered = habitsInDisplayOrder(habits, languageTag = "en")

        assertEquals(listOf("Apple", "banana"), ordered.map { it.name })
    }
}
