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
