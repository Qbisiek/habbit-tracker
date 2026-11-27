package kubisp.habittracker.domain.habit

/** BHV-HABIT-R4: every habit that is not archived, every day (v1 has no weekday schedule), in display order. */
fun habitsOnDayScreen(habits: List<Habit>, languageTag: String): List<Habit> =
    habitsInDisplayOrder(habits.filter { it.archivedOn == null }, languageTag)
