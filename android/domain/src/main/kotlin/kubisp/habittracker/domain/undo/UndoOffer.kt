package kubisp.habittracker.domain.undo

import java.time.LocalDate
import kubisp.habittracker.domain.entry.Entry

/** What the current undo offer would bring back (`docs/DESIGN.md` section 5.7, Decision 93). */
sealed interface UndoOffer {
    /** [entry]: the copy of the deleted entry, written back with its identity and values on Undo (BHV-ENTRY-22b). */
    data class DeletedEntry(val entry: Entry) : UndoOffer {
        /** BHV-ENTRY-R6a: a new entry of the habit on a day that the deleted entry covers withdraws the offer. */
        internal fun isWithdrawnBy(added: Entry): Boolean = added.habitId == entry.habitId && added.day() == entry.day()
    }
}

private fun Entry.day(): LocalDate = when (this) {
    is Entry.Numeric -> dateTime.toLocalDate()
}
