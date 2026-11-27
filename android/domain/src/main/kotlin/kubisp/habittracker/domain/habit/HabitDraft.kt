package kubisp.habittracker.domain.habit

/** The habit form as entered; checked by [validateHabitDraft] (BHV-HABIT-R1). */
data class HabitDraft(val name: String, val direction: Direction, val measurement: MeasurementDraft)
