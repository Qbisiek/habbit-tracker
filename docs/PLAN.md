# Plan: Habit Tracker (working title)

> **STATUS (2026-11-25): APPROVED BY THE USER.**

## Summary

The plan builds the approved design (`docs/DESIGN.md`, `docs/UX.md`,
`docs/REQUIREMENTS.md`) in small steps; every milestone ends with an APK the user tests
by hand on a phone. Order and reasons (user decisions of 2026-11-24):
- M1 first: the project skeleton and the four spikes carried from `docs/DESIGN.md`
  section 10, because their results can change the UX or the design.
- M2: one kind of habit end to end, numeric build without goals (it exercises the
  "+" action, typed values, exact decimals, units and the entry forms).
- M3: export and import early, with the full data model and the full file format, so
  that every later milestone can load its test data from a file.
- M4: breadth for the first habit (management, statistics, language) before new
  variants.
- M5-M6: goals (with the quit direction) before more habit kinds and before the
  timer; goal logic is pure Domain, covered by unit tests of the `BHV-*` scenarios.
- M7-M8: the other habit kinds, then the timer and its notification (the risky API
  points are checked by the spikes of M1).
- M9: the success criteria (`docs/REQUIREMENTS.md` section 4) and the finishing
  touches.

Principles: a running app after every milestone; breadth before variants; export and
import early; function before look (a plain UI until M9); spikes first.

## Conventions

Agreed with the user on 2026-11-24; they apply to every task.
- Texts: every task with UI adds its texts in Polish and English to the resources
  (REQ-C4); there is no separate translation task.
- Navigation bar: only the tabs that work; Statistics is added in M4, no placeholder.
- Database schema before v1: a schema change recreates the database, without a
  migration; test data are kept with export / import from M3 on (data of M2 are lost
  when moving to M3). Real migrations (`docs/DESIGN.md` section 8) start with the
  first version the user uses every day, at the latest in M9.

## M1 - Skeleton and spikes
**Goal:** The Gradle project (a pure Kotlin `domain` module and the `app` module) and
prototypes for the four checks carried to the plan (`docs/DESIGN.md` section 10).
**Manual test:** Install the APK with the prototypes and judge the results: the
language of the timer notification texts, the width of the tightest rows on a phone,
the Undo snackbar above the entries sheet; the listing of shown notifications works.

| ID   | Task | Status | Effort | Depends on | Covers |
|------|------|--------|--------|------------|--------|
| T1.1 | Check the current versions of the toolchain and libraries in the official docs (AGP, Gradle, Kotlin, Compose BOM, Navigation Compose, lifecycle ViewModel, Room, KSP, kotlinx.serialization, coroutines, JUnit) | done | S | - | - |
| T1.2 | Create the Gradle project: pure Kotlin/JVM `domain` module, `app` module with Compose and minimum API 33, version catalog, unit tests running in both modules | done | M | T1.1 | REQ-C3 |
| T1.3 | Disable Auto Backup (manifest and data extraction rules), no INTERNET permission; check question 21 (transfer to iOS) | in progress | S | T1.2 | REQ-C1, REQ-C5 |
| T1.4 | Spike: do the timer notification texts follow the per-app language | todo | M | T1.2 | - |
| T1.5 | Spike: the API that lists the app's shown notifications, for the notification sync | todo | S | T1.2 | - |
| T1.6 | Spike: widths on a phone prototype (eight quick buttons, Day row with a running timer, widest ring texts) | todo | M | T1.2 | - |
| T1.7 | Spike: the Undo snackbar above an open `ModalBottomSheet` | todo | S | T1.2 | - |
| T1.8 | Record the spike results in `docs/UX.md` / `docs/DESIGN.md` (each change approved by the user) and remove the prototypes | todo | S | T1.4, T1.5, T1.6, T1.7 | - |

