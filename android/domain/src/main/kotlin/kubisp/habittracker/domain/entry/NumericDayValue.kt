package kubisp.habittracker.domain.entry

import java.math.BigDecimal
import java.time.LocalDate
import kubisp.habittracker.domain.decimal.ExactDecimal

/** BHV-ENTRY-R4: the total of the numeric entries of [date]; 0 when it has none. */
fun numericValueOfDay(entries: List<Entry>, date: LocalDate): ExactDecimal = entries
    .filterIsInstance<Entry.Numeric>()
    .filter { it.dateTime.toLocalDate() == date }
    .fold(ExactDecimal.of(BigDecimal.ZERO)) { total, entry -> total + entry.value }
