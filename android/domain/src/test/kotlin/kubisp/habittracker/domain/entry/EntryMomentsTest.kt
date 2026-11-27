package kubisp.habittracker.domain.entry

import org.junit.Assert.assertEquals
import org.junit.Test

class EntryMomentsTest {
    private val now = nov(11, 11, 30, 27).atZone(WARSAW).toInstant()

    @Test
    fun `BHV-ENTRY-R5 the default moment on today's date is now cut to the whole minute`() {
        val moment = defaultMomentOfNewEntry(TODAY, now, WARSAW)

        assertEquals(nov(11, 11, 30), moment)
    }

    @Test
    fun `BHV-ENTRY-R5 the default moment on a past date is 12 00 of that date`() {
        val moment = defaultMomentOfNewEntry(nov(5), now, WARSAW)

        assertEquals(nov(5, 12, 0), moment)
    }

    @Test
    fun `today is the date of now in the device time zone`() {
        val afterMidnightInWarsaw = nov(12, 0, 30, 10).atZone(WARSAW).toInstant()

        val moment = defaultMomentOfNewEntry(nov(12), afterMidnightInWarsaw, WARSAW)

        assertEquals(nov(12, 0, 30), moment)
    }
}
