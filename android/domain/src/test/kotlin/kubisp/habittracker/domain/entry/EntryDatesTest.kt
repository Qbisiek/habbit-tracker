package kubisp.habittracker.domain.entry

import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EntryDatesTest {
    private val today = LocalDate.of(2026, 11, 11)

    @Test
    fun `BHV-ENTRY-R3 entries can be added on today's date`() {
        val canAdd = canAddEntriesOn(today, today)

        assertTrue(canAdd)
    }

    @Test
    fun `BHV-ENTRY-R3 entries can be added on a past date`() {
        val canAdd = canAddEntriesOn(LocalDate.of(2026, 11, 5), today)

        assertTrue(canAdd)
    }

    @Test
    fun `BHV-ENTRY-2 no entry can be added on a date after today`() {
        val canAdd = canAddEntriesOn(LocalDate.of(2026, 11, 12), today)

        assertFalse(canAdd)
    }
}
