package kubisp.habittracker.gui.habitform

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.math.BigDecimal
import kubisp.habittracker.R
import kubisp.habittracker.gui.format.decimalInputText
import kubisp.habittracker.gui.unit.builtInUnitName
import kubisp.habittracker.presentation.habitform.HabitFormAction
import kubisp.habittracker.presentation.habitform.HabitFormMessage
import kubisp.habittracker.presentation.habitform.HabitFormState
import kubisp.habittracker.presentation.habitform.PlusStepField
import kubisp.habittracker.presentation.habitform.UnitChoice

@Composable
fun HabitFormScreen(holder: HabitFormViewModel, onClose: () -> Unit) {
    val state by holder.presenter.state.collectAsStateWithLifecycle()
    val currentOnClose by rememberUpdatedState(onClose)
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) currentOnClose()
    }
    HabitFormContent(state = state, onAction = holder.presenter::onAction, onBack = onClose)
}

/** UI-HABITFORM-SCREEN for a new numeric build habit: the basics of `docs/UX.md` section 5, "Save" at the bottom. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitFormContent(state: HabitFormState, onAction: (HabitFormAction) -> Unit, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ui_habitform_title_new)) },
                navigationIcon = { BackButton(onBack) }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NameField(state, onAction)
            UnitField(state, onAction)
            if (state.unit == UnitChoice.Custom) CustomUnitField(state, onAction)
            PlusStepField(state, onAction)
            Button(
                onClick = { onAction(HabitFormAction.Save) },
                enabled = state.isSaveEnabled,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.ui_habitform_save)) }
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = stringResource(R.string.ui_habitform_back)
        )
    }
}

@Composable
private fun NameField(state: HabitFormState, onAction: (HabitFormAction) -> Unit) {
    OutlinedTextField(
        value = state.name,
        onValueChange = { onAction(HabitFormAction.NameChanged(it)) },
        label = { Text(stringResource(R.string.ui_habitform_name)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        isError = HabitFormMessage.NAME_EMPTY in state.messages,
        supportingText = fieldMessage(state, HabitFormMessage.NAME_EMPTY, R.string.ui_habitform_name_empty),
        modifier = Modifier.fillMaxWidth()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitField(state: HabitFormState, onAction: (HabitFormAction) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = isExpanded, onExpandedChange = { isExpanded = it }) {
        OutlinedTextField(
            value = unitChoiceText(state.unit),
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(R.string.ui_habitform_unit)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = isExpanded, onDismissRequest = { isExpanded = false }) {
            state.unitChoices.forEach { choice ->
                DropdownMenuItem(
                    text = { Text(unitChoiceText(choice)) },
                    onClick = {
                        isExpanded = false
                        onAction(HabitFormAction.UnitChosen(choice))
                    }
                )
            }
        }
    }
}

@Composable
private fun CustomUnitField(state: HabitFormState, onAction: (HabitFormAction) -> Unit) {
    OutlinedTextField(
        value = state.customUnitLabel,
        onValueChange = { onAction(HabitFormAction.CustomUnitChanged(it)) },
        label = { Text(stringResource(R.string.ui_habitform_custom_unit)) },
        singleLine = true,
        isError = HabitFormMessage.CUSTOM_UNIT_EMPTY in state.messages,
        supportingText = fieldMessage(
            state,
            HabitFormMessage.CUSTOM_UNIT_EMPTY,
            R.string.ui_habitform_custom_unit_empty
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PlusStepField(state: HabitFormState, onAction: (HabitFormAction) -> Unit) {
    OutlinedTextField(
        value = plusStepText(state.plusStep),
        onValueChange = { onAction(HabitFormAction.PlusStepChanged(it)) },
        label = { Text(stringResource(R.string.ui_habitform_plus_step)) },
        suffix = { Text(stepUnitText(state)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        isError = HabitFormMessage.PLUS_STEP_NOT_ABOVE_ZERO in state.messages,
        supportingText = fieldMessage(
            state,
            HabitFormMessage.PLUS_STEP_NOT_ABOVE_ZERO,
            R.string.ui_habitform_plus_step_not_above_zero
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

private fun fieldMessage(
    state: HabitFormState,
    message: HabitFormMessage,
    @StringRes text: Int
): (@Composable () -> Unit)? = if (message in state.messages) {
    { Text(stringResource(text)) }
} else {
    null
}

@Composable
private fun plusStepText(plusStep: PlusStepField): String = when (plusStep) {
    is PlusStepField.FollowsUnit -> decimalInputText(plusStep.defaultStep, LocalConfiguration.current.locales[0])
    is PlusStepField.Typed -> plusStep.text
}

@Composable
private fun unitChoiceText(choice: UnitChoice): String = when (choice) {
    is UnitChoice.BuiltIn -> stringResource(builtInUnitName(choice.id))
    UnitChoice.Custom -> stringResource(R.string.ui_habitform_unit_custom)
}

@Composable
private fun stepUnitText(state: HabitFormState): String = when (val unit = state.unit) {
    is UnitChoice.BuiltIn -> stringResource(builtInUnitName(unit.id))
    UnitChoice.Custom -> state.customUnitLabel
}

@Preview
@Composable
private fun HabitFormContentPreview() {
    val state = HabitFormState(
        name = "Water",
        unit = UnitChoice.BuiltIn("l"),
        unitChoices = listOf(UnitChoice.BuiltIn("l"), UnitChoice.Custom),
        customUnitLabel = "",
        plusStep = PlusStepField.FollowsUnit(BigDecimal("0.25")),
        messages = emptySet(),
        isSaveEnabled = true,
        isSaved = false
    )
    MaterialTheme {
        HabitFormContent(state = state, onAction = {}, onBack = {})
    }
}

@Preview
@Composable
private fun HabitFormContentMessagesPreview() {
    val state = HabitFormState(
        name = " ",
        unit = UnitChoice.Custom,
        unitChoices = listOf(UnitChoice.BuiltIn("l"), UnitChoice.Custom),
        customUnitLabel = "",
        plusStep = PlusStepField.Typed("0"),
        messages = HabitFormMessage.entries.toSet(),
        isSaveEnabled = false,
        isSaved = false
    )
    MaterialTheme {
        HabitFormContent(state = state, onAction = {}, onBack = {})
    }
}
