package kubisp.habittracker.domain.habit

/**
 * The list of the Habits screen (UI-HABITS-LIST, `docs/UX.md` section 5): the habits that are not archived, in display
 * order (BHV-HABIT-R4). The archived section of the screen comes with T4.4.
 */
fun habitsOnHabitsScreen(habits: List<Habit>, languageTag: String): List<Habit> =
    habitsInDisplayOrder(habits.filter { it.archivedOn == null }, languageTag)
