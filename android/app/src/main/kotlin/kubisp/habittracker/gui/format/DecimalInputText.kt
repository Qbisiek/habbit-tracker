package kubisp.habittracker.gui.format

import java.math.BigDecimal
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * [value] as the text of an input field: the decimal separator of [locale], no grouping and no trailing zeros, so the
 * input filter of the field (`docs/UX.md` section 5) accepts it when the user edits it.
 */
fun decimalInputText(value: BigDecimal, locale: Locale): String {
    val separator = DecimalFormatSymbols.getInstance(locale).decimalSeparator
    return value.stripTrailingZeros().toPlainString().replace('.', separator)
}
