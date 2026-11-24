# Plan: Habit Tracker (working title)

> **STATUS (2026-11-24): DRAFT, NOT APPROVED.** Level 1 (milestones) confirmed by the
> user; level 2 (tasks) in progress: M1-M3 agreed, M4-M9 not started. Effort,
> dependencies and Covers are proposals until level 3.
> **Session interrupted on 2026-11-24** after the tasks of M3; the next step is the
> tasks of M4. Details: `docs/STATUS.md`.

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
| T1.1 | Check the current versions of the toolchain and libraries in the official docs (AGP, Gradle, Kotlin, Compose BOM, Navigation Compose, lifecycle ViewModel, Room, KSP, kotlinx.serialization, coroutines, JUnit) | todo | S | - | - |
| T1.2 | Create the Gradle project: pure Kotlin/JVM `domain` module, `app` module with Compose and minimum API 33, version catalog, unit tests running in both modules | todo | M | T1.1 | REQ-C3 |
| T1.3 | Disable Auto Backup (manifest and data extraction rules), no INTERNET permission; check question 21 (transfer to iOS) | todo | S | T1.2 | REQ-C1, REQ-C5 |
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
| T2.1  | Domain: numeric habit and entry model, exact decimals, built-in units with default steps, custom unit | todo | M | T1.2 | REQ-M7, REQ-S2 |
| T2.2  | Domain: ports (Habits, Entries, Data operations, Clock), Domain operations skeleton with the lock, hand-written fakes for tests | todo | M | T2.1 | - |
| T2.3  | Domain: create a habit and its validation (name, unit, "+" step) | todo | S | T2.2 | REQ-M6 |
| T2.4  | Domain: numeric entries (add with "+" and typed, edit, delete), validation, default moment, value of a day | todo | L | T2.2 | REQ-M5 |
| T2.5  | Domain: undo offer of a deleted entry, its expiry and withdrawal | todo | M | T2.4 | - |
| T2.6  | Domain: which habits are shown and their order (`Collator`) | todo | S | T2.3 | - |
| T2.7  | Persistence: Room with habits and entries, entries read by date range, instrumented tests (G8 included) | todo | L | T2.2 | REQ-C2 |
| T2.8  | Platform services: Clock adapter (now, time zone, ticks, day and time zone change) | todo | M | T2.2 | - |
| T2.9  | App: composition root, App shell with the Undo snackbar, bottom navigation bar | todo | M | T2.7, T2.8 | - |
| T2.10 | Habits screen, minimal: list and the form of a new numeric build habit | todo | M | T2.3, T2.9 | REQ-M6 |
| T2.11 | Day screen: header (arrows, calendar picker, date follows today), rows with "+", typed value and the value of the day | todo | L | T1.8, T2.4, T2.6, T2.9 | REQ-M5, REQ-M6 |
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
| T3.2 | Persistence: Room schema of the full model; Data operations: export snapshot, *Replace all*, *Add to existing* | todo | L | T2.7, T3.1 | REQ-S1 |
| T3.3 | Platform services: Backup file adapter (file-model classes with kotlinx.serialization, two-step read with a streaming header, a write that reports success only for the whole file) | todo | L | T3.1 | REQ-S1 |
| T3.4 | Domain: format rules (version check, BHV-IMPORT-R4 without goal validity, unique identities, the import summary) | todo | M | T3.1 | REQ-S1 |
| T3.5 | Domain: operations export, import in both modes (new identities on *Add to existing*) and "Export and replace" under one lock | todo | M | T3.2, T3.3, T3.4 | REQ-S1 |
| T3.6 | UI: Export / Import menu on the Habits screen, system save and open dialogs, import summary, mode choice, the *Replace all* dialog with "Export and replace", the rejection dialog, messages | todo | L | T2.10, T3.5 | REQ-S1 |
| T3.7 | Tests: round trip, sample files of format version 1 kept as fixtures, a file cut off after its header, a photo | todo | M | T3.4, T3.5 | REQ-SC1 |
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

## M5 - Goals and the quit direction
**Goal:** Creating goals (periods day to year, repeating or for N periods), the
evaluation of a period, the progress indicator on the Day screen, the goal blocks in
statistics, quit habits with limits.
**Manual test:** Set a goal and a limit, log entries and check the percentage, the
colours and the statistics against a manual calculation.

## M6 - Goal changes and history
**Goal:** Changing goals (target, period, first day, N), replacing, archived goals,
restoring a habit with or without its goal.
**Manual test:** Change a goal in the middle of a period and check the Day screen and
the statistics against the rules.

## M7 - Binary and duration habits
**Goal:** Binary habits and duration habits with manual entries (crossing midnight,
the clock changes), in both directions, on all screens.
**Manual test:** Every habit kind and direction works on the Day, Habits and
Statistics screens.

## M8 - Timer and notification
**Goal:** The timer (start, stop, discard with Undo, surviving the closing of the app),
the ongoing notification with Stop, the permission and the notification sync.
**Manual test:** Run a timer with the app closed, stop it from the notification,
refuse the permission, dismiss the notification and reopen the app.

## M9 - Success criteria and finishing
**Goal:** The replay test of the three-habit week, the app name, the Auto Backup
settings, the tuning of the GUI.
**Manual test:** One week of real use with the three habits of the success criteria
(`docs/REQUIREMENTS.md` section 4).

## Deferred

None yet.
