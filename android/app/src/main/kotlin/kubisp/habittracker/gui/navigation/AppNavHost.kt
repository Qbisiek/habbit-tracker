package kubisp.habittracker.gui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import kubisp.habittracker.gui.PresenterFactories
import kubisp.habittracker.gui.day.DayScreen
import kubisp.habittracker.gui.day.DayViewModel
import kubisp.habittracker.gui.habitform.HabitFormScreen
import kubisp.habittracker.gui.habitform.HabitFormViewModel
import kubisp.habittracker.gui.habits.HabitsScreen
import kubisp.habittracker.gui.habits.HabitsViewModel

private const val HABIT_LIST_ROUTE = "habits/list"
private const val NEW_HABIT_ROUTE = "habits/new"

/**
 * The destinations of the tabs. The Habits tab is a nested graph, so the tab stays selected and keeps its back stack
 * while the form is open.
 */
@Composable
fun AppNavHost(navController: NavHostController, factories: PresenterFactories, contentPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = AppTab.DAY.route,
        // Consumed, so the top bars and the keyboard padding of the screens do not add the system bars again.
        modifier = Modifier.fillMaxSize().padding(contentPadding).consumeWindowInsets(contentPadding)
    ) {
        composable(AppTab.DAY.route) {
            DayDestination(
                factories,
                onCreateHabit = {
                    navController.openTab(AppTab.HABITS)
                    navController.navigate(NEW_HABIT_ROUTE) { launchSingleTop = true }
                },
                onGoToHabits = { navController.openTab(AppTab.HABITS) }
            )
        }
        navigation(startDestination = HABIT_LIST_ROUTE, route = AppTab.HABITS.route) {
            composable(HABIT_LIST_ROUTE) {
                HabitListDestination(factories, onAddHabit = { navController.navigate(NEW_HABIT_ROUTE) })
            }
            composable(NEW_HABIT_ROUTE) {
                NewHabitDestination(factories, onClose = { navController.popBackStack(HABIT_LIST_ROUTE, false) })
            }
        }
    }
}

@Composable
private fun DayDestination(factories: PresenterFactories, onCreateHabit: () -> Unit, onGoToHabits: () -> Unit) {
    val languageTag = LocalConfiguration.current.locales[0].toLanguageTag()
    val holder = viewModel { DayViewModel(factories, languageTag) }
    DayScreen(holder, onCreateHabit, onGoToHabits)
}

@Composable
private fun HabitListDestination(factories: PresenterFactories, onAddHabit: () -> Unit) {
    val languageTag = LocalConfiguration.current.locales[0].toLanguageTag()
    val holder = viewModel { HabitsViewModel(factories, languageTag) }
    HabitsScreen(holder, onAddHabit)
}

@Composable
private fun NewHabitDestination(factories: PresenterFactories, onClose: () -> Unit) {
    val holder = viewModel { HabitFormViewModel(factories) }
    HabitFormScreen(holder, onClose)
}

// Switching tabs keeps one back stack: back from any tab returns to the start tab (Day), and a tab keeps its state.
fun NavHostController.openTab(tab: AppTab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
