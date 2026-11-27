package kubisp.habittracker.domain.habit

import java.math.BigDecimal
import kubisp.habittracker.domain.decimal.ExactDecimal

/**
 * Built-in units of numeric habits (REQ-M7, Decision 37). [id] is business data (stored and exported); the
 * names are in the string resources of the app.
 */
enum class BuiltInUnit(val id: String, defaultStep: String) {
    MILLILITRES("ml", "250"),
    LITRES("l", "0.25"),
    GLASSES("glasses", "1"),
    GRAMS("g", "100"),
    KILOGRAMS("kg", "0.5"),
    KILOMETRES("km", "1"),
    STEPS("steps", "1000"),
    KILOCALORIES("kcal", "100"),
    PAGES("pages", "1"),
    REPETITIONS("repetitions", "1"),
    COUNT("count", "1");

    val defaultStep: ExactDecimal = ExactDecimal.of(BigDecimal(defaultStep))
}
