package kubisp.habittracker.presentation.habitform

sealed interface HabitFormAction {
    data class NameChanged(val name: String) : HabitFormAction

    data class UnitChosen(val unit: UnitChoice) : HabitFormAction

    data class CustomUnitChanged(val label: String) : HabitFormAction

    /** The text of the "+" step field after an edit; an edit that the input filter does not allow is not taken. */
    data class PlusStepChanged(val text: String) : HabitFormAction

    data object Save : HabitFormAction
}
