package kubisp.habittracker.presentation.shell

/** [snackbar]: the snackbar the App shell shows above the current screen, or null when none is shown. */
data class AppShellState(val snackbar: ShellSnackbar?)

/** The snackbars of the App shell; the GUI resolves their texts (`docs/UX.md` section 7). */
sealed interface ShellSnackbar {
    /**
     * UI-MSG-UNDO-ENTRY: "Entry deleted" with the action "Undo", shown exactly while the undo offer exists.
     * [offerNumber] differs for each new offer, so a new deletion replaces the snackbar visibly (`docs/UX.md` section 4).
     */
    data class UndoDeletedEntry(val offerNumber: Long) : ShellSnackbar
}
