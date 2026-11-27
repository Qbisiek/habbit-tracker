package kubisp.habittracker.gui.day

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID
import kubisp.habittracker.R
import kubisp.habittracker.gui.entryform.NumericEntryFormDialog
import kubisp.habittracker.gui.unit.valueWithUnitText
import kubisp.habittracker.presentation.day.DayAction
import kubisp.habittracker.presentation.day.DayHabits
import kubisp.habittracker.presentation.day.DayRow
import kubisp.habittracker.presentation.day.DayState
import kubisp.habittracker.presentation.unit.UnitLabel

@Composable
fun DayScreen(holder: DayViewModel, onCreateHabit: () -> Unit, onGoToHabits: () -> Unit) {
    val state by holder.presenter.state.collectAsStateWithLifecycle()
    val numericEntryForm by holder.presenter.numericEntryForm.state.collectAsStateWithLifecycle()
    DayContent(
        state = state,
        onAction = holder.presenter::onAction,
        onCreateHabit = onCreateHabit,
        onGoToHabits = onGoToHabits
    )
    numericEntryForm?.let { NumericEntryFormDialog(it, holder.presenter.numericEntryForm::onAction) }
}

/** The Day screen (`docs/UX.md` section 3): UI-DAY-HEADER above UI-DAY-ROW of each habit, or an empty state. */
@Composable
fun DayContent(state: DayState, onAction: (DayAction) -> Unit, onCreateHabit: () -> Unit, onGoToHabits: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        when (state) {
            DayState.Loading -> Unit

            is DayState.Shown -> Column {
                DayHeader(state.date, onAction)
                HorizontalDivider()
                DayHabitsContent(state.habits, onAction, onCreateHabit, onGoToHabits)
            }
        }
    }
}

@Composable
private fun DayHabitsContent(
    habits: DayHabits,
    onAction: (DayAction) -> Unit,
    onCreateHabit: () -> Unit,
    onGoToHabits: () -> Unit
) {
    when (habits) {
        DayHabits.NoHabits -> EmptyState(R.string.ui_day_empty_none, R.string.ui_day_empty_none_button, onCreateHabit)

        DayHabits.AllArchived ->
            EmptyState(R.string.ui_day_empty_archived, R.string.ui_day_empty_archived_button, onGoToHabits)

        is DayHabits.Rows -> LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(habits.rows, key = { it.habitId }) { row ->
                HabitRow(row, onAction)
                HorizontalDivider()
            }
        }
    }
}

/**
 * UI-DAY-ROW (Decision 99): the name in the first line; the action controls and the value in the second, which wraps
 * when it does not fit. The progress indicator beside the name comes with the goals (T5.9).
 */
@Composable
private fun HabitRow(row: DayRow, onAction: (DayAction) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(text = row.name, style = MaterialTheme.typography.titleMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            itemVerticalAlignment = Alignment.CenterVertically
        ) {
            if (row.canAddEntries) NumericActions(row, onAction)
            Text(text = valueWithUnitText(row.value, row.unit), style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/** UI-DAY-ACTION of a numeric habit: "+" adds one step, the keyboard button opens UI-ENTRYFORM-NUMERIC. */
@Composable
private fun NumericActions(row: DayRow, onAction: (DayAction) -> Unit) {
    FilledTonalIconButton(onClick = { onAction(DayAction.AddPlusStep(row.habitId)) }) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = stringResource(R.string.ui_day_action_plus, row.name)
        )
    }
    IconButton(onClick = { onAction(DayAction.TypeValue(row.habitId)) }) {
        Icon(
            painter = painterResource(R.drawable.ic_keyboard),
            contentDescription = stringResource(R.string.ui_day_action_type, row.name)
        )
    }
}

@Composable
private fun EmptyState(@StringRes text: Int, @StringRes button: Int, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(text), style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onClick) { Text(stringResource(button)) }
    }
}

@Preview
@Composable
private fun DayContentPreview() {
    val rows = listOf(
        DayRow(UUID.randomUUID(), "Reading", BigDecimal("12"), UnitLabel.BuiltIn("pages"), canAddEntries = true),
        DayRow(UUID.randomUUID(), "Water", BigDecimal("1.25"), UnitLabel.BuiltIn("l"), canAddEntries = true)
    )
    MaterialTheme {
        DayContent(
            state = DayState.Shown(LocalDate.of(2026, 11, 11), DayHabits.Rows(rows)),
            onAction = {},
            onCreateHabit = {},
            onGoToHabits = {}
        )
    }
}

@Preview
@Composable
private fun DayContentEmptyPreview() {
    MaterialTheme {
        DayContent(
            state = DayState.Shown(LocalDate.of(2026, 11, 11), DayHabits.NoHabits),
            onAction = {},
            onCreateHabit = {},
            onGoToHabits = {}
        )
    }
}
