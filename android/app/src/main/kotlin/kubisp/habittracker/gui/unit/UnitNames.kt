package kubisp.habittracker.gui.unit

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kubisp.habittracker.R
import kubisp.habittracker.presentation.unit.UnitLabel

@Composable
fun unitText(unit: UnitLabel): String = when (unit) {
    is UnitLabel.BuiltIn -> stringResource(builtInUnitName(unit.id))
    is UnitLabel.Custom -> unit.label
}

/** The name of a built-in unit by its Domain identifier (Decision 37). */
@StringRes
fun builtInUnitName(id: String): Int = when (id) {
    "ml" -> R.string.unit_ml
    "l" -> R.string.unit_l
    "glasses" -> R.string.unit_glasses
    "g" -> R.string.unit_g
    "kg" -> R.string.unit_kg
    "km" -> R.string.unit_km
    "steps" -> R.string.unit_steps
    "kcal" -> R.string.unit_kcal
    "pages" -> R.string.unit_pages
    "repetitions" -> R.string.unit_repetitions
    "count" -> R.string.unit_count
    else -> error("Unknown built-in unit: $id")
}
