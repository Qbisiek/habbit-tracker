package kubisp.habittracker.presentation.entryform

sealed interface NumericEntryFormAction {
    /** The text of the value field after an edit; an edit that the input filter does not allow is not taken. */
    data class ValueChanged(val text: String) : NumericEntryFormAction

    data object Save : NumericEntryFormAction

    /** The form is closed without saving. */
    data object Close : NumericEntryFormAction
}
