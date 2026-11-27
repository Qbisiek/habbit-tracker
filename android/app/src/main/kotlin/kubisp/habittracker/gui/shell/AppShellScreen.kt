package kubisp.habittracker.gui.shell

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kubisp.habittracker.R
import kubisp.habittracker.gui.PresenterFactories
import kubisp.habittracker.gui.navigation.AppNavHost
import kubisp.habittracker.gui.navigation.AppNavigationBar
import kubisp.habittracker.gui.navigation.AppTab
import kubisp.habittracker.gui.navigation.openTab
import kubisp.habittracker.presentation.shell.AppShellAction
import kubisp.habittracker.presentation.shell.AppShellState
import kubisp.habittracker.presentation.shell.ShellSnackbar

@Composable
fun AppShellScreen(holder: AppShellViewModel, factories: PresenterFactories) {
    val state by holder.presenter.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    AppShellContent(
        state = state,
        onAction = holder.presenter::onAction,
        selectedTab = AppTab.ofDestination(backStackEntry?.destination),
        onTabSelected = navController::openTab
    ) { contentPadding -> AppNavHost(navController, factories, contentPadding) }
}

/** The frame of every screen: the navigation bar (UI-NAV-BAR) and the snackbar host of the App shell above it. */
@Composable
fun AppShellContent(
    state: AppShellState,
    onAction: (AppShellAction) -> Unit,
    selectedTab: AppTab?,
    onTabSelected: (AppTab) -> Unit,
    screen: @Composable (PaddingValues) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    ShellSnackbarEffect(state.snackbar, snackbarHostState, onAction)
    Scaffold(
        bottomBar = { AppNavigationBar(selectedTab, onTabSelected) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { contentPadding -> screen(contentPadding) }
}

// The snackbar has no timeout of its own: it is shown while the state holds it and hidden when the state drops it,
// because cancelling `showSnackbar` removes it (`docs/DESIGN.md` section 5.7). A tap on its action or its dismiss
// button is passed to the App shell. A new snackbar state (also a new offer with the same text) shows it anew.
@Composable
private fun ShellSnackbarEffect(
    snackbar: ShellSnackbar?,
    hostState: SnackbarHostState,
    onAction: (AppShellAction) -> Unit
) {
    val visuals = snackbar?.let { snackbarVisuals(it) }
    val currentOnAction by rememberUpdatedState(onAction)
    LaunchedEffect(snackbar) {
        if (visuals == null) return@LaunchedEffect
        when (hostState.showSnackbar(visuals)) {
            SnackbarResult.ActionPerformed -> currentOnAction(AppShellAction.Undo)
            SnackbarResult.Dismissed -> currentOnAction(AppShellAction.DismissSnackbar)
        }
    }
}

@Composable
private fun snackbarVisuals(snackbar: ShellSnackbar): ShellSnackbarVisuals = when (snackbar) {
    is ShellSnackbar.UndoDeletedEntry -> ShellSnackbarVisuals(
        message = stringResource(R.string.ui_msg_undo_entry),
        actionLabel = stringResource(R.string.ui_msg_undo_action)
    )
}

private data class ShellSnackbarVisuals(override val message: String, override val actionLabel: String) :
    SnackbarVisuals {
    override val withDismissAction: Boolean get() = true
    override val duration: SnackbarDuration get() = SnackbarDuration.Indefinite
}

@Preview
@Composable
private fun AppShellContentPreview() {
    MaterialTheme {
        AppShellContent(
            state = AppShellState(snackbar = null),
            onAction = {},
            selectedTab = AppTab.DAY,
            onTabSelected = {}
        ) {}
    }
}
