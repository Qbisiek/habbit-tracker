package kubisp.habittracker.gui.format

import android.text.format.DateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** [date] with its weekday and year in the short form of [locale], e.g. "Wed, Nov 11, 2026", "śr., 11 lis 2026". */
fun dateWithWeekdayText(date: LocalDate, locale: Locale): String {
    val pattern = DateFormat.getBestDateTimePattern(locale, "yMMMEd")
    return DateTimeFormatter.ofPattern(pattern, locale).format(date)
}
