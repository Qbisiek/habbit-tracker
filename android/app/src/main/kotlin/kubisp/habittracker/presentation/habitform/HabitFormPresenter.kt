package kubisp.habittracker.presentation.habitform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kubisp.habittracker.domain.decimal.isAllowedDecimalInput
import kubisp.habittracker.domain.habit.BuiltInUnit
import kubisp.habittracker.domain.habit.CreateHabitResult
import kubisp.habittracker.domain.habit.Direction
import kubisp.habittracker.domain.habit.HabitDraft
import kubisp.habittracker.domain.habit.HabitDraftProblem
import kubisp.habittracker.domain.habit.HabitOperations
import kubisp.habittracker.domain.habit.MeasurementDraft
import kubisp.habittracker.domain.habit.NumericUnit
import kubisp.habittracker.domain.habit.validateHabitDraft

/**
 * The form of a new numeric build habit (`docs/UX.md` section 5); M2 offers no other direction or type. The rules
 * come from the Domain validation of the draft; this object only tracks which fields are touched, so a message
 * appears only after its field was changed (`docs/UX.md` section 1, "Rejected"). [scope] is the scope of its holder.
 */
class HabitFormPresenter(private val habitOperations: HabitOperations, private val scope: CoroutineScope) {
    private var input = FormInput()
    private val mutableState = MutableStateFlow(stateOf(input))

    val state: StateFlow<HabitFormState> = mutableState.asStateFlow()

    fun onAction(action: HabitFormAction) {
        when (action) {
            is HabitFormAction.NameChanged -> changeName(action.name)
            is HabitFormAction.UnitChosen -> edit { copy(unit = action.unit) }
            is HabitFormAction.CustomUnitChanged -> changeCustomUnit(action.label)
            is HabitFormAction.PlusStepChanged -> changePlusStep(action.text)
            HabitFormAction.Save -> save()
        }
    }

    private fun changeName(name: String) {
        edit { copy(name = name).touching(FormField.NAME, name) }
    }

    private fun changeCustomUnit(label: String) {
        edit { copy(customUnitLabel = label).touching(FormField.CUSTOM_UNIT, label) }
    }

    private fun changePlusStep(text: String) {
        if (!isAllowedDecimalInput(text)) return
        edit { copy(plusStep = text, touched = touched + FormField.PLUS_STEP) }
    }

    private fun save() {
        if (!mutableState.value.isSaveEnabled) return
        edit { copy(progress = SaveProgress.SAVING) }
        val draft = draftOf(input)
        scope.launch {
            // The user may leave the form while the habit is written; the write is finished all the same.
            val result = withContext(NonCancellable) { habitOperations.createHabit(draft) }
            edit { afterSave(result) }
        }
    }

    private fun edit(change: FormInput.() -> FormInput) {
        input = input.change()
        mutableState.value = stateOf(input)
    }
}

private enum class FormField { NAME, CUSTOM_UNIT, PLUS_STEP }

private enum class SaveProgress { EDITING, SAVING, SAVED }

/** [plusStep] is null while the user has not set a step. */
private data class FormInput(
    val name: String = "",
    val unit: UnitChoice = UnitChoice.BuiltIn(BuiltInUnit.COUNT.id),
    val customUnitLabel: String = "",
    val plusStep: String? = null,
    val touched: Set<FormField> = emptySet(),
    val progress: SaveProgress = SaveProgress.EDITING
)

// A field is touched once its value has differed from the empty value it had when the form opened.
private fun FormInput.touching(field: FormField, value: String): FormInput =
    if (value.isEmpty()) this else copy(touched = touched + field)

// Save is enabled only for a valid draft, so a rejection is not expected; its messages are then shown at once.
private fun FormInput.afterSave(result: CreateHabitResult): FormInput = when (result) {
    is CreateHabitResult.Created -> copy(progress = SaveProgress.SAVED)
    is CreateHabitResult.Rejected -> copy(progress = SaveProgress.EDITING, touched = FormField.entries.toSet())
}

private val unitChoices: List<UnitChoice> = BuiltInUnit.entries.map { UnitChoice.BuiltIn(it.id) } + UnitChoice.Custom

private fun stateOf(input: FormInput): HabitFormState {
    val problems = validateHabitDraft(draftOf(input))
    return HabitFormState(
        name = input.name,
        unit = input.unit,
        unitChoices = unitChoices,
        customUnitLabel = input.customUnitLabel,
        plusStep = plusStepFieldOf(input),
        messages = problems.mapNotNull { messageOf(it, input.touched) }.toSet(),
        isSaveEnabled = problems.isEmpty() && input.progress == SaveProgress.EDITING,
        isSaved = input.progress == SaveProgress.SAVED
    )
}

private fun draftOf(input: FormInput): HabitDraft = HabitDraft(
    name = input.name,
    direction = Direction.BUILD,
    measurement = MeasurementDraft.Numeric(unit = numericUnitOf(input), plusStep = input.plusStep)
)

private fun numericUnitOf(input: FormInput): NumericUnit = when (val unit = input.unit) {
    is UnitChoice.BuiltIn -> NumericUnit.BuiltIn(builtInUnitOf(unit.id))
    UnitChoice.Custom -> NumericUnit.Custom(input.customUnitLabel)
}

private fun builtInUnitOf(id: String): BuiltInUnit =
    BuiltInUnit.entries.find { it.id == id } ?: error("Unknown built-in unit: $id")

private fun plusStepFieldOf(input: FormInput): PlusStepField = when (val typed = input.plusStep) {
    null -> PlusStepField.FollowsUnit(numericUnitOf(input).defaultStep.toBigDecimal())
    else -> PlusStepField.Typed(typed)
}

private fun messageOf(problem: HabitDraftProblem, touched: Set<FormField>): HabitFormMessage? {
    val (field, message) = when (problem) {
        HabitDraftProblem.NameEmpty -> FormField.NAME to HabitFormMessage.NAME_EMPTY
        HabitDraftProblem.CustomUnitEmpty -> FormField.CUSTOM_UNIT to HabitFormMessage.CUSTOM_UNIT_EMPTY
        HabitDraftProblem.PlusStepNotAboveZero -> FormField.PLUS_STEP to HabitFormMessage.PLUS_STEP_NOT_ABOVE_ZERO
    }
    return message.takeIf { field in touched }
}
