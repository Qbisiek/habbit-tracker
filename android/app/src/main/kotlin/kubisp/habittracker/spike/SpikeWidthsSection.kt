package kubisp.habittracker.spike

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kubisp.habittracker.R

/**
 * T1.6: width prototypes of docs/UX.md section 8 item 3 and the variants agreed with the user on 2026-11-26.
 * Overflows are measured and printed, so the result does not depend on judging screenshots by eye. Sample values are
 * data, the same in both languages.
 */
@Composable
fun WidthsSection() {
    Text(text = stringResource(R.string.spike_widths_title), style = MaterialTheme.typography.titleLarge)
    ScreenInfo()
    Subtitle(R.string.spike_widths_quick_in_row)
    QuickButtonRow(labels = START_BUTTONS, modifier = Modifier.fillMaxWidth())
    QuickButtonRow(labels = END_BUTTONS, modifier = Modifier.fillMaxWidth())
    Subtitle(R.string.spike_widths_quick_blocks)
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        QuickButtonBlock(labels = START_BUTTONS, modifier = Modifier.weight(1f))
        QuickButtonBlock(labels = END_BUTTONS, modifier = Modifier.weight(1f))
    }
    DayRowSamples()
    BarRowSamples()
    Subtitle(R.string.spike_widths_rings)
    RingSamples()
}

@Composable
private fun DayRowSamples() {
    Subtitle(R.string.spike_widths_row_one_line)
    OneLineDayRow(SHORT_SAMPLE)
    OneLineDayRow(LONG_SAMPLE)
    Subtitle(R.string.spike_widths_row_text_inside)
    TwoLineDayRow(SHORT_SAMPLE) { Ring(lines = SHORT_SAMPLE.ringLines, diameter = 56.dp) }
    TwoLineDayRow(LONG_SAMPLE) { Ring(lines = LONG_SAMPLE.ringLines, diameter = 56.dp) }
    Subtitle(R.string.spike_widths_row_text_beside)
    TwoLineDayRow(SHORT_SAMPLE) { RingWithTextBeside(SHORT_SAMPLE.ringLines) }
    TwoLineDayRow(LONG_SAMPLE) { RingWithTextBeside(LONG_SAMPLE.ringLines) }
    Subtitle(R.string.spike_widths_row_indicator_top)
    IndicatorTopDayRow(SHORT_SAMPLE)
    IndicatorTopDayRow(LONG_SAMPLE)
}

@Composable
private fun Subtitle(@StringRes text: Int) {
    Text(text = stringResource(text), style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun ScreenInfo() {
    val density = LocalDensity.current
    val widthDp = with(density) { LocalWindowInfo.current.containerSize.width.toDp() }
    Text(stringResource(R.string.spike_widths_screen, widthDp.value.toInt(), density.fontScale.toString()))
}

@Composable
private fun QuickButtonRow(labels: List<String>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEach { label ->
            OutlinedButton(onClick = {}, contentPadding = QUICK_BUTTON_PADDING, modifier = Modifier.weight(1f)) {
                OverflowMarkedText(label)
            }
        }
    }
}

@Composable
private fun QuickButtonBlock(labels: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        labels.chunked(2).forEach { QuickButtonRow(labels = it) }
    }
}

// Red background when the text does not fit in one line, so a clipped label is visible on a screenshot.
@Composable
private fun OverflowMarkedText(text: String, style: TextStyle = MaterialTheme.typography.labelLarge) {
    var hasOverflow by remember { mutableStateOf(false) }
    Text(
        text = text,
        style = style,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        onTextLayout = { hasOverflow = it.hasVisualOverflow },
        modifier = if (hasOverflow) Modifier.background(Color.Red) else Modifier
    )
}

private data class DayRowSample(
    val name: String,
    val isRunning: Boolean,
    val value: String,
    val ringLines: List<String>
)

@Composable
private fun OneLineDayRow(sample: DayRowSample) {
    MeasuredName(sample.name) { name ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            name(Modifier.weight(1f))
            TimerControls(sample)
            OverflowMarkedText(text = sample.value, style = MaterialTheme.typography.bodyMedium)
            Ring(lines = sample.ringLines, diameter = 48.dp)
        }
    }
}

