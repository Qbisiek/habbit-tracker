package kubisp.habittracker.presentation.shell

sealed interface AppShellAction {
    /** The user tapped the action of an undo snackbar. */
    data object Undo : AppShellAction

    /** The user dismissed the snackbar without its action. */
    data object DismissSnackbar : AppShellAction
}