**T1.1:** Sources: `docs/DESIGN.md` section 2. Also choose SharedPreferences or
Jetpack DataStore for the two notification flags (Decision 97) when it is needed (M8).
**T1.2:** Application ID and package: `kubisp.habittracker` (user, 2026-11-24).
**T1.4-T1.7:** Prototypes live in a `spike` package of the `app` module and are
removed in T1.8. Sources: `docs/UX.md` section 8 items 1, 3, 4; `docs/DESIGN.md`
section 10 (question 7) and section 5.5 (fallback for the notification language).

## M2 - First habit end to end
**Goal:** A numeric build habit without a goal: a minimal habit form, the Day screen,
entries (add with "+" and typed, edit, delete with Undo), past days, Room storage,
Polish and English texts in resources.
**Manual test:** Create a habit (e.g. water in litres), log entries today and on past
days, edit them, delete one and undo; close and reopen the app and the data are kept.

| ID    | Task | Status | Effort | Depends on | Covers |
|-------|------|--------|--------|------------|--------|
| T2.1  | Domain: numeric habit and entry model, exact decimals, built-in units with default steps, custom unit | todo | M | T1.2 | REQ-M7, REQ-S2, BHV-HABIT-14 |
| T2.2  | Domain: ports (Habits, Entries, Data operations, Clock), Domain operations skeleton with the lock, hand-written fakes for tests | todo | M | T2.1 | - |
| T2.3  | Domain: create a habit and its validation (name, unit, "+" step) | todo | S | T2.2 | REQ-M6, BHV-HABIT-R1, BHV-HABIT-1, BHV-HABIT-11, BHV-HABIT-12 |
| T2.4  | Domain: numeric entries (add with "+" and typed, edit, delete), validation, default moment, value of a day | todo | L | T2.2 | REQ-M5, BHV-ENTRY-R1, BHV-ENTRY-R2, BHV-ENTRY-R3, BHV-ENTRY-R4, BHV-ENTRY-R5, BHV-ENTRY-R6, BHV-ENTRY-R6b, BHV-ENTRY-R8, BHV-ENTRY-4, BHV-ENTRY-9a, BHV-ENTRY-9b, BHV-ENTRY-9c, BHV-ENTRY-9d, BHV-ENTRY-10, BHV-ENTRY-11, BHV-ENTRY-12a, BHV-ENTRY-12b, BHV-ENTRY-16, BHV-ENTRY-17, BHV-ENTRY-18, BHV-ENTRY-20, BHV-ENTRY-21a, BHV-ENTRY-21b, BHV-ENTRY-21c, BHV-ENTRY-21d |
| T2.5  | Domain: undo offer of a deleted entry, its expiry and withdrawal | todo | M | T2.4 | BHV-ENTRY-R6a, BHV-ENTRY-22a, BHV-ENTRY-22b |
| T2.6  | Domain: which habits are shown and their order (`Collator`) | todo | S | T2.3 | BHV-HABIT-R4, BHV-HABIT-2, BHV-HABIT-3, BHV-HABIT-16 |
| T2.7  | Persistence: Room with habits and entries, entries read by date range, instrumented tests (G8 included) | todo | L | T2.2 | REQ-C2, REQ-SC1 |
| T2.8  | Platform services: Clock adapter (now, time zone, ticks, day and time zone change) | todo | M | T2.2 | - |
| T2.9  | App: composition root, App shell with the Undo snackbar, bottom navigation bar | todo | M | T2.7, T2.8 | - |
| T2.10 | Habits screen, minimal: list and the form of a new numeric build habit | todo | M | T2.3, T2.9 | REQ-M6 |
| T2.11 | Day screen: header (arrows, calendar picker, date follows today), rows with "+", typed value and the value of the day | todo | L | T1.8, T2.4, T2.6, T2.9 | REQ-M5, REQ-M6, BHV-DAY-R1, BHV-DAY-1, BHV-DAY-2, BHV-ENTRY-2 |
| T2.12 | Entries sheet and the numeric entry form: add, edit, delete with Undo, validation messages | todo | L | T2.5, T2.11 | REQ-M5 |

**T2.5:** `docs/DESIGN.md` section 5.7. **T2.6:** BHV-HABIT-R4; needed by the Day
screen, so it comes before the rest of habit management (M4).
**T2.11:** depends on T1.8 because the spike results can change the Day row.

