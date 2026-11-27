package kubisp.habittracker.gui.habits

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.UUID
import kubisp.habittracker.R
import kubisp.habittracker.gui.unit.unitText
import kubisp.habittracker.presentation.habits.HabitListRow
import kubisp.habittracker.presentation.habits.HabitsState
import kubisp.habittracker.presentation.unit.UnitLabel

@Composable
fun HabitsScreen(holder: HabitsViewModel, onAddHabit: () -> Unit) {
    val state by holder.presenter.state.collectAsStateWithLifecycle()
    HabitsContent(state = state, onAddHabit = onAddHabit)
}

/** The Habits screen (`docs/UX.md` section 5): UI-HABITS-LIST, UI-HABITS-ADD and UI-HABITS-EMPTY. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsContent(state: HabitsState, onAddHabit: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.ui_habits_title)) }) },
        floatingActionButton = { AddHabitButton(onAddHabit) }
    ) { contentPadding ->
        when (state) {
            HabitsState.Loading -> Unit
            is HabitsState.Shown -> HabitList(state.habits, contentPadding)
        }
    }
}

@Composable
private fun AddHabitButton(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(painter = painterResource(R.drawable.ic_add), contentDescription = stringResource(R.string.ui_habits_add))
    }
}

@Composable
private fun HabitList(habits: List<HabitListRow>, contentPadding: PaddingValues) {
    if (habits.isEmpty()) {
        NoHabits(contentPadding)
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = contentPadding) {
        items(habits, key = { it.id }) { habit ->
            ListItem(supportingContent = { Text(unitText(habit.unit)) }) { Text(habit.name) }
        }
    }
}

@Composable
private fun NoHabits(contentPadding: PaddingValues) {
    Box(modifier = Modifier.fillMaxSize().padding(contentPadding), contentAlignment = Alignment.Center) {
        Text(text = stringResource(R.string.ui_habits_empty), style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview
@Composable
private fun HabitsContentPreview() {
    val habits = listOf(
        HabitListRow(UUID.randomUUID(), "Reading", UnitLabel.BuiltIn("pages")),
        HabitListRow(UUID.randomUUID(), "Water", UnitLabel.BuiltIn("l"))
    )
    MaterialTheme {
        HabitsContent(state = HabitsState.Shown(habits), onAddHabit = {})
    }
}

@Preview
@Composable
private fun HabitsContentEmptyPreview() {
    MaterialTheme {
        HabitsContent(state = HabitsState.Shown(emptyList()), onAddHabit = {})
    }
}
