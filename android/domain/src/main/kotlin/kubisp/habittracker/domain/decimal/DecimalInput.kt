package kubisp.habittracker.domain.decimal

import java.math.BigDecimal

private val decimalNumber = Regex("""-?(\d+[.,]?\d*|[.,]\d+)""")

/**
 * The typed number, not rounded yet (BHV-ENTRY-9c tells "rounds to 0.000" from "not above 0"), or null when [text]
 * is not a number. Both a comma and a dot are accepted as the decimal separator (BHV-ENTRY-R5, BHV-ENTRY-9d). A minus
 * sign is accepted, so a negative value reaches the rule "above 0" instead of looking like a missing value
 * (BHV-ENTRY-9b); exponents and grouping are not numbers here.
 */
fun parseDecimalInput(text: String): BigDecimal? {
    if (!decimalNumber.matches(text)) return null
    return BigDecimal(text.replace(',', '.'))
}

private val allowedDecimalInput = Regex("""\d*[.,]?\d*""")

/**
 * The input filter of a typed value, target or "+" step (`docs/UX.md` section 5): digits and at most one decimal
 * separator, no minus sign. An edit that gives a text it does not allow is not taken, so no message is needed for it.
 */
fun isAllowedDecimalInput(text: String): Boolean = allowedDecimalInput.matches(text)
