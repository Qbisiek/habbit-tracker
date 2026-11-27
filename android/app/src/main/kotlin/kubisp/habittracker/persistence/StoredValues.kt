package kubisp.habittracker.persistence

import java.math.BigDecimal
import kubisp.habittracker.domain.decimal.ExactDecimal

/** Room assigns the next order of addition (an AUTOINCREMENT key) to a row inserted with 0. */
const val NOT_YET_ADDED = 0L

/** The plain text of the exact decimal with its 3 places, e.g. "0.250": lossless, without floating point. */
fun ExactDecimal.toStored(): String = toBigDecimal().toPlainString()

fun exactDecimalFromStored(stored: String): ExactDecimal = ExactDecimal.of(BigDecimal(stored))
