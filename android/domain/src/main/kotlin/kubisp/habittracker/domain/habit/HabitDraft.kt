package kubisp.habittracker.domain.habit

/** Checked by [validateHabitDraft] (BHV-HABIT-R1). */
data class HabitDraft(val name: String, val direction: Direction, val measurement: Measurement)
