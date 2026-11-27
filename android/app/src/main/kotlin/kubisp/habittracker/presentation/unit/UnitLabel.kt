package kubisp.habittracker.presentation.unit

import kubisp.habittracker.domain.habit.NumericUnit

/** The unit of a numeric habit as the GUI shows it (`docs/DESIGN.md` section 7, Decision 37). */
sealed interface UnitLabel {
    /** [id] is the identifier of the built-in unit; its name is in the string resources. */
    data class BuiltIn(val id: String) : UnitLabel

    data class Custom(val label: String) : UnitLabel
}

fun NumericUnit.toUnitLabel(): UnitLabel = when (this) {
    is NumericUnit.BuiltIn -> UnitLabel.BuiltIn(unit.id)
    is NumericUnit.Custom -> UnitLabel.Custom(label)
}