## M3 - Export and import
**Goal:** The full data model (all measurement types, goals, running timers) in Domain
and Room, and the versioned JSON export and import (both modes, "Export and replace").
**Manual test:** Export the data, import a prepared file with test data in both modes,
and see a rejected invalid file leave the data unchanged.

| ID   | Task | Status | Effort | Depends on | Covers |
|------|------|--------|--------|------------|--------|
| T3.1 | Domain: full data model (all measurement types as a sealed hierarchy, goals as data, running timers, UUIDs and the order of addition) | todo | L | T2.4 | REQ-M1 |
| T3.2 | Persistence: Room schema of the full model; Data operations: export snapshot, *Replace all*, *Add to existing* | todo | L | T2.7, T3.1 | REQ-S1, BHV-IMPORT-R3 |
| T3.3 | Platform services: Backup file adapter (file-model classes with kotlinx.serialization, two-step read with a streaming header, a write that reports success only for the whole file) | todo | L | T3.1 | REQ-S1, BHV-EXPORT-3, BHV-IMPORT-9, BHV-IMPORT-9a, BHV-IMPORT-9b |
| T3.4 | Domain: format rules (version check, BHV-IMPORT-R4 without goal validity, unique identities, the import summary) | todo | M | T3.1 | REQ-S1, BHV-IMPORT-R1, BHV-IMPORT-R4, BHV-IMPORT-1, BHV-IMPORT-2a, BHV-IMPORT-3a, BHV-IMPORT-3b, BHV-IMPORT-4a, BHV-IMPORT-4b, BHV-IMPORT-7, BHV-IMPORT-13, BHV-IMPORT-15, BHV-IMPORT-16, BHV-IMPORT-18 |
| T3.5 | Domain: operations export, import in both modes (new identities on *Add to existing*) and "Export and replace" under one lock | todo | M | T3.2, T3.3, T3.4 | REQ-S1, BHV-EXPORT-R1, BHV-IMPORT-5a, BHV-IMPORT-5b, BHV-IMPORT-5d, BHV-IMPORT-5e, BHV-IMPORT-11, BHV-IMPORT-14, BHV-IMPORT-20, BHV-LANG-R3, BHV-LANG-5 |
| T3.6 | UI: Export / Import menu on the Habits screen, system save and open dialogs, import summary, mode choice, the *Replace all* dialog with "Export and replace", the rejection dialog, messages | todo | L | T2.10, T3.5 | REQ-S1, BHV-IMPORT-R2, BHV-IMPORT-5c, BHV-IMPORT-10 |
| T3.7 | Tests: round trip, sample files of format version 1 kept as fixtures, a file cut off after its header, a photo | todo | M | T3.4, T3.5 | REQ-SC1, BHV-EXPORT-1, BHV-EXPORT-2 |
| T3.8 | Test data: a sample backup file for manual tests, extended in later milestones | todo | S | T3.6 | - |

**T3.4:** the goal validity part of BHV-IMPORT-R4 is added in M5 with the goal rules
(user, 2026-11-24). **T3.5:** the notification sync and the one-time message after an
import (BHV-NOTIF-2, 4) and the check that "Export and replace" fits the time of a
receiver (`docs/DESIGN.md` section 7) are added in M8 (user, 2026-11-24).

## M4 - Breadth for the first habit
**Goal:** Habit management (edit, archive, restore, delete, order), the Statistics
screen without goals, the language switch.
**Manual test:** Use all three tabs: manage habits, read statistics over the presets
and a custom range, switch the language.

