package kubisp.habittracker.gui.day

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kubisp.habittracker.R
import kubisp.habittracker.gui.format.dateWithWeekdayText
import kubisp.habittracker.presentation.day.DayAction

/** UI-DAY-HEADER: the selected date between the previous and next arrows; the date opens UI-DAY-PICKER. */
@Composable
fun DayHeader(date: LocalDate, onAction: (DayAction) -> Unit) {
    var isPickerOpen by rememberSaveable { mutableStateOf(false) }
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        ArrowButton(R.drawable.ic_chevron_left, R.string.ui_day_header_previous) { onAction(DayAction.ShowPreviousDay) }
        TextButton(onClick = { isPickerOpen = true }, modifier = Modifier.weight(1f)) {
            Icon(
                painter = painterResource(R.drawable.ic_calendar),
                contentDescription = stringResource(R.string.ui_day_picker_open)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = dateWithWeekdayText(date, LocalConfiguration.current.locales[0]),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        }
        ArrowButton(R.drawable.ic_chevron_right, R.string.ui_day_header_next) { onAction(DayAction.ShowNextDay) }
    }
    if (isPickerOpen) {
        DayPicker(
            date = date,
            onPicked = { picked ->
                isPickerOpen = false
                onAction(DayAction.PickDate(picked))
            },
            onDismiss = { isPickerOpen = false }
        )
    }
}

@Composable
private fun ArrowButton(@DrawableRes icon: Int, @StringRes description: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(painter = painterResource(icon), contentDescription = stringResource(description))
    }
}

// Any date can be picked, also one after today (BHV-ENTRY-R3).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayPicker(date: LocalDate, onPicked: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = date.toPickerMillis())
    val confirm = {
        val pickedMillis = pickerState.selectedDateMillis
        if (pickedMillis == null) onDismiss() else onPicked(pickedMillis.toPickerDate())
    }
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = confirm) { Text(stringResource(R.string.ui_day_picker_ok)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.ui_day_picker_cancel)) } }
    ) {
        DatePicker(state = pickerState)
    }
}

// The Material3 date picker works with the start of the day in UTC, whatever the device time zone.
private fun LocalDate.toPickerMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toPickerDate(): LocalDate = Instant.ofEpochMilli(this).atOffset(ZoneOffset.UTC).toLocalDate()
