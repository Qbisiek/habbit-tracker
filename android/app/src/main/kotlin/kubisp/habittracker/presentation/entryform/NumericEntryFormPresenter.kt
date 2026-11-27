package kubisp.habittracker.presentation.entryform

import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kubisp.habittracker.domain.decimal.isAllowedDecimalInput
import kubisp.habittracker.domain.entry.EntryOperations
import kubisp.habittracker.domain.entry.NumericEntryDraft
import kubisp.habittracker.domain.entry.NumericEntryResult
import kubisp.habittracker.domain.entry.defaultMomentOfNewEntry
import kubisp.habittracker.domain.entry.numericEntryProblems
import kubisp.habittracker.domain.habit.HabitId
import kubisp.habittracker.domain.port.Clock
import kubisp.habittracker.presentation.unit.UnitLabel

/**
 * UI-ENTRYFORM-NUMERIC for a new entry, opened from the Day row (`docs/UX.md` sections 3, 4). It has the value only;
 * the entry gets the default moment of the selected date, taken when the form opens (`docs/DESIGN.md` section 5.8),
 * so a form saved after midnight keeps the date it was opened on with its time. The rules come from the
 * Domain validation of the draft. [scope] is the scope of its holder.
 */
class NumericEntryFormPresenter(
    private val entryOperations: EntryOperations,
    private val clock: Clock,
    private val scope: CoroutineScope
) {
    private var input: FormInput? = null
    private val mutableState = MutableStateFlow<NumericEntryFormState?>(null)

    /** Null while the form is closed. */
    val state: StateFlow<NumericEntryFormState?> = mutableState.asStateFlow()

    /** Opens an empty form of a new entry of the habit on [date]. */
    fun openNew(habitId: HabitId, habitName: String, unit: UnitLabel, date: LocalDate) {
        val moment = defaultMomentOfNewEntry(date, clock.now(), clock.timeZone())
        update(FormInput(habitId, habitName, unit, moment))
    }

    fun onAction(action: NumericEntryFormAction) {
        when (action) {
            is NumericEntryFormAction.ValueChanged -> changeValue(action.text)
            NumericEntryFormAction.Save -> save()
            NumericEntryFormAction.Close -> update(null)
        }
    }

    private fun changeValue(text: String) {
        val current = input ?: return
        if (!isAllowedDecimalInput(text)) return
        update(current.copy(value = text))
    }

    private fun save() {
        val current = input ?: return
        if (mutableState.value?.isSaveEnabled != true) return
        update(current.copy(isSaving = true))
        val draft = draftOf(current)
        scope.launch {
            // The user may leave the screen while the entry is written; the write is finished all the same.
            val result = withContext(NonCancellable) { entryOperations.addNumericEntry(current.habitId, draft) }
            afterSave(result)
        }
    }

    // Save is enabled only for a valid draft, so a rejection (the moment passed midnight) keeps the form open.
    private fun afterSave(result: NumericEntryResult) {
        when (result) {
            is NumericEntryResult.Saved -> update(null)
            is NumericEntryResult.Rejected -> input?.let { update(it.copy(isSaving = false)) }
        }
    }

    private fun update(newInput: FormInput?) {
        input = newInput
        mutableState.value = newInput?.let { stateOf(it) }
    }

    private fun stateOf(input: FormInput): NumericEntryFormState {
        val problems = numericEntryProblems(draftOf(input), stored = null, clock.now(), clock.timeZone())
        return NumericEntryFormState(
            habitName = input.habitName,
            unit = input.unit,
            value = input.value,
            isSaveEnabled = problems.isEmpty() && !input.isSaving
        )
    }

    private fun draftOf(input: FormInput): NumericEntryDraft = NumericEntryDraft(
        value = input.value,
        date = input.moment.toLocalDate(),
        time = input.moment.toLocalTime()
    )
}

private data class FormInput(
    val habitId: HabitId,
    val habitName: String,
    val unit: UnitLabel,
    val moment: LocalDateTime,
    val value: String = "",
    val isSaving: Boolean = false
)