| ID   | Task | Status | Effort | Depends on | Covers |
|------|------|--------|--------|------------|--------|
| T4.1 | Domain: edit a habit (name, unit, "+" step), the unit-change warning check against the habit as stored, *save habit* for an edit | todo | M | T2.3, T3.2 | REQ-M6, REQ-S2, BHV-HABIT-R2, BHV-HABIT-5, BHV-HABIT-13a, BHV-HABIT-13b, BHV-HABIT-19 |
| T4.2 | Domain and Persistence: archive, restore, delete a habit (Data operations) for a habit without a goal and without a timer | todo | M | T3.2 | REQ-M6, BHV-HABIT-R3, BHV-HABIT-7c |
| T4.3 | UI: edit form (locked fields with the explanation, UI-DIALOG-UNIT), the "⋮" menu with archive and delete (confirmation dialog) | todo | M | T2.10, T4.1, T4.2 | REQ-M6, BHV-HABIT-4, BHV-HABIT-9 |
| T4.4 | UI: the "Archived" section of the Habits screen, the read-only view, "Restore" and "Delete" | todo | M | T4.3 | REQ-M6 |
| T4.5 | Domain: preset date ranges (BHV-STATS-R1), life of the habit with the earliest-entry date from the Entries port (BHV-STATS-R3), the Numeric figures (sum, average per day) | todo | M | T3.2 | REQ-M8, REQ-M8d, BHV-STATS-R1, BHV-STATS-R2, BHV-STATS-R3, BHV-STATS-11a, BHV-STATS-11b, BHV-STATS-17, BHV-STATS-18, BHV-HABIT-7d |
| T4.6 | UI: Statistics screen (range selector with presets and a custom range, cards, archived section, the "no days" message) and its tab in the navigation bar | todo | L | T2.9, T4.5 | REQ-M8 |
| T4.7 | Language: the dialog in the Habits menu, the per-app locale API, `generateLocaleConfig`, the effective language tag passed to Presentation and the re-sort of habits | todo | M | T2.10 | REQ-M9, REQ-C4, BHV-LANG-R1, BHV-LANG-R2, BHV-LANG-1, BHV-LANG-2, BHV-LANG-3, BHV-LANG-4, BHV-LANG-6, BHV-LANG-7, BHV-HABIT-16 |
| T4.8 | Test data: extend the sample file (an archived habit, numeric entries over a year for the statistics) | todo | S | T3.8, T4.4, T4.6 | - |

**T4.2:** the goal part of archive and restore (BHV-HABIT-R3, the three restore forms,
UI-DIALOG-RESTORE) is added in M6, the timer part (BHV-TIMER-R3) in M8 (user,
2026-11-25). **T4.5, T4.6:** figures only for Numeric; Binary and Duration (with the
split of a Duration entry across days, `docs/DESIGN.md` section 5.3) are added in M7.
Goal blocks in M5. **T4.7:** sources: `docs/UX.md` section 5 (Language), Decision 84,
`docs/DESIGN.md` section 3 (language tag); going back to the device language is only in
the system settings.

## M5 - Goals and the quit direction
**Goal:** Creating goals (periods day to year, repeating or for N periods), the
evaluation of a period, the progress indicator on the Day screen, the goal blocks in
statistics, quit habits with limits.
**Manual test:** Set a goal and a limit, log entries and check the percentage, the
colours and the statistics against a manual calculation.

