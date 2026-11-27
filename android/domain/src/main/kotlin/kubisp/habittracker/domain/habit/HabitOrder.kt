package kubisp.habittracker.domain.habit

import java.text.Collator
import java.util.Locale

/**
 * BHV-HABIT-R4: by name with the collator of the app language, ignoring case, then by creation day and time.
 * [habits] must be in the order of addition (the [kubisp.habittracker.domain.port.Habits] port gives them so):
 * the sort is stable, so that order is the last tie-break (`docs/DESIGN.md` section 4.6, Decision 92).
 */
fun habitsInDisplayOrder(habits: List<Habit>, languageTag: String): List<Habit> {
    val byName = compareBy<Habit, String>(nameCollator(languageTag)) { it.name }
    return habits.sortedWith(byName.thenBy { it.createdAt })
}

// SECONDARY strength ignores case (a tertiary difference) but keeps accents apart.
private fun nameCollator(languageTag: String): Collator =
    Collator.getInstance(Locale.forLanguageTag(languageTag)).apply { strength = Collator.SECONDARY }
