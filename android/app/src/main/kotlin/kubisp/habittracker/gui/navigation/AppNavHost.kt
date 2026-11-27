package kubisp.habittracker.gui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

/** The destinations of the tabs; the Day screen comes in T2.11 and the Habits screen in T2.10. */
@Composable
fun AppNavHost(navController: NavHostController, contentPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = AppTab.DAY.route,
        modifier = Modifier.fillMaxSize().padding(contentPadding)
    ) {
        composable(AppTab.DAY.route) {}
        composable(AppTab.HABITS.route) {}
    }
}

// Switching tabs keeps one back stack: back from any tab returns to the start tab (Day), and a tab keeps its state.
fun NavHostController.openTab(tab: AppTab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