| ID    | Task | Status | Effort | Depends on | Covers |
|-------|------|--------|--------|------------|--------|
| T5.1  | Domain: periods (day to year, computed start, last day of a goal for N periods, week parameters from the Clock port) | todo | M | T3.1 | REQ-M3, BHV-PERIOD-R1, BHV-PERIOD-R2, BHV-GOAL-R1, BHV-GOAL-R2, BHV-PERIOD-1, BHV-PERIOD-2, BHV-PERIOD-3, BHV-PERIOD-5, BHV-PERIOD-7, BHV-PERIOD-8, BHV-PERIOD-9 |
| T5.2  | Domain: goal validation (BHV-GOALVAL-R1) for Numeric, build and quit | todo | S | T3.1 | REQ-M3, BHV-GOALVAL-R1, BHV-GOALVAL-4, BHV-GOALVAL-5, BHV-GOALVAL-6, BHV-GOAL-22 |
| T5.3  | Domain: counting periods of a goal (BHV-GOAL-R5, Decision 98) and the evaluation of a period (BHV-EVAL-R1 to R3) for Numeric and Binary, build and quit | todo | L | T5.1 | REQ-M2, REQ-M3, REQ-SC3, BHV-EVAL-R1, BHV-EVAL-R2, BHV-EVAL-R3, BHV-GOAL-R5, BHV-EVAL-1, BHV-EVAL-2, BHV-EVAL-3, BHV-EVAL-4a, BHV-EVAL-4b, BHV-EVAL-5a, BHV-EVAL-5b, BHV-EVAL-6, BHV-PERIOD-6, BHV-ENTRY-14 |
| T5.4  | Domain: create a goal in *save habit* (new habit, or an existing habit without a goal, BHV-GOAL-10), the form preview (calendar period, "Last day") | todo | M | T4.1, T5.1, T5.2 | REQ-M3, BHV-GOAL-R3, BHV-GOAL-10 |
| T5.5  | Domain: goal validity in the import (the part of BHV-IMPORT-R4 moved from M3) | todo | S | T3.4, T5.2 | REQ-S1, BHV-IMPORT-6, BHV-IMPORT-8, BHV-IMPORT-17 |
| T5.6  | Domain: the progress indicator of the Day screen (BHV-DAY-R2) | todo | M | T5.3 | REQ-M6, BHV-DAY-R2, BHV-DAY-4, BHV-DAY-5, BHV-DAY-9a, BHV-DAY-9b, BHV-DAY-9c, BHV-DAY-9d |
| T5.7  | Domain: goal blocks of the statistics (BHV-STATS-R4 to R7) | todo | L | T4.5, T5.3 | REQ-M8c, REQ-SC2, BHV-STATS-R4, BHV-STATS-R5, BHV-STATS-R6, BHV-STATS-R7, BHV-STATS-1, BHV-STATS-2, BHV-STATS-3, BHV-STATS-4, BHV-STATS-5, BHV-STATS-6, BHV-STATS-7, BHV-STATS-13, BHV-STATS-21, BHV-STATS-22 |
| T5.8  | UI: habit form with the direction Build / Quit and the goal section (switch, target, period, Start with its calendar period, goal length, "Last day", validation messages) | todo | L | T4.3, T5.4 | REQ-M2, REQ-M3 |
| T5.9  | UI: the ring on the Day screen (UI-DAY-INDICATOR) | todo | M | T2.11, T5.6 | REQ-M6 |
| T5.10 | UI: goal blocks on the Statistics cards (UI-STATS-GOAL, UI-STATS-PROVISIONAL) | todo | M | T4.6, T5.7 | REQ-M8c |
| T5.11 | Test data: goals and quit habits in the sample file | todo | S | T4.8, T5.5, T5.8 | - |

**T5.2:** goal validation of Binary and Duration (BHV-GOALVAL-1a, 1b, 2, 3a, 3b, 7) is
added in M7 with those habit kinds (user, 2026-11-25). **T5.8:** in M5 an existing
active goal is shown read-only in the form with a short temporary note; changing it
comes in M6 (user, 2026-11-25). The "Archived goals" sections (form and Statistics)
come in M6, since no goal is archived before it. **T5.3:** the evaluation of Binary
is here, not in M7, because the goal-change scenarios of M6 (Fixture S) use a binary
habit; Binary entries are data of T3.1 and the tests use fakes (user, 2026-11-25).

## M6 - Goal changes and history
**Goal:** Changing goals (target, period, first day, N), replacing, archived goals,
restoring a habit with or without its goal.
**Manual test:** Change a goal in the middle of a period and check the Day screen and
the statistics against the rules.

