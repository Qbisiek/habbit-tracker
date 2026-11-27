package kubisp.habittracker.domain.entry

import kubisp.habittracker.domain.fake.aHabit
import kubisp.habittracker.domain.fake.aNumericEntry
import kubisp.habittracker.domain.fake.decimal
import org.junit.Assert.assertEquals
import org.junit.Test

class NumericDayValueTest {
    private val water = aHabit()

    @Test
    fun `BHV-ENTRY-R4 the value of a day is the total of the entries of that day only`() {
        val entries = listOf(
            aNumericEntry(water.id, value = decimal("0.25"), dateTime = nov(10, 8, 0)),
            aNumericEntry(water.id, value = decimal("0.3"), dateTime = nov(10, 23, 59, 59)),
            aNumericEntry(water.id, value = decimal("1"), dateTime = nov(11, 0, 0))
        )

        val value = numericValueOfDay(entries, nov(10))

        assertEquals(decimal("0.55"), value)
    }

    @Test
    fun `BHV-ENTRY-R4 the value of a day without entries is 0`() {
        val entries = listOf(aNumericEntry(water.id, dateTime = nov(10, 8, 0)))

        val value = numericValueOfDay(entries, nov(9))

        assertEquals(decimal("0"), value)
    }

    @Test
    fun `BHV-ENTRY-R6b entries with equal times are listed in the order they were added`() {
        val first = aNumericEntry(water.id, value = decimal("1"), dateTime = nov(10, 9, 0))
        val second = aNumericEntry(water.id, value = decimal("2"), dateTime = nov(10, 9, 0))
        val earlier = aNumericEntry(water.id, value = decimal("3"), dateTime = nov(10, 8, 59, 59))

        val listed = numericEntriesByTime(listOf(first, second, earlier))

        assertEquals(listOf(earlier, first, second), listed)
    }
}
