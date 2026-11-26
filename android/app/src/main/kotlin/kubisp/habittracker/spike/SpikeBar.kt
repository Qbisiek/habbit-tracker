package kubisp.habittracker.spike

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** T1.6 variant (e), proposed by the user: a rectangular progress bar with the text inside, instead of the ring. */
@Composable
fun ProgressBar(text: String, fill: BarFill, width: Dp) {
    Box(
        modifier =
            Modifier
                .width(width)
                .heightIn(min = BAR_MIN_HEIGHT)
                .clip(RoundedCornerShape(4.dp))
                .background(BAR_TRACK),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.matchParentSize()) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(fill.fraction).background(fill.color))
        }
        BarText(text)
    }
}

// Red text background when the text does not fit in the bar, so it is visible on a screenshot.
@Composable
private fun BarText(text: String) {
    var hasOverflow by remember { mutableStateOf(false) }
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = BAR_TEXT,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        onTextLayout = { hasOverflow = it.hasVisualOverflow },
        modifier = if (hasOverflow) Modifier.background(Color.Red) else Modifier
    )
}

/** The fixed bar width: the widest daily text at the current font scale, so the rows stay aligned. */
@Composable
fun barWidth(): Dp {
    val measured =
        rememberTextMeasurer().measure(
            text = WIDEST_DAILY_TEXT,
            style = MaterialTheme.typography.labelMedium,
            softWrap = false
        )
    return with(LocalDensity.current) { measured.size.width.toDp() } + BAR_TEXT_PADDING * 2
}

/** Light fill tones, so the dark text stays readable over the filled and the empty part alike. */
enum class BarFill(val fraction: Float, val color: Color) {
    BuildHalf(0.5f, Color(0xFFC8E6C9)),
    BuildFull(1f, Color(0xFFA5D6A7)),
    QuitLow(0.45f, Color(0xFFDCEDC8)),
    QuitNearLimit(0.99f, Color(0xFFFFE0B2)),
    QuitAbove(1f, Color(0xFFFFCDD2))
}

private const val WIDEST_DAILY_TEXT = "23h59m/23h59m"
private val BAR_MIN_HEIGHT = 24.dp
private val BAR_TEXT_PADDING = 6.dp
private val BAR_TRACK = Color(0xFFECEFF1)
private val BAR_TEXT = Color(0xFF1C1B1F)