@Composable
private fun TwoLineDayRow(sample: DayRowSample, indicator: @Composable () -> Unit) {
    MeasuredName(sample.name) { name ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                name(Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TimerControls(sample)
                    OverflowMarkedText(text = sample.value, style = MaterialTheme.typography.bodyMedium)
                }
            }
            indicator()
        }
    }
}

@Composable
private fun BarRowSamples() {
    Subtitle(R.string.spike_widths_row_bar)
    val width = barWidth()
    BAR_SAMPLES.forEach { (sample, text, fill) ->
        BarDayRow(sample) { ProgressBar(text = text, fill = fill, width = width) }
    }
}

@Composable
private fun BarDayRow(sample: DayRowSample, bar: @Composable () -> Unit) {
    MeasuredName(sample.name) { name ->
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                name(Modifier.weight(1f))
                bar()
            }
            FlowRow(itemVerticalAlignment = Alignment.CenterVertically) {
                TimerControls(sample)
                OverflowMarkedText(text = sample.value, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// The indicator only in the first line, so the second line has the full width and wraps when it does not fit.
@Composable
private fun IndicatorTopDayRow(sample: DayRowSample) {
    MeasuredName(sample.name) { name ->
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                name(Modifier.weight(1f))
                RingWithTextBeside(sample.ringLines)
            }
            FlowRow(itemVerticalAlignment = Alignment.CenterVertically) {
                TimerControls(sample)
                OverflowMarkedText(text = sample.value, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// Shows the row and, under it, the width the row leaves for the habit name.
@Composable
private fun MeasuredName(name: String, row: @Composable (name: @Composable (Modifier) -> Unit) -> Unit) {
    var nameWidthPx by remember { mutableIntStateOf(0) }
    val nameWidth = with(LocalDensity.current) { nameWidthPx.toDp() }
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        row { modifier ->
            Text(
                text = name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = modifier.onSizeChanged { nameWidthPx = it.width }
            )
        }
        Text(
            text = stringResource(R.string.spike_widths_name_width, nameWidth.value.toInt()),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun TimerControls(sample: DayRowSample) {
    if (sample.isRunning) {
        IconAction(R.drawable.spike_ic_stop, R.string.spike_widths_stop_timer)
        OverflowMarkedText(text = SESSION_TIME, style = MaterialTheme.typography.bodyMedium)
    } else {
        IconAction(R.drawable.spike_ic_start, R.string.spike_widths_start_timer)
    }
    IconAction(android.R.drawable.ic_menu_edit, R.string.spike_widths_manual_entry)
}

@Composable
private fun IconAction(@DrawableRes icon: Int, @StringRes description: Int) {
    IconButton(onClick = {}) {
        Icon(painter = painterResource(icon), contentDescription = stringResource(description))
    }
}

@Composable
private fun RingWithTextBeside(lines: List<String>) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Ring(lines = emptyList(), diameter = 32.dp)
        OverflowMarkedText(text = lines.joinToString(""), style = MaterialTheme.typography.labelMedium)
    }
}

private val START_BUTTONS = listOf("-5m", "-15m", "-30m", "-1h")
private val END_BUTTONS = listOf("+5m", "+15m", "+30m", "+1h")
private val QUICK_BUTTON_PADDING = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
private const val SESSION_TIME = "1:23:45"
private val SHORT_SAMPLE = DayRowSample("Run", isRunning = false, "1 h 20 min", listOf("1h20m", "/3h"))
private val LONG_SAMPLE =
    DayRowSample("Reading technical books in the evening", isRunning = true, "23 h 59 min", listOf("23h59m", "/23h59m"))

private val BAR_SAMPLES =
    listOf(
        Triple(SHORT_SAMPLE, "1h20m/3h", BarFill.QuitLow),
        Triple(LONG_SAMPLE, "23h59m/23h59m", BarFill.QuitNearLimit),
        Triple(LONG_SAMPLE, "3h1m/3h", BarFill.QuitAbove),
        Triple(LONG_SAMPLE, "1234.567/2000", BarFill.QuitLow),
        Triple(LONG_SAMPLE, "167h59m/168h", BarFill.QuitNearLimit),
        Triple(SHORT_SAMPLE, "75%", BarFill.BuildHalf),
        Triple(SHORT_SAMPLE, "100%", BarFill.BuildFull)
    )

@Preview(widthDp = 360)
@Composable
private fun WidthsSectionPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            WidthsSection()
        }
    }
}