| ID   | Task | Status | Effort | Depends on | Covers |
|------|------|--------|--------|------------|--------|
| T6.1 | Domain: goal status (archived, ended, pending, active), invariant 4 (an ended goal gets its last day as the archiving day on a write), the order among goals for the tie-break of BHV-DAY-R2 | todo | M | T5.3 | REQ-M3, BHV-PERIOD-4, BHV-DAY-6, BHV-DAY-7, BHV-DAY-8 |
| T6.2 | Domain: goal changes in *save habit*: archive or replace (BHV-GOAL-R4, R4a), switching a goal off, the default Start of the preview (Decision 89) | todo | L | T5.4, T6.1 | REQ-M3, BHV-GOAL-R4, BHV-GOAL-R4a, BHV-GOAL-1, BHV-GOAL-2, BHV-GOAL-3, BHV-GOAL-4, BHV-GOAL-5, BHV-GOAL-6a, BHV-GOAL-6b, BHV-GOAL-7a, BHV-GOAL-7b, BHV-GOAL-8, BHV-GOAL-9, BHV-GOAL-14, BHV-GOAL-15, BHV-GOAL-16, BHV-GOAL-17, BHV-GOAL-18, BHV-GOAL-19, BHV-GOAL-20, BHV-GOAL-21, BHV-IMPORT-19 |
| T6.3 | Domain: changes without a choice (BHV-GOAL-R6): N or mode changed, a pending goal edited in place, the need for the confirmation | todo | M | T6.2 | REQ-M3, BHV-GOAL-R6, BHV-GOAL-11a, BHV-GOAL-11b, BHV-GOAL-12a, BHV-GOAL-12b, BHV-GOAL-12c, BHV-GOAL-12d, BHV-GOAL-12e, BHV-HABIT-20 |
| T6.4 | Domain and Persistence: archive a habit with its goal (mark "archived with its habit", delete a pending goal) and restore in its three forms (BHV-HABIT-R3) | todo | M | T4.2, T6.1 | REQ-M3, REQ-M6, BHV-HABIT-R3, BHV-HABIT-6, BHV-HABIT-7a, BHV-HABIT-7b, BHV-HABIT-7e, BHV-HABIT-7f, BHV-HABIT-10, BHV-HABIT-15, BHV-HABIT-17 |
| T6.5 | UI: editable goal section (the behavior of Start), UI-DIALOG-GOAL-END, UI-HABITFORM-ARCHIVED-GOALS; remove the temporary read-only goal of M5 | todo | L | T5.8, T6.3 | REQ-M3, BHV-GOAL-R7, BHV-GOAL-13 |
| T6.6 | UI: UI-DIALOG-RESTORE on restore; "Archived goals" expanded in the read-only view | todo | S | T4.4, T6.4, T6.5 | REQ-M6, BHV-HABIT-18 |
| T6.7 | UI: the sub-block UI-STATS-ARCHIVED-GOALS on the cards and the card of an archived habit (the goal archived with the habit as an ordinary block, expanded when the sub-block would hold every block) | todo | M | T5.10, T6.1 | REQ-M8c, BHV-STATS-8, BHV-STATS-9, BHV-STATS-10, BHV-STATS-12, BHV-STATS-19, BHV-STATS-20 |
| T6.8 | Test data: goal history (changed, replaced, archived with the habit) in the sample file | todo | S | T5.11, T6.6 | - |

**T6.2, T6.3:** the hardest logic of the project (about 30 BHV-GOAL scenarios); pure
Domain, tested by the scenarios before the UI tasks.

## M7 - Binary and duration habits
**Goal:** Binary habits and duration habits with manual entries (crossing midnight,
the clock changes), in both directions, on all screens.
**Manual test:** Every habit kind and direction works on the Day, Habits and
Statistics screens.

