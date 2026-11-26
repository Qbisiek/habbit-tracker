package kubisp.habittracker.spike

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt
import kubisp.habittracker.R

/** T1.6: the progress ring of docs/UX.md section 3 with its text in one or two lines inside, or with no text. */
@Composable
fun Ring(lines: List<String>, diameter: Dp) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val fillColor = MaterialTheme.colorScheme.tertiary
    Box(modifier = Modifier.size(diameter), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(diameter)) {
            val stroke = RING_STROKE.toPx()
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)
            drawArc(trackColor, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
            drawArc(fillColor, -90f, RING_SAMPLE_SWEEP, false, topLeft, arcSize, style = Stroke(stroke))
        }
        if (lines.isNotEmpty()) RingText(lines)
    }
}

@Composable
private fun RingText(lines: List<String>) {
    Text(
        text = lines.joinToString("\n"),
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
        softWrap = false
    )
}

/** The ring texts of docs/UX.md section 8 item 3 with the 2-unit rule, in one and in two lines, in several sizes. */
@Composable
fun RingSamples() {
    RING_TEXTS.forEach { text ->
        RingSampleRow(lines = listOf(text))
        if (text.contains('/')) RingSampleRow(lines = splitAtLimit(text))
    }
}

private fun splitAtLimit(text: String): List<String> {
    val result = text.substringBefore('/')
    return listOf(result, "/" + text.substringAfter('/'))
}

@Composable
private fun RingSampleRow(lines: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        RING_SAMPLE_DIAMETERS.forEach { diameter ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Ring(lines = lines, diameter = diameter)
                Text(text = "${diameter.value.toInt()} dp", style = MaterialTheme.typography.labelSmall)
                RingFit(lines = lines, diameter = diameter)
            }
        }
    }
}

@Composable
private fun RingFit(lines: List<String>, diameter: Dp) {
    val overflow = ringTextOverflow(lines, diameter)
    val label =
        if (overflow <= 0.dp) {
            stringResource(R.string.spike_widths_fits)
        } else {
            stringResource(R.string.spike_widths_overflow, overflow.value.toInt() + 1)
        }
    Text(text = label, style = MaterialTheme.typography.labelSmall)
}

// The text block must fit inside the circle: at its height h the circle of diameter d is sqrt(d^2 - h^2) wide.
@Composable
private fun ringTextOverflow(lines: List<String>, diameter: Dp): Dp {
    val measured =
        rememberTextMeasurer().measure(
            text = lines.joinToString("\n"),
            style = MaterialTheme.typography.labelSmall,
            softWrap = false
        )
    val (width, height) = with(LocalDensity.current) { measured.size.width.toDp() to measured.size.height.toDp() }
    val inner = (diameter - (RING_STROKE + RING_TEXT_PADDING) * 2).value
    if (height.value >= inner) return maxOf(width, height - inner.dp)
    val chord = sqrt(inner * inner - height.value * height.value)
    return width - chord.dp
}

// With the 2-unit rule and no seconds in a limit above 1 h (user, 2026-11-26); weekly limits may exceed 24 h.
private val RING_TEXTS = listOf("1h20m/3h", "23h59m/23h59m", "59m59s/59m59s", "167h59m/168h", "1250/2000", "100%")
private val RING_SAMPLE_DIAMETERS = listOf(40.dp, 48.dp, 56.dp)
private val RING_STROKE = 4.dp
private val RING_TEXT_PADDING = 2.dp
private const val RING_SAMPLE_SWEEP = 240f
