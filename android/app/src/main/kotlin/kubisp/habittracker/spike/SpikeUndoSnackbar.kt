package kubisp.habittracker.spike

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kubisp.habittracker.R

/**
 * T1.7 (docs/UX.md section 8 item 4): is the Undo snackbar of the screen shown above an open ModalBottomSheet?
 * The offer stands in for the Domain undo stream (docs/DESIGN.md section 5.7): the snackbar is shown exactly while it
 * exists. Variant A has only the screen host (the App shell); variant B adds a host inside the sheet.
 */
@Composable
fun UndoSpikeScaffold(content: @Composable () -> Unit) {
    var state by remember { mutableStateOf(UndoState()) }
    val screenHost = remember { SnackbarHostState() }
    val sheetHost = remember { SnackbarHostState() }
    val activeHost = if (state.variant == UndoVariant.SheetHost && state.isSheetOpen) sheetHost else screenHost
    UndoSnackbarEffect(
        offer = state.offer,
        host = activeHost,
        onUndo = { state = state.undo() },
        onDismiss = { state = state.copy(offer = null) }
    )
    Scaffold(snackbarHost = { SnackbarHost(screenHost) }, contentWindowInsets = WindowInsets(0)) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            UndoSection(state = state, onChange = { state = it })
            content()
        }
    }
    if (state.isSheetOpen) {
        EntriesSheet(state = state, sheetHost = sheetHost, onChange = { state = it })
    }
}

private enum class UndoVariant { ScreenHost, SheetHost }

private data class DeletedEntry(val entry: String, val index: Int)

private data class UndoState(
    val entries: List<String> = SAMPLE_ENTRIES,
    val offer: DeletedEntry? = null,
    val isSheetOpen: Boolean = false,
    val variant: UndoVariant = UndoVariant.ScreenHost
) {
    // A new deletion replaces the previous offer (docs/UX.md section 4).
    fun delete(entry: String): UndoState =
        copy(entries = entries - entry, offer = DeletedEntry(entry, entries.indexOf(entry)))

    fun undo(): UndoState {
        val deleted = offer ?: return this
        val restored = entries.toMutableList().apply { add(deleted.index.coerceAtMost(size), deleted.entry) }
        return copy(entries = restored, offer = null)
    }
}

// Cancelling the effect (no offer, or another host) hides the snackbar, so it never outlives the offer.
@Composable
private fun UndoSnackbarEffect(
    offer: DeletedEntry?,
    host: SnackbarHostState,
    onUndo: () -> Unit,
    onDismiss: () -> Unit
) {
    val message = stringResource(R.string.spike_undo_deleted)
    val action = stringResource(R.string.spike_undo_action)
    LaunchedEffect(offer, host) {
        if (offer == null) return@LaunchedEffect
        val result =
            host.showSnackbar(
                message = message,
                actionLabel = action,
                withDismissAction = true,
                duration = SnackbarDuration.Indefinite
            )
        when (result) {
            SnackbarResult.ActionPerformed -> onUndo()
            SnackbarResult.Dismissed -> onDismiss()
        }
    }
}

@Composable
private fun UndoSection(state: UndoState, onChange: (UndoState) -> Unit) {
    Column(
        modifier = Modifier.safeDrawingPadding().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = stringResource(R.string.spike_undo_title), style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VariantChip(state, UndoVariant.ScreenHost, R.string.spike_undo_variant_screen, onChange)
            VariantChip(state, UndoVariant.SheetHost, R.string.spike_undo_variant_sheet, onChange)
        }
        Button(onClick = { onChange(state.copy(isSheetOpen = true)) }) {
            Text(stringResource(R.string.spike_undo_open_sheet))
        }
        Text(stringResource(R.string.spike_undo_offer, state.offer?.entry ?: "-"))
    }
}

@Composable
private fun VariantChip(state: UndoState, variant: UndoVariant, label: Int, onChange: (UndoState) -> Unit) {
    FilterChip(
        selected = state.variant == variant,
        onClick = { onChange(state.copy(variant = variant)) },
        label = { Text(stringResource(label)) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntriesSheet(state: UndoState, sheetHost: SnackbarHostState, onChange: (UndoState) -> Unit) {
    ModalBottomSheet(onDismissRequest = { onChange(state.copy(isSheetOpen = false)) }) {
        Column(modifier = Modifier.padding(PaddingValues(horizontal = 16.dp, vertical = 8.dp))) {
            Text(text = stringResource(R.string.spike_undo_sheet_title), style = MaterialTheme.typography.titleMedium)
            state.entries.forEach { entry -> EntryRow(entry) { onChange(state.delete(entry)) } }
            if (state.variant == UndoVariant.SheetHost) SnackbarHost(sheetHost)
        }
    }
}

@Composable
private fun EntryRow(entry: String, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = entry, modifier = Modifier.weight(1f))
        IconButton(onClick = onDelete) {
            Icon(
                painter = painterResource(android.R.drawable.ic_menu_delete),
                contentDescription = stringResource(R.string.spike_undo_delete)
            )
        }
    }
}

private val SAMPLE_ENTRIES = listOf("07:10 - 07:40", "12:00 - 12:25", "18:30 - 19:45", "21:00 - 21:20")
