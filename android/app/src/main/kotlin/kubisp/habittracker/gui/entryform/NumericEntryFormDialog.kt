package kubisp.habittracker.gui.entryform

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import kubisp.habittracker.R
import kubisp.habittracker.gui.unit.unitText
import kubisp.habittracker.presentation.entryform.NumericEntryFormAction
import kubisp.habittracker.presentation.entryform.NumericEntryFormState
import kubisp.habittracker.presentation.unit.UnitLabel

/** UI-ENTRYFORM-NUMERIC of a new entry: the value field opens with the keyboard, so a value is one line of typing. */
@Composable
fun NumericEntryFormDialog(state: NumericEntryFormState, onAction: (NumericEntryFormAction) -> Unit) {
    val save = { onAction(NumericEntryFormAction.Save) }
    AlertDialog(
        onDismissRequest = { onAction(NumericEntryFormAction.Close) },
        title = { Text(state.habitName) },
        text = { ValueField(state, onAction, onDone = save) },
        confirmButton = {
            TextButton(onClick = save, enabled = state.isSaveEnabled) {
                Text(stringResource(R.string.ui_entryform_save))
            }
        },
        dismissButton = {
            TextButton(onClick = { onAction(NumericEntryFormAction.Close) }) {
                Text(stringResource(R.string.ui_entryform_cancel))
            }
        }
    )
}

@Composable
private fun ValueField(state: NumericEntryFormState, onAction: (NumericEntryFormAction) -> Unit, onDone: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    OutlinedTextField(
        value = state.value,
        onValueChange = { onAction(NumericEntryFormAction.ValueChanged(it)) },
        label = { Text(stringResource(R.string.ui_entryform_numeric_value)) },
        suffix = { Text(unitText(state.unit)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
    )
}

@Preview
@Composable
private fun NumericEntryFormDialogPreview() {
    MaterialTheme {
        NumericEntryFormDialog(
            state = NumericEntryFormState("Water", UnitLabel.BuiltIn("l"), "0,5", isSaveEnabled = true),
            onAction = {}
        )
    }
}