| ID    | Task | Status | Effort | Depends on | Covers |
|-------|------|--------|--------|------------|--------|
| T7.1  | Domain: Binary entries (mark and unmark, at most one per day, no Undo for unmarking on the Day row, delete from the sheet with Undo), value of a day | todo | M | T2.5, T3.1 | REQ-M1, REQ-M5, BHV-ENTRY-1, BHV-ENTRY-13, BHV-ENTRY-22c, BHV-ENTRY-22d |
| T7.2  | Domain: manual Duration entries (validation of the length and "not after now"), the split across days (`docs/DESIGN.md` section 5.3), value of a day, a time zone change | todo | L | T2.5, T3.1 | REQ-M1, REQ-M4, REQ-M5, BHV-ENTRY-R2, BHV-ENTRY-R4, BHV-ENTRY-R5, BHV-ENTRY-R7, BHV-ENTRY-3, BHV-ENTRY-5, BHV-ENTRY-6, BHV-ENTRY-7, BHV-ENTRY-8, BHV-ENTRY-15, BHV-ENTRY-19 |
| T7.3  | Domain: times of Duration entries (`docs/DESIGN.md` section 5.8): kept seconds, the offset, clock changes (gap and repeated hour), quick buttons move the instant | todo | L | T7.2 | REQ-M5, BHV-ENTRY-R6b, BHV-ENTRY-23a, BHV-ENTRY-23b, BHV-ENTRY-23c, BHV-ENTRY-23d, BHV-ENTRY-24, BHV-ENTRY-25, BHV-ENTRY-26 |
| T7.4  | Domain: goals of Binary and Duration (the validation moved from M5, the evaluation of Duration, the indicator); Presentation: the numbers of the Duration quit ring (rounding) | todo | M | T5.6, T7.1, T7.2 | REQ-M2, REQ-M3, REQ-SC3, BHV-GOALVAL-1a, BHV-GOALVAL-1b, BHV-GOALVAL-2, BHV-GOALVAL-3a, BHV-GOALVAL-3b, BHV-GOALVAL-7, BHV-IMPORT-2b, BHV-DAY-10a, BHV-DAY-10b, BHV-DAY-11, BHV-DAY-12a, BHV-DAY-12b |
| T7.5  | Domain: statistics figures for Binary (occurrences) and Duration (total time, average per day) | todo | M | T4.5, T7.1, T7.2 | REQ-M8a, REQ-M8b, BHV-STATS-15, BHV-STATS-16, BHV-STATS-23 |
| T7.6  | UI: habit form with the type choice Binary / Duration; goal target in days and in h / min / s | todo | M | T6.5, T7.4 | REQ-M1, REQ-M3 |
| T7.7  | UI: Day row: checkbox (build) and the chip "It happened" (quit), the manual Duration entry button (no timer), value texts, the quit ring in the compact format | todo | M | T5.9, T7.4 | REQ-M1, REQ-M2, REQ-M6 |
| T7.8  | UI: Duration entry form (Start / End with date and time, quick buttons, length, the disabled OK of the time picker, validation); Binary and Duration entries in the sheet | todo | L | T2.12, T7.3 | REQ-M4, REQ-M5 |
| T7.9  | UI: Statistics cards for Binary and Duration | todo | S | T6.7, T7.5 | REQ-M8a, REQ-M8b |
| T7.10 | Test data: Binary and Duration habits in both directions, entries across midnight and on the days of the clock changes | todo | S | T6.8, T7.8 | - |

**T7.2, T7.7:** no timer in M7; the Duration row has only the manual entry, start and
stop come in M8.

## M8 - Timer and notification
**Goal:** The timer (start, stop, discard with Undo, surviving the closing of the app),
the ongoing notification with Stop, the permission and the notification sync.
**Manual test:** Run a timer with the app closed, stop it from the notification,
refuse the permission, dismiss the notification and reopen the app.

