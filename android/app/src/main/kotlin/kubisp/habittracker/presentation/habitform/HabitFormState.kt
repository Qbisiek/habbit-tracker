package kubisp.habittracker.presentation.habitform

import java.math.BigDecimal

/**
 * The form of a new numeric build habit (UI-HABITFORM-SCREEN, `docs/UX.md` section 5). [messages] are shown at their
 * fields; [isSaved] tells the GUI to close the form.
 */
data class HabitFormState(
    val name: String,
    val unit: UnitChoice,
    val unitChoices: List<UnitChoice>,
    val customUnitLabel: String,
    val plusStep: PlusStepField,
    val messages: Set<HabitFormMessage>,
    val isSaveEnabled: Boolean,
    val isSaved: Boolean
)

sealed interface UnitChoice {
    /** [id] is the identifier of the built-in unit; its name is in the string resources. */
    data class BuiltIn(val id: String) : UnitChoice

    /** A custom unit; its label is [HabitFormState.customUnitLabel]. */
    data object Custom : UnitChoice
}

sealed interface PlusStepField {
    /** The user has not set a step, so the field shows the default step of the unit (BHV-HABIT-R1). */
    data class FollowsUnit(val defaultStep: BigDecimal) : PlusStepField

    /** The text the user typed; it stays when the unit changes (BHV-HABIT-R1). */
    data class Typed(val text: String) : PlusStepField
}

/** The messages at the fields (`docs/UX.md` section 5, validations of the forms). */
enum class HabitFormMessage {
    /** "Enter a name" (BHV-HABIT-1). */
    NAME_EMPTY,

    /** "Enter a unit" (BHV-HABIT-11). */
    CUSTOM_UNIT_EMPTY,

    /** "Step must be above 0" (BHV-HABIT-12). */
    PLUS_STEP_NOT_ABOVE_ZERO
}
