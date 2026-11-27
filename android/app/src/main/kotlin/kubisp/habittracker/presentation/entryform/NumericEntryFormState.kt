package kubisp.habittracker.presentation.entryform

import kubisp.habittracker.presentation.unit.UnitLabel

/** UI-ENTRYFORM-NUMERIC of a new entry: [value] as typed. */
data class NumericEntryFormState(
    val habitName: String,
    val unit: UnitLabel,
    val value: String,
    val isSaveEnabled: Boolean
)
