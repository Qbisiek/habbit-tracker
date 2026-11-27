package kubisp.habittracker.gui.unit

import android.content.res.Configuration
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.math.BigDecimal
import java.util.Locale
import kubisp.habittracker.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** The `unit_value_*` patterns of both languages through ICU, which the JVM unit tests do not have. */
@RunWith(AndroidJUnit4::class)
class UnitValueTextTest {
    @Test
    fun `Polish glasses take the form of their number also for fractions`() {
        val texts = listOf("1", "2", "5", "22", "0.5", "1.25").map { text(POLISH, R.string.unit_value_glasses, it) }

        val expected = listOf("1 szklanka", "2 szklanki", "5 szklanek", "22 szklanki", "0,5 szklanki", "1,25 szklanki")
        assertEquals(expected, texts)
    }

    @Test
    fun `Polish values of the other counted units`() {
        val texts = listOf(
            text(POLISH, R.string.unit_value_steps, "5"),
            text(POLISH, R.string.unit_value_pages, "3"),
            text(POLISH, R.string.unit_value_repetitions, "12"),
            text(POLISH, R.string.unit_value_count, "1"),
            text(POLISH, R.string.unit_value_count, "0.5")
        )

        assertEquals(listOf("5 kroków", "3 strony", "12 powtórzeń", "1 raz", "0,5 raza"), texts)
    }

    @Test
    fun `English takes the singular only for exactly 1`() {
        val texts = listOf("1", "1.5", "0").map { text(ENGLISH, R.string.unit_value_glasses, it) }

        assertEquals(listOf("1 glass", "1.5 glasses", "0 glasses"), texts)
    }

    @Test
    fun `a value of three decimal places is written without trailing zeros`() {
        val texts = listOf(
            text(POLISH, R.string.unit_value_l, "1.250"),
            text(ENGLISH, R.string.unit_value_ml, "250.000"),
            text(ENGLISH, R.string.unit_value_kg, "0.001")
        )

        assertEquals(listOf("1,25 l", "250 ml", "0.001 kg"), texts)
    }

    @Test
    fun `a custom unit is written as its label after the number`() {
        val text = text(ENGLISH, R.string.unit_value_custom, "2.5", customLabel = "cups")

        assertEquals("2.5 cups", text)
    }

    private fun text(locale: Locale, pattern: Int, value: String, customLabel: String = ""): String {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val configuration = Configuration(context.resources.configuration).apply { setLocale(locale) }
        val localized = context.createConfigurationContext(configuration)
        return valueWithUnitText(localized.getString(pattern), locale, BigDecimal(value), customLabel)
    }

    private companion object {
        val POLISH: Locale = Locale.forLanguageTag("pl-PL")
        val ENGLISH: Locale = Locale.forLanguageTag("en-US")
    }
}