| ID   | Task | Status | Effort | Depends on | Covers |
|------|------|--------|--------|------------|--------|
| T8.1 | Domain: timer start, stop, discard (BHV-TIMER-R1, R2: rounding to the second, a length not above 0), a running timer in the value of a day, the split across days and the statistics (up to now), the undo of a discarded timer and its withdrawal (`docs/DESIGN.md` section 5.7) | todo | L | T7.2, T7.5 | REQ-M4, BHV-TIMER-R1, BHV-TIMER-R2, BHV-TIMER-1, BHV-TIMER-2, BHV-TIMER-3, BHV-TIMER-7, BHV-TIMER-8, BHV-TIMER-9a, BHV-TIMER-9b, BHV-TIMER-9c, BHV-TIMER-10a, BHV-TIMER-10b, BHV-TIMER-10c, BHV-TIMER-11a, BHV-TIMER-11b, BHV-DAY-3, BHV-STATS-14 |
| T8.2 | Domain: the timer on archiving and deleting a habit (BHV-TIMER-R3, the part moved from T4.2), the result for UI-MSG-TIMER-ARCHIVED | todo | M | T6.4, T8.1 | REQ-M4, REQ-M6, BHV-TIMER-R3, BHV-TIMER-4, BHV-TIMER-5, BHV-HABIT-8 |
| T8.3 | Platform services: notification adapter (chronometer, Stop, texts in the app language by the result of T1.4, text change), Stop receiver with `goAsync`, the operation "stop timer from the notification", the "timer not stopped" stream | todo | L | T1.8, T8.1 | REQ-S3, BHV-TIMER-R4, BHV-TIMER-6 |
| T8.4 | Domain: notification sync (`docs/DESIGN.md` section 5.6), called at process start, on the move to the foreground and after an import (BHV-NOTIF-2, moved from M3) | todo | M | T3.5, T8.3 | REQ-S3, BHV-TIMER-R5, BHV-NOTIF-1, BHV-NOTIF-2, BHV-IMPORT-12 |
| T8.5 | Domain and Platform services: permission state and the two device flags (SharedPreferences or DataStore, by T1.1), the permission request flow, the one-time message, also after an import (BHV-NOTIF-3, 4, 5) | todo | M | T8.4 | REQ-S3, BHV-NOTIF-3, BHV-NOTIF-4, BHV-NOTIF-5 |
| T8.6 | UI: Start / Stop with the session time on the Day row, the live timer item in the sheet (Stop, Discard), UI-MSG-UNDO-TIMER, UI-MSG-TIMER-NOT-STOPPED, UI-DIALOG-NOTIFICATIONS, the system permission request | todo | L | T7.7, T7.8, T8.2, T8.5 | REQ-M4, REQ-S3 |
| T8.7 | Measure that "Export and replace" on large test data stays well within the about 10 s of a receiver (moved from M3) | todo | S | T3.5, T8.3 | REQ-S1 |
| T8.8 | Test data: running timers in the sample file | todo | S | T7.10, T8.6 | - |

**T8.1:** a timer surviving the closing of the app and a restart of the phone
(BHV-TIMER-7, 8) follows from the design (the stored start instant) and is checked in
the manual test of M8, not by a separate task.

## M9 - Success criteria and finishing
**Goal:** The replay test of the three-habit week, real database migrations, the app
name, the tuning of the GUI, a signed release APK.
**Manual test:** One week of real use with the three habits of the success criteria
(`docs/REQUIREMENTS.md` section 4).

| ID   | Task | Status | Effort | Depends on | Covers |
|------|------|--------|--------|------------|--------|
| T9.1 | Domain test: replay of the three-habit week (Duration build with a timer and a past-day entry, Numeric build with a daily goal, Binary quit with a limit of 3 days per week) with hand-computed results | todo | M | T8.1, T7.4 | REQ-SC2, REQ-SC3 |
| T9.2 | Real database migrations: freeze the v1 schema, migration tests (ends the "recreate the database" convention) | todo | M | T8.8 | REQ-SC1 |
| T9.3 | App name (question 12, the user's choice) in the PL / EN resources and in the documents | todo | S | T1.2 | - |
| T9.4 | GUI tuning: Day screen and the entries sheet | todo | L | T8.6 | - |
| T9.5 | GUI tuning: Habits screen and the forms | todo | L | T7.6 | - |
| T9.6 | GUI tuning: Statistics screen; review of the Polish texts | todo | M | T7.9 | REQ-C4 |
| T9.7 | One week of real use by the author (final confirmation) | todo | S | T9.1, T9.2, T9.3, T9.4, T9.5, T9.6, T9.8 | REQ-SC1, REQ-SC2, REQ-SC3 |
| T9.8 | Signed release APK for the author and friends who test (no Google Play, REQ-W8), with a fixed signing key kept outside the repo | todo | S | T9.2 | - |

**T9.4-T9.6:** planning principle 4 (function before look). **T9.8:** user decision
2026-11-25: a fixed key, so that updates install over the old version without losing
data; the debug key is local to one machine. The Auto Backup settings are in T1.3, not
in M9.

## Deferred

None yet.
