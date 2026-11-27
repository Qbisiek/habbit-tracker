package kubisp.habittracker.gui.unit

import android.icu.text.MessageFormat
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import java.math.BigDecimal
import java.util.Locale
import kubisp.habittracker.R
import kubisp.habittracker.presentation.unit.UnitLabel

/** [value] with its unit for the current locale, e.g. "1,25 l", "0,5 szklanki", "1 glass" (UI-DAY-VALUE). */
@Composable
fun valueWithUnitText(value: BigDecimal, unit: UnitLabel): String {
    val pattern = stringResource(unitValuePattern(unit))
    val customLabel = when (unit) {
        is UnitLabel.BuiltIn -> ""
        is UnitLabel.Custom -> unit.label
    }
    return valueWithUnitText(pattern, LocalConfiguration.current.locales[0], value, customLabel)
}

/**
 * Formats [value] by [pattern], an ICU MessageFormat of a `unit_value_*` resource. The number is written for [locale]
 * and the plural form is chosen for the decimal as it is written, so "0,5" takes the fraction form in Polish; Android
 * `plurals` choose by an integer only. [customLabel] is the argument {1} of the pattern of a custom unit.
 */
fun valueWithUnitText(pattern: String, locale: Locale, value: BigDecimal, customLabel: String): String =
    MessageFormat(pattern, locale).format(arrayOf<Any>(value, customLabel))

@StringRes
private fun unitValuePattern(unit: UnitLabel): Int = when (unit) {
    is UnitLabel.BuiltIn -> builtInUnitValuePattern(unit.id)
    is UnitLabel.Custom -> R.string.unit_value_custom
}

@StringRes
private fun builtInUnitValuePattern(id: String): Int = when (id) {
    "ml" -> R.string.unit_value_ml
    "l" -> R.string.unit_value_l
    "glasses" -> R.string.unit_value_glasses
    "g" -> R.string.unit_value_g
    "kg" -> R.string.unit_value_kg
    "km" -> R.string.unit_value_km
    "steps" -> R.string.unit_value_steps
    "kcal" -> R.string.unit_value_kcal
    "pages" -> R.string.unit_value_pages
    "repetitions" -> R.string.unit_value_repetitions
    "count" -> R.string.unit_value_count
    else -> error("Unknown built-in unit: $id")
}
