# Design: Habit Tracker (working title)

> **DESIGN PAUSED (2026-11-13).** On 2026-11-13 the user decided to move the
> user-visible behavior (scenarios, goal rules, statistics rules, import) into
> `docs/REQUIREMENTS.md` section 7 and to change the goal model (a goal is only a
> definition; changing it archives it; statistics per goal). This document is
> therefore partly outdated (sections 4, 5, 6 and 8, Decisions 15, 31, 42, 52, 62,
> 67-69). Do not work on it until the requirements are reviewed and approved; then
> triage it as described in the status of `docs/REQUIREMENTS.md`. The status
> below dates from 2026-11-11.
>
> **STATUS: SESSION INTERRUPTED, DOCUMENT NOT APPROVED (2026-11-11).**
> The user had to end the session during the review of the revised design.
> The user agreed to each change below in conversation, but has not approved
> the document as a whole. The decisions log is `docs/DESIGN-DECISIONS.md`
> (Decisions 52-72 are new in this revision).
>
> **Agreed and applied in this session** (Decisions 68-72 and the text of
> sections 3-7): the form lists all goals valid today or pending and the user
> edits the one tapped; after *Wait* the new goal starts by default at the end
> of the old one (but see B1 below); mode and N are derived (mode = has an
> end), changing the end needs no choice; manual Duration entry start <= now;
> Android-created objects get dependencies through interfaces implemented by
> Application; single writes in the Entries and Running timers ports; goals
> counted only if they contain a day of the habit's life; no average per day
> without days of life; overlap read of entries; full import validation
> (scenarios J1-J5); different symbol for Binary quit; indicator marks for
> abandoned and cut-off periods; R-VALID-HABIT; "days left". Outdated
> decisions are replaced in place, not kept as history.
>
> **Still open** (report `docs/reviews/2026-11-11-docs-review-3.md`, all
> findings open, none applied): B1 (start after *Wait* when the period length
> changes), I1 (two goals without an end), I2-I7 and M1-M10, structure
> proposals. The questions P1-P4 in the report await the user's decision.
>
> **To resume:** decide P1-P4, go through I4-I7 (the user decides each edge
> case of I7), apply the minor findings, then run docs-reviewer again and ask
> the user for approval. The plan phase must not start before that.
>
> **Still to do before approval:** resolve or defer the open findings of the
> latest docs-reviewer report, get the user's approval. The plan phase must
> not start before that. Open questions that remain are carried to the plan
> (section 10).

## Summary and map

- **What:** Android app (Kotlin, Jetpack Compose, minimum API 33), fully
  offline, local Room storage, no analytics (section 2).
- **Architecture (section 3):** `GUI -> Presentation -> Domain <-
  Persistence`, `Domain <- Platform services`; App wires everything. Domain
  is a pure Kotlin/JVM Gradle module and owns the ports.
- **Data (section 4):** Habit (name, direction Build / Quit, creation day, archiving day) has
  one Measurement (Binary, Numeric with unit, or Duration), optional Goals
  (separate entities: target, period day / week / month / quarter / year,
  validity from / to), Entries (a moment in
  time and a value; Binary: a date) and at most one RunningTimer (a start instant).
- **Rules (section 5, tagged `R-...`):** week starts Monday; a day is a
  calendar day in the device time zone; period result, "achieved" and the
  completion percentage per type and direction; one goal runs at a time in
  v1, and changing it offers three options (abandon, delete the unfinished
  period, wait); a new goal starts at the current period of its length (after
  "wait", at the end of the old goal) unless the user picks another date; statistics give one line per period length,
  count the unfinished period with its result so far (marked provisional)
  and only periods that start inside the range and not after today;
  time-dependent screens refresh through the Clock port. The goal and
  statistics rules are specified by the scenario table at the end of section 5.
- **Screens (section 6):** three tabs: Day (selected date, one row per
  habit, entries edited in a bottom sheet), Habits (one create / edit
  form), Statistics (common date range, one card per habit).
- **Cross-cutting (section 7):** Polish and English via text identifiers;
  versioned JSON export / import that replaces all data; Auto Backup off.
- **Testing (section 8):** Domain and Presentation as JVM tests with fakes;
  the success criteria are checked by a replay test of the three-habit week.
- **Where things are:** decisions in `docs/DESIGN-DECISIONS.md` (cited as
  "Decision N"); requirements in `docs/REQUIREMENTS.md` (IDs `REQ-...`);
  rules in section 5 (IDs `R-...`); open questions in section 10; diagram in
  `docs/diagrams/components.drawio`.

## 1. Overview

Source: `docs/REQUIREMENTS.md`. Design topics confirmed by the user (derived
from Must/Should features and constraints):

1. Habit model: measurement type and direction.
2. Entries: any day, editing past days.
3. Timer: survives closing the app; ongoing notification with Stop (Should).
4. Goals and periods: fixed day/week/month/quarter/year, repeating or for a
   number of periods, evaluation per type and direction, goal completion percentage.
5. Statistics over a user-chosen date range.
6. Habit management: create, edit, archive, delete.
7. Units: built-in and custom label (Should).
8. Screens and navigation: today screen, day switching, calendar picker,
   habit management, statistics.
9. Export / import to a file (Should).
10. Local, offline-only storage.
11. Localization: Polish and English, texts in resources.
12. Platform: Android only, minimum version.
13. No analytics or crash reporting (constrains library choices).

Always part of the design: architecture with a component diagram, and the
testing strategy (including verification of the success criteria).

## 2. Tech stack

Decided:
- Android, Kotlin (CLAUDE.md and requirements).
- GUI toolkit: Jetpack Compose (Decision 8).
- A thin `ViewModel` from androidx (lifecycle library) in the GUI component,
  only to keep a Presentation object alive across configuration changes
  such as rotation (Decision 9). The exact artifact must be checked in the
  official docs before it is named in the plan.

- Minimum Android version: 13 (API 33) (Decision 34). `java.time` is
  available natively; the notification permission (`POST_NOTIFICATIONS`)
  is always required, so there is one code path. Lowering it later
  (e.g. for a Google Play release) would mostly affect Platform services.

- Local storage: Room (androidx), only inside Persistence; stored classes
  are separate from domain entities and mapped (Decision 35).

- JSON serialization for export / import: kotlinx.serialization, in the
  Platform services adapter only, with file-model classes separate from
  Domain (Decision 36).
- Dependency injection: manually, by constructor, in App (Decision 36).
- Navigation: Navigation Compose (Decision 36).

To check in the official docs in the plan phase: versions of all libraries
and the minimum versions required by Compose and androidx.

## 3. Architecture

Level 1, confirmed by the user. Diagram:
`docs/diagrams/components.drawio` (confirmed by the user). It shows App on
the left with dashed arrows (creates / wires) to every component, and solid
arrows for code dependencies.

**Module structure (Decision 1):** one Gradle module for Domain (pure
Kotlin/JVM, no Android) and one `app` module for everything else, separated
by packages.

**Components:**

| Component | Responsibility |
|---|---|
| Domain | Entities and business rules: periods, goal evaluation, statistics, timer logic (pure state based on a start instant), export/import rules. Defines the interfaces (ports) for storage, clock, etc. No framework dependencies. |
| Persistence | Implements the storage ports defined by Domain. Has its own stored representation and maps it to domain entities. |
| Platform services | Adapters for system features: timer notification (no background service, Decision 21), system clock, file access and JSON serialization for export/import (Decisions 30, 36). |
| Presentation | Screen state (everything computed; texts as identifiers and raw values in simple types or types owned by Presentation, never Domain types, resolved and formatted by the GUI, Decisions 22 and 61) and handling of user actions on a screen (calls Domain, updates state, decides navigation). Pure Kotlin, no Android and no widgets. No business rules. Depends on Domain. |
| GUI | Compose widgets and navigation; renders state from Presentation and forwards user actions to it, without making decisions. Contains the thin androidx `ViewModel` holders. Replaceable without changes to Domain and Presentation. |
| App (composition root) | Wires everything: creates the port implementations and connects them to Domain and Presentation. |

**Dependency direction:** `GUI -> Presentation -> Domain <- Persistence`, and
`Domain <- Platform services`. Domain depends on nothing. App knows all
components; no component knows App.

**Objects created by Android (Decision 71):** Android creates the Activity
and the timer BroadcastReceiver, so they cannot get dependencies through a
constructor. Each of the two components that contain them (GUI, Platform
services) defines a small interface listing what it needs (for GUI, the
factories of Presentation objects; for the receiver, the Data operations and
Timer notification ports). The Application class in App implements both
interfaces, and the Activity and the receiver reach it by casting the
application context to the interface. The dependency still points from App to
the components, never the other way.

**How the design principles are met:**
1. Business logic free of frameworks: Domain is a separate pure Kotlin/JVM
   module; storage and platform features are reached through ports defined
   by Domain and implemented in Persistence and Platform services.
2. Business logic separated from the GUI: GUI depends on Presentation only
   (state in, actions out; the state contains no Domain types, Decision 61);
   Presentation and Domain know nothing about Compose. Replacing the GUI does
   not touch them.
3. Patterns: see section 9 (MVVM with unidirectional state, Ports and
   Adapters, Repository, sealed hierarchy, manual dependency injection).

**Ports (Decisions 48, 59):** interfaces defined by Domain, implemented outside it.

| Port | Implemented by | Purpose |
|---|---|---|
| Habits | Persistence | Reads of habits, and restoring an archived habit. All other writes of habits are Data operations. |
| Entries | Persistence | Single-entry writes: add, edit, delete (also the Binary toggle). Entries of a habit, read by date range (never "all entries", Decision 54). A read returns every entry whose time span overlaps the range (calendar days in the device time zone), not only entries that start in it (scenario G8). Also the date of the habit's earliest entry (for R-STATS-HABIT-LIFE). Export reads from that date to today, range by range. |
| Goals | Persistence | Goals of a habit with their validity (Decision 52). Reads only: every goal write is part of "save habit". |
| Running timers | Persistence | Start instants of running timers (the source of truth, Decision 18). Single writes: start a timer, discard it without an entry. Stopping is a Data operation. |
| Data operations | Persistence | Composite operations that touch several of the ports above, each all-or-nothing (Decision 59). |
| Clock | Platform services | Current instant and device time zone; a tick stream that ticks while it has a subscriber (Presentation subscribes only while a timer runs); signals of a day or time zone change (R-TIME-REFRESH, Decision 58). |
| Timer notification | Platform services | Show and remove the notification with Stop; synchronize notifications with running timers (R-NOTIFICATION-SYNC). |
| Backup file | Platform services | Read and write the export file, which is identified by an opaque identifier (Decision 60): JSON parsing and serialization, mapping between file classes and domain entities with the format version (section 7). |

Rules for all storage ports:
- **Reads return `Flow`, writes are `suspend`.** Screens refresh by themselves
  when data change. Domain therefore depends on `kotlinx.coroutines`, and on
  nothing else outside the Kotlin standard library. Passing time is not a
  data change: it reaches screens through the Clock port (R-TIME-REFRESH).
- **Composite operations are the methods of the Data operations port
  (Decision 59), named after business actions; each runs in one database
  transaction.** They are all-or-nothing, so a crash in the middle leaves
  the previous state: for the timer, it keeps running and Stop can be
  tapped again. The operations are:
  - stop timer (create an entry and delete the running timer),
  - archive habit (stop its timer as above and set the archiving day),
  - delete habit (the habit with its entries, goals and running timer),
  - import (replace all data),
  - save habit (the habit and its goal changes from the form: create, edit,
    and the end, removal or creation of goals by R-GOAL-CHANGE, all in one
    operation).

  **Domain computes the resulting entities** (the goals after a change,
  the length of a stopped timer, the validations); a Data operations method
  only writes them in one transaction. Domain does not know the term
  "transaction". Timer notifications are not
  part of the transaction: Domain updates them through the Timer
  notification port after the operation, and a mismatch left by a crash is
  repaired by R-NOTIFICATION-SYNC.

**Note on iOS:** pure Kotlin Presentation and Domain make a later move
easier, but sharing them with iOS would need Kotlin Multiplatform and a
change of the Domain module type. Not designed for in v1 (out of scope);
the choices only do not rule it out.

## 4. Data model

Level 1 (confirmed on 2026-11-08, Goal revised on 2026-11-11, Decision 52): names and relations only; attributes and invariants are Level 2.

- **Habit**: a pure definition of the activity. Identity, name, direction
  (Build / Quit), creation day, archiving day (empty while the habit is
  not archived; Decision 43). Direction is the habit's
  intent and exists even without a goal. It is not part of the goal.
- **Measurement**: how the habit is measured. A closed (`sealed`) set of
  variants: Binary, Numeric (with a unit), Duration. Exactly one per habit.
  The unit belongs to the Numeric variant, not to the Habit. A unit is
  data, not a type: either a built-in one (identifier, name from string
  resources) or a custom one (text label). Details are Level 2.
- **Goal**: optional (a habit may exist without any goal). A separate
  entity, so changing a goal does not change the habit definition: a
  reference to the habit, a target value, a period (day / week / month /
  quarter / year), the first day of its validity and an optional end of its
  validity (Decision 52). No goal versions and no series identifier. The
  comparison ("at least" / "at most") comes from the habit's direction, not
  from the goal. The period is a separate attribute so a goal without a
  period can be added later (see Decision 7).
- **Entry**: a recorded value for a habit at a moment in time (date and
  time, not only a day; a Binary entry holds a date, Decision 44). Its
  meaning comes from the habit's Measurement.
- **RunningTimer**: a habit plus the instant the timer was started.

Relations: Habit 1 - 1 Measurement; Habit 1 - 0..n Goal (one runs at a time
in v1; see INV-ONE-OPEN-GOAL and INV-GOAL-NO-SHARED-PERIOD); Habit 1 - 0..n Entry; Habit 1 - 0..1 RunningTimer
(several timers may run at once, at most one per habit; Decision 18).

Level 2 (business-relevant attributes and invariants; invariants and validations have IDs):
- **Habit** (INV-DIRECTION-IMMUTABLE): direction is immutable after creation. Name is editable.
  Archived habits keep entries and statistics; deleting removes the habit
  with its entries and goals (Decision 17). Archiving sets the archiving
  day; restoring clears it.
- **Measurement** (INV-TYPE-LOCKED): the type cannot change once the habit has its first
  entry. For Numeric the unit is a label on the habit; entries hold plain
  numbers. Changing the unit relabels all values, without conversion
  (Decision 17). Numeric also has an optional step: the amount added by
  one tap of "+" on the Day screen. Its default depends on the unit (1 for
  a custom unit). The step does not change how entries are summed
  (Decision 25).
- **Entry**:
  - Duration: a start instant and a length. It can span midnight; the
    split per day is computed by Domain, not stored (Decision 12). Several
    per day.
  - Numeric: a value; several per day, summed.
  - Binary: stores a local date, not an instant (Decision 44); at most one
    per day (INV-BINARY-ONE-PER-DAY); presence means "occurred", absence means "did not
    occur" (Decision 14).
  - The day of a Duration or Numeric entry is a calendar day in the device
    time zone (Decision 13).
- **Goal** (Decision 52):
  - INV-ONE-OPEN-GOAL: at most one goal per habit has no end of validity.
  - INV-GOAL-NO-SHARED-PERIOD: two goals of one habit with the same period
    length never contain days of the same period (so one line of
    statistics never counts a period twice). Goals of different period
    lengths may overlap in time.
  - INV-GOAL-BOUNDARIES: the first day lies on the first day of a period of
    the goal's own length, and the end of validity is after the first day (an
    abandoned goal may end inside a period, R-GOAL-VALIDITY).
  - Mode and N (Decision 69) are not stored: the *mode* is whether the goal
    has an end ("repeat" = no end, "N periods" = an end), and *N* is the
    number of periods from the first day to the end. A goal whose end is not
    on a period boundary was abandoned; it has no N and the form shows it
    as "ended on X", read-only.
  - All three are checked in Domain, also on import (section 7), and are v1
    invariants, so parallel goals can be allowed later (Decision 67).
  - The first day and the end of validity are calendar days, so no instant
    or time zone is involved. How a goal starts, repeats, changes and ends is
    defined by R-GOAL-VALIDITY and R-GOAL-CHANGE in section 5. For the user, changing a
  goal is just editing it on the same habit (same identity, name and entries;
  no new habit is created, Decision 28); the end of the old goal and the
  start of the new one are internal.
- **Validation (Decisions 51, 65):**
  - **R-VALID-GOAL:** the target is Build > 0; Quit >= 0 (a limit of 0 is
    allowed). A Binary target is a whole number of days; it cannot exceed the
    largest number of days its period can have (1 for a day, 7 for a week, 31
    for a month, 92 for a quarter, 366 for a year). A target above the length
    of a shorter period (e.g. 31 in February) is unreachable in that period;
    this is accepted in v1 (Decision 65). A Duration target has a precision
    of one second, is entered as hours, minutes and seconds (no days), and a
    daily goal cannot exceed 24 h. The number of periods of a goal for N
    periods is a whole number >= 1 (Decision 53).
  - **R-VALID-ENTRY:** a manual Duration entry is entered as a start and an
    end (date and time); its length is their difference, with a precision of
    one second, and must be > 0 (the end is after the start). The end may
    lie in the future (Decision 63); the app may show an informational
    notice, but does not block it. The start cannot lie in the future
    (start <= now, Decision 70), so a range read never misses an entry. A
    Numeric value is > 0 (checked after rounding to 3 decimal places,
    R-EXACT-VALUES). Entries cannot be negative.
  - **R-VALID-HABIT:** the name of a habit is not empty after trimming
    white space. Duplicate names are allowed; habits with equal names are
    ordered by creation day (Decision 72).
  - **R-VALID-TIMER-STOP:** stopping a timer whose computed length is not
    positive (the device clock was moved back before the start instant)
    creates no entry. The app shows a message and the timer keeps running.

**Built-in units (Decision 37):** each has an identifier, a name from
string resources (PL / EN) and a default "+" step. The identifiers and
steps are business data and live in Domain; the names live in resources.
A custom unit is a text label with a step of 1. The step can be changed
per habit (Decision 25). Duration is not a unit (it is its own type).

| Unit | Default step |
|---|---|
| ml | 250 |
| l | 0.25 |
| glasses | 1 |
| g | 100 |
| kg | 0.5 |
| km | 1 |
| steps | 1000 |
| kcal | 100 |
| pages | 1 |
| repetitions | 1 |
| count | 1 |

ml and l are separate units with no conversion (conversion is in "Ideas
for later").

## 5. Behavior and rules

Rules are grouped by topic; the IDs are stable. The scenario table at the end
of this section specifies the goal and statistics rules by examples.
Validations and invariants (`R-VALID-*`, `INV-*`) are in section 4.

**Rule index**

| ID | Rule in one sentence | Decisions | Scenarios |
|---|---|---|---|
| R-WEEK-START | The week starts on Monday; the first day is a parameter. | 11 | |
| R-PERIODS | Goal periods are calendar day, week, month, quarter, year. | 53 | |
| R-GOAL-VALIDITY | A goal is valid from the first day of a period, repeating or for N periods. | 52, 53, 67 | B8, B9, D1-D3 |
| R-GOAL-CHANGE | One goal at a time; changing it offers abandon, delete the unfinished period, wait. | 15, 52, 67, 68, 69 | B1-B14 |
| R-PERIOD-RESULT | Result of a period per measurement type. | 16 | A1-A6 |
| R-ACHIEVED | Build: result >= target. Quit: result <= target. | 16 | A1-A6 |
| R-PERCENT | Completion percentage, capped, truncated for display. | 16, 64 | A6 |
| R-EXACT-VALUES | Exact decimals, three places, no floating point. | 46, 64 | A6 |
| R-DAY | A day is a calendar day in the device time zone. | 12, 13, 63 | G1, G2, G5 |
| R-ENTRY-DATE | Entries apply to the selected day; no future dates; times of entries. | 40, 44, 50 | G6 |
| R-DAY-VALUE | Value of a day, including the running timer. | 45, 56 | G1, G4 |
| R-INDICATOR-PERIOD | The indicator shows the period of the selected date; abandoned and cut-off periods. | 41, 50 | |
| R-TIME-REFRESH | Passing time reaches screens through the Clock port. | 58 | |
| R-TIMERS | Several timers, one per habit; a timer is a start instant. | 18 | G1-G4 |
| R-TIMER-ANDROID | No background service; one notification per timer with Stop. | 21, 32 | |
| R-TIMER-HABIT-CHANGE | Archiving stops a timer, deleting discards it. | 51 | G7 |
| R-TIMER-PERMISSION | Notification permission at the first timer start; dismissal. | 47, 57 | H1, H3 |
| R-NOTIFICATION-SYNC | Notifications follow running timers; one idempotent operation. | 57 | H1, H2 |
| R-STATS-UNFINISHED | The unfinished period counts, marked provisional; days left. | 41 | E1, E2 |
| R-STATS-RANGE | Which goal periods count in a range. | 20, 41, 55 | E1-E7, E11 |
| R-STATS-GOALS | One statistics line per period length; abandoned periods. | 52, 62, 67 | E8-E10 |
| R-STATS-HABIT-LIFE | Statistics follow the life of the habit; cut-off period. | 43 | F1, F2, E11 |
| R-VALID-GOAL | Limits of a goal target and of the number of periods. | 51, 53, 65 | I1-I3, J2 |
| R-VALID-ENTRY | Manual Duration entry: start <= now, length > 0; Numeric value > 0. | 51, 63, 70 | J2 |
| R-VALID-TIMER-STOP | A timer with a non-positive length creates no entry. | 51 | G3 |
| R-VALID-HABIT | A habit name is not empty (duplicates allowed). | 72 | |
| INV-* | Invariants of Habit, Measurement, Entry and Goal. | 14, 17, 44, 52, 67 | J1-J5 |

The rules R-VALID-*, INV-* are defined in section 4, not in 5.

### 5.1 Periods and goals

- **R-WEEK-START: Week start:** Monday. The first day of the week is a parameter of the
  period calculation, not a constant hard-coded in the rules, so a user
  setting can be added later without changing the rules (Decision 11).
  Day and month boundaries need no decision.

- **R-PERIODS: Goal periods (Decision 53):** calendar periods in the device
  time zone: a day; a week (Monday to Sunday, R-WEEK-START); a month; a
  quarter (January-March, April-June, July-September, October-December); a
  year (a calendar year).

- **R-GOAL-VALIDITY: Goal validity and repeating (Decisions 52, 53, 67):** a
  goal is valid from its first day up to its end of validity (exclusive), or
  without an end. The first day is always the first day of a period of the
  goal's own length: the user picks a date, and the goal starts at the
  beginning of the period that contains it. The default is the current period
  of the goal's length (today, this week, this month, this quarter, this
  year); an earlier date (to count retrospectively) or a later one (a
  pending goal) is allowed. The GUI must show the calendar period that the
  chosen date selects (e.g. "Week 9-15 Nov 2026"), so the user sees that goals
  follow the calendar. Mode "repeat" (the default): no end; the goal applies
  to every period from its first day, so periods repeat automatically (they
  are derived, not stored). Mode "N periods": the end is the first day of
  the period after the N-th period, counted from the period that contains
  the first day (e.g. 30 days, or 8 weeks); the goal ends there and nothing
  repeats. The periods of a goal are the periods that contain a day of its
  validity. They are evaluated whole, so a weekly goal created on a Tuesday
  also counts that week's Monday (scenario D1). The only validity that ends
  inside a period is an abandoned goal (R-GOAL-CHANGE); its last period is
  "abandoned". Invariants: INV-ONE-OPEN-GOAL, INV-GOAL-NO-SHARED-PERIOD.

- **R-GOAL-CHANGE: Goal change (Decisions 15, 52, 67):** v1 runs one goal at
  a time (parallel goals are in "Ideas for later"). The form lists every
  goal that is valid today or pending (Decision 68); the user edits the one
  they tap. Goals that have already ended are not listed. If there is
  none, the goal section is off, and enabling it creates a new goal; ended
  goals are never deleted by the app. Saving the form changes goals only
  when the target, the period or the end (mode, number of periods) changed
  (renaming the habit touches no goal).
  - **Change that needs a choice.** Changing the target or the period, or
    switching the goal off, asks the user to choose one of three
    options (none preselected):
    - *Abandon* (stop tracking): the goal's validity ends after today. Its
      completed periods stay; its current period is "abandoned" (unless
      today is its last day) and entries after today do not affect it.
    - *Delete the unfinished period*: the goal ends on the first day of its
      current period; completed periods stay. A goal with no completed period
      is removed (no empty record is left).
    - *Wait*: the goal finishes its current period and does not repeat
      further (in N-periods mode the end is the earlier of the existing end
      and the end of the current period); the new goal starts after it
      (Decision 68: by default exactly at that end, also when the period
      length changes).
    Switching off offers the same three options without a new goal.
  - **Start of the new goal.** R-GOAL-VALIDITY applies (default: the current
    period of its length; after *Wait* the end of the old goal; the user may
    pick another date). The date is
    limited by INV-GOAL-NO-SHARED-PERIOD: it cannot select a period that
    contains a day of a goal of the same length, so with the same length the
    default is the current period after *Delete* and the next period after
    *Abandon* and *Wait*. Goals of different lengths may overlap; their
    common days are counted in each of their lines (R-STATS-GOALS). The GUI
    offers only valid dates.
  - **Pending goal.** A goal that has not started yet has no history: it is
    edited in place or deleted, with no choice.
  - **End alone (Decision 69).** Changing only N, or switching between
    "repeat" and "N periods", moves or removes the end of validity, with no
    choice. N cannot be below the number of periods already started, and an
    end that would make the goal share a period with another goal of the same
    length (INV-GOAL-NO-SHARED-PERIOD) is not offered.
  - Periods of the old goal before its end keep their goal and result.
    Percentage and "achieved" are always computed, never stored.
    Examples: scenarios B1-B15.

- **R-PERIOD-RESULT: Period result (Decision 16):** Duration: total length attributed to the
  days of the period. Numeric: sum of entry values. Binary: number of days
  with an entry.

- **R-ACHIEVED: Achieved (Decision 16):** Build: result >= target. Quit: result <=
  target (the target may be 0).

- **R-PERCENT: Completion percentage (Decisions 16, 64):** Build: result / target, capped at
  100% (the cap is a parameter in Domain, always on in v1). Quit: 100% when
  the limit is kept, otherwise target / result (0% when the target is 0
  and the result is above 0; limit 3 and result 5 give 60%). A period is
  achieved exactly when its capped percentage is 100%. For display the
  percentage is truncated down to a whole percent and 100% is shown only
  for an achieved period (99.6% is shown as 99%).

- **R-EXACT-VALUES: Exact values (Decisions 46, 64):** values of Numeric entries,
  targets and sums are exact decimals with a fixed precision of 3 decimal
  places; input with more places is rounded. Comparison with the target
  uses exact arithmetic, never floating point, and never a rounded
  percentage (R-ACHIEVED). A percentage is an exact fraction (1/3 has no
  finite decimal expansion); it is truncated only for display, and the
  average completion percentage is computed from the exact fractions and
  then truncated down for display. The representation (`BigDecimal`, an
  integer in thousandths or a fraction type) is chosen in the plan.

### 5.2 Day and entries

- **R-DAY: Day boundary (Decision 13):** a day is a calendar day in the device
  time zone. The time zone is a parameter of the calculation, supplied via
  a clock port, like the week start. A duration entry that crosses midnight
  contributes to each day the part of its length that falls in that day
  (Decision 12). A manual duration entry is entered as a start and an end
  (date and time, R-VALID-ENTRY). The default start is 12:00 of the chosen
  day when the day is not today, and the current moment when it is today
  (like a Numeric entry, R-ENTRY-DATE; Decision 63); the default end is the
  start plus one hour; the user changes both. If the device time zone
  changes, days of Duration and Numeric entries are recomputed from the
  stored instants (accepted for v1); Binary entries keep their stored date
  (Decision 44).

- **R-ENTRY-DATE: Entries and dates (Decisions 40, 44, 50):** entries apply to
  the selected day, also a past one; future dates are not possible in v1. A
  Binary entry on a past date stores that date. The timer works only on
  today's date; on any other date a Duration habit offers manual entry only.
  A Numeric entry (added with "+" or a typed value) gets the current moment
  on today's date and the fixed time 12:00 on any other date, so a time zone
  change does not move it to a neighboring day. The default start of a
  manual Duration entry is in R-DAY.

- **R-DAY-VALUE: Value of a day (Decisions 45, 56):** the value of a habit on
  a date is the total of its entries on that day plus, for Duration, the
  part of a running timer that falls on that day (up to the current
  instant). It is the value of the day, not the result of the goal period.
  For Binary it is occurred / did not occur. The session time of a running
  timer (now - start) is a separate figure, shown next to Stop and on the
  timer's item in the entries sheet.

- **R-INDICATOR-PERIOD: Period of the progress indicator (Decisions 41, 50):**
  the indicator of the Day screen shows the completion percentage of the
  goal period that contains the selected date (not the period counted from
  today); a period that is not finished is provisional (its result so far).
  If several goals contain the selected date (goals of different period
  lengths may overlap), the newest one (the latest first day; on equal first
  days the one with the shorter period) is used. A habit with no goal containing the date has no
  indicator. For a date in an abandoned period (R-GOAL-CHANGE) the indicator
  shows no percentage but the mark "abandoned"; for a cut-off period
  (R-STATS-HABIT-LIFE) the mark "archived". Statistics lines leave both out
  (R-STATS-GOALS).

- **R-TIME-REFRESH: Passing time (Decision 58):** a `Flow` of data does not
  change when time passes, so time reaches the screens through the Clock
  port. Besides the current instant and the time zone it exposes a tick
  stream (every second, only while it has a subscriber; Presentation
  subscribes only while at least one timer is running) and a signal on a change of day or time zone.
  Presentation recomputes the time-dependent values (R-DAY-VALUE, the
  session time, the indicator) on a tick or signal. The selected date of the
  Day screen is either "today" (it follows the clock, so after midnight it
  moves to the new day) or a concrete date once the user chose one; choosing
  today's date puts it back to "today". A timer running over midnight keeps
  running and its part after midnight belongs to the new day (R-DAY). After
  a time zone change the days are recomputed from the stored instants (R-DAY).

### 5.3 Timer and notifications

- **R-TIMERS: Timers (Decision 18):** several timers may run at once, at most one per
  duration habit. A running timer is just a start instant, so it survives
  closing the app. Stopping it creates a duration entry (start, length). A
  running timer counts toward the result of its habit: its length
  now - start is split across days like an entry (R-DAY), up to the current
  instant, so the result grows live (R-DAY-VALUE, Decisions 45, 56). Statistics
  count it by the same rule. On the entries sheet it is an extra live item
  that can be stopped or discarded; editing its start is not in v1.

- **R-TIMER-ANDROID: Timer vs. Android (Decision 21):** no background service. Domain holds
  the start/stop logic (length = now - stored start instant); Persistence
  holds the start instant; Platform services implements a "timer
  notification" port (show / hide). The notification is an ongoing system
  notification whose built-in chronometer only displays elapsed time from
  the start instant; the app never reads state from it. There is one
  notification per running timer (Decision 32). The Stop button
  in the notification calls the same stop logic as the Stop button in the
  app, through a receiver in Platform services. A later home screen widget
  would be one more adapter on the same ports (out of v1).

- **R-TIMER-HABIT-CHANGE: Timer and habit changes (Decision 51):** archiving
  a habit with a running timer stops the timer with the normal stop logic
  (an entry is created) and removes its notification; if the timer's computed
  length is not positive (R-VALID-TIMER-STOP), it is discarded without an
  entry and the app shows a message. Deleting a habit
  discards the timer without an entry, together with the habit's entries,
  and removes the notification. Both are composite operations of the Data
  operations port (section 3); the notification is updated after them
  (R-NOTIFICATION-SYNC repairs a mismatch). A timer has no maximum length: a
  forgotten timer is corrected by editing the entry in the bottom sheet. A
  reminder about a long-running timer is in "Ideas for later".

- **R-TIMER-PERMISSION: Notification permission and dismissal
  (Decisions 47, 57):** the app asks for the notification permission at the
  first timer start, not at the first launch. After a refusal the timer
  works without a notification and the app shows a one-time message that
  Stop is available only in the app. If the user dismisses the notification,
  nothing changes at that moment: the timer keeps running and Stop stays in
  the app.

- **R-NOTIFICATION-SYNC: Notification state (Decision 57):** the
  notifications follow the running timers. One idempotent operation in
  Platform services shows a notification for every running timer that has
  none and removes notifications that have no timer (when the permission was
  granted). App calls it at every process start (also one started by the Stop
  button of a notification) and after an import. A dismissed
  notification therefore comes back at the next app start (accepted). After
  a phone restart, an app update or a force stop the notifications reappear
  at the next app start, not earlier. The operation is separate from its
  callers, so a boot receiver (an extra component and permission) can be
  added later as one more caller; its details are to verify in the plan
  (open question 7).

### 5.4 Statistics

- **R-STATS-UNFINISHED: Statistics, unfinished period (Decision 41):** the
  current unfinished period is counted in "times the goal was achieved" and
  in the average completion percentage, using its result so far with the
  same rules (R-ACHIEVED, R-PERCENT). The card marks it as provisional and
  shows the number of days left (the days after today up to the end of the
  period; scenario E2). For a quit habit the period counts as
  achieved while the limit is kept and can change before the period ends.

- **R-STATS-RANGE: Statistics, range edges (Decisions 20, 41, 55):** a goal
  period counts if its first day is in the chosen range and not after today,
  and either its last day is also in the range, or it is the current period
  (it contains today) and the range reaches today or later. The current
  period is then counted with its result so far (R-STATS-UNFINISHED). So
  "this week", "1 year" and a custom range ending today treat the current
  period the same way, and a period that starts after today never counts
  (a daily goal in "this week" counts only the days up to today). Other
  periods that stick out of the range, also at its start, do not count.
  An abandoned period does not count (R-STATS-GOALS).
  Total time, number of occurrences and average per day use the days of the
  range; the average per day divides by the days within the life of the habit
  (R-STATS-HABIT-LIFE), also those without entries. A range reaching into
  the future counts only up to today. If the range contains no period that
  counts, the card says so. If the range contains no day of the habit's life,
  the total is 0, there is no average per day (it is not shown, never a
  division by zero) and the card says so (scenario E11).
  Statistics of archived habits stay available (Decision 17).

- **R-STATS-GOALS: Statistics per period length (Decisions 52, 62, 67):**
  goal statistics have one line for each period length that has at least one
  counting period (R-STATS-RANGE, R-STATS-HABIT-LIFE). A line collects the
  counting periods of all the habit's goals of that length (they never share
  a period, INV-GOAL-NO-SHARED-PERIOD), each evaluated against the target of
  its own goal. Lines of different lengths are never summed, and a day
  covered by goals of two lengths counts in both lines. An abandoned period
  is neither achieved nor failed: it is left out of "X of Y" and of the
  average, and the line shows how many periods were abandoned. Percentage and
  "achieved" are computed from the entries, never stored.

- **R-STATS-HABIT-LIFE: Statistics, life of the habit (Decision 43):** the
  life of a habit runs from its creation day (or the day of its earliest
  entry, if earlier) to its archiving day (inclusive) or today. The average
  per day divides only by the days of the range inside this life. Goal
  periods count only where a goal is valid (R-GOAL-VALIDITY) and the habit
  is not archived: periods before the first goal and after archiving are
  skipped. A goal period counts only if it contains at least one day of the
  habit's life (so a retrospective goal start does not create periods that
  were over before the habit existed; a period that only partly precedes the
  habit is evaluated whole, like D1). The period containing the archiving day is left out as a
  cut-off period (neither achieved nor failed). A habit restored from the
  archive keeps the gap as ordinary days.

### 5.5 Scenario table (specification of goals and statistics)

This table is the specification of the goal and statistics rules above and
the list of Domain tests (section 8): one row, one test. If a row and a rule
text disagree, that is a defect to fix in the document, not a choice for
the implementer. Rows are numbered for the plan; new edge cases are added
here.

Conventions: today is Wednesday 11 Nov 2026 (the week is Mon 9 - Sun 15
Nov) unless a row says otherwise. An end of validity is exclusive ("ends 9
Nov" means the last valid day is 8 Nov). "Setup S": Build habit with a
weekly goal, target 3 days, mode repeat, first day Mon 26 Oct; weeks of 26
Oct and 2 Nov are completed, the week 9-15 Nov is current.

**A. Evaluation (R-PERIOD-RESULT, R-ACHIEVED, R-PERCENT, R-EXACT-VALUES)**

| ID | Situation | Expected outcome |
|---|---|---|
| A1 | Build, Numeric, daily target 2 l; entries 1.25 l and 0.75 l on one day | Result 2, achieved, 100% |
| A2 | Build, Binary, weekly target 3; 2 days with an entry | Not achieved, 66% (truncated from 66.6) |
| A3 | Build, Numeric, target 0.8; entries 0.7 and 0.1 | Achieved, 100% (exact decimals) |
| A4 | Quit, Binary, weekly limit 3; 3 days, then 4 days | 3 days: achieved, 100%. 4 days: not achieved, 75% |
| A5 | Quit, limit 0; result 0, then result above 0 | Result 0: achieved, 100%. Above 0: not achieved, 0% |
| A6 | Build, target 1000 ml; result 996 ml | Not achieved; shown as 99%, never 100% |

**B. Goal change (R-GOAL-CHANGE; S = setup S, "Delete" = delete the unfinished period)**

| ID | Situation | Expected outcome |
|---|---|---|
| B1 | S, target to 4, option Delete | Old goal ends 9 Nov and keeps weeks of 26 Oct and 2 Nov. New weekly goal (4) from 9 Nov. No gap, no overlap |
| B2 | S, target to 4, option Abandon | Old goal ends 12 Nov; week 9-15 is abandoned. New weekly goal from 16 Nov (default; 9 Nov is not available) |
| B3 | S, target to 4, option Wait | Old goal ends 16 Nov; week 9-15 counts normally with target 3. New goal (4) pending from 16 Nov |
| B4 | S, period to day (target 1), Delete | Old goal ends 9 Nov. New daily goal from 11 Nov. 9 and 10 Nov have no goal and do not count |
| B5 | S, period to day, Abandon | Old goal ends 12 Nov; week 9-15 abandoned. New daily goal from 11 Nov. 11 Nov counts in both lines |
| B6 | S, period to day, Wait | Old goal ends 16 Nov; week 9-15 counts normally. New daily goal pending from 16 Nov (default). If the user picks 11 Nov, 11-15 Nov count in both lines |
| B7 | Daily goal (target 1) from 9 Nov; period to week (target 3), Delete | Old goal ends 11 Nov (today's period removed; 9 and 10 Nov stay). New weekly goal from 9 Nov. 9 and 10 Nov count in both lines |
| B8 | Yearly goal from 1 Jan 2026 (no completed period); period to day, Delete | Old goal removed (no empty record). New daily goal from 11 Nov, not from 1 Jan |
| B9 | As B8, but the user picks the start 1 Nov | New daily goal from 1 Nov; 1-11 Nov are counted (only days of the habit's life, R-STATS-HABIT-LIFE) |
| B10 | S, period unchanged, target to 4, Abandon; the user picks the start 10 Nov | Not allowed: week 9-15 contains a day of the old goal (INV-GOAL-NO-SHARED-PERIOD). The earliest start offered is 16 Nov |
| B11 | Weekly goal created today (no completed period); target changed, Delete | Old goal removed; new goal from 9 Nov |
| B12 | S, switch the goal off | The same three options: Delete ends the goal 9 Nov, Abandon 12 Nov, Wait 16 Nov. No new goal; history stays |
| B13 | Weekly goal that ended 2 Nov, nothing valid today | The goal section is off; enabling it creates a new goal from 9 Nov. The ended goal stays |
| B14 | S, only the habit name (or unit, step) changes | No goal changes, no choice is asked |
| B15 | Goal for 30 days from 11 Nov; N to 40, then N to 10 on 25 Nov (day 15) | 40: the end moves from 11 Dec to 21 Dec, no choice asked. 10 on day 15: rejected, N cannot be below 15 |

**C. Goals for N periods (R-GOAL-VALIDITY)**

| ID | Situation | Expected outcome |
|---|---|---|
| C1 | 30 days, created 11 Nov | Ends 11 Dec (last day 10 Dec); nothing repeats |
| C2 | 30 days, created 20 Jan 2027 | Ends 19 Feb 2027; passes February with no special rule |
| C3 | 8 weeks, created Wed 11 Nov | First day Mon 9 Nov; ends Mon 4 Jan 2027 |
| C4 | The day after the end of a goal for N periods | No goal is valid; behaves as B13 |

**D. First period (R-GOAL-VALIDITY)**

| ID | Situation | Expected outcome |
|---|---|---|
| D1 | Weekly Binary goal, target 5, created Wed 11 Nov; entries on Mon 9 and Tue 10 Nov | The first period is the whole week 9-15 Nov; both entries count |
| D2 | Monthly goal created 20 Nov | The first period is the whole of November (from 1 Nov) |
| D3 | Yearly goal created 11 Nov with the default start | First day 1 Jan 2026 |

**E. Statistics range (R-STATS-RANGE, R-STATS-UNFINISHED, R-STATS-GOALS)**

| ID | Situation | Expected outcome |
|---|---|---|
| E1 | Daily goal, range "this week" | 9, 10, 11 Nov count (11 Nov provisional); 12-15 Nov start after today and never count. X of 3 |
| E2 | Weekly goal, range "this week" | Week 9-15 counts as the current period, provisional, 4 days left. X of 1 |
| E3 | Monthly goal, range "6 months" (12 May - 11 Nov) | June-Nov count (Nov provisional); May starts before the range and does not. X of 6 |
| E4 | Monthly goal, range "1 year" (12 Nov 2025 - 11 Nov 2026) | Dec 2025 - Nov 2026 count, Nov provisional. X of 12 |
| E5 | Quarterly goal, range "this quarter" | Q4 counts as the current period, provisional; the range reaches the future, so only days up to today count |
| E6 | Weekly goal, custom range 3-8 Nov | No period counts (week 2-8 Nov starts before the range); the card says so |
| E7 | Weekly goal, custom range 1-31 Oct | Weeks of 5, 12 and 19 Oct count; the week of 26 Oct sticks out of the range and does not. X of 3 |
| E8 | Daily goal in Oct (ended), weekly goal from 2 Nov; range 1 Oct - 11 Nov | Two lines, never summed: Daily X of 31; Weekly X of 2 (week 9 Nov provisional) |
| E9 | S abandoned on 11 Nov (B2); range "this week" | Week 9-15 is not counted in "X of Y"; the line shows 1 abandoned period; entries after 11 Nov do not change the goal |
| E10 | Two goals of different lengths overlap (B5), range "this week" | Weekly line and Daily line both count 11 Nov |
| E11 | Habit created 11 Nov, range "previous month" (October); or a habit archived before the range | Total 0, no average per day; the card says the range has no day of the habit's life |

**F. Archiving (R-STATS-HABIT-LIFE)**

| ID | Situation | Expected outcome |
|---|---|---|
| F1 | Habit with a weekly goal archived on 11 Nov; range "this week" | The week 9-15 contains the archiving day: a cut-off period, neither achieved nor failed, not counted |
| F2 | Habit created 11 Nov, daily goal with the retrospective start 1 Nov, range "this month" | Only 11 Nov counts (X of 1); 1-10 Nov are before the habit's life and are not periods. For a Quit Binary habit they are not free successes either |

**G. Timer and value of a day (R-DAY, R-DAY-VALUE, R-TIMERS, R-VALID-TIMER-STOP)**

| ID | Situation | Expected outcome |
|---|---|---|
| G1 | Timer started 23:30 on 10 Nov, still running at 00:20 on 11 Nov | Value of 10 Nov: 30 min; value of 11 Nov: 20 min and growing |
| G2 | The timer of G1 is stopped at 00:20 | One entry (start 23:30, length 50 min); 30 min fall on 10 Nov and 20 min on 11 Nov |
| G3 | Stop when the clock was moved back before the start instant | No entry; a message; the timer keeps running |
| G4 | Running timer, range "this week", Duration goal | The running timer's length so far is in the total and in the result of the goal period |
| G5 | Time zone changes | Duration and Numeric entries are recomputed from the stored instants; Binary entries keep their stored date |
| G6 | Numeric entry added on the past date 5 Nov | Stored with the time 12:00 on 5 Nov |
| G7 | Habit with a running timer is archived | The timer is stopped with an entry, the notification is removed |
| G8 | Entry from 23:30 on 10 Nov to 00:20 on 11 Nov; the Entries port reads the range 11 Nov - 11 Nov | The entry is returned (its span overlaps the range), so the 20 min fall on 11 Nov; an export reading range by range loses no entry |

**H. Notifications (R-NOTIFICATION-SYNC, R-TIMER-PERMISSION)**

| ID | Situation | Expected outcome |
|---|---|---|
| H1 | The user dismisses the notification of a running timer | The timer keeps running; the notification comes back at the next app start |
| H2 | An import replaces all data | Notifications are synchronized: one per imported running timer, others removed |
| H3 | The notification permission is refused at the first timer start | The timer works; a one-time message says Stop is available only in the app |

**I. Limits (R-VALID-GOAL)**

| ID | Situation | Expected outcome |
|---|---|---|
| I1 | Binary weekly target 8 | Rejected (maximum 7); 7 is accepted |
| I2 | Binary monthly target 31, evaluated for February | Accepted; unreachable in February (Decision 65), not achieved |
| I3 | Duration daily target 25 h, then 24 h | 25 h rejected; 24 h accepted |

**J. Import validation (section 7)**

| ID | Situation | Expected outcome |
|---|---|---|
| J1 | A numeric entry (value 3) of a Binary habit | File rejected; current data unchanged |
| J2 | A negative Numeric value, or a Binary goal target of 8 with a weekly period | File rejected (R-VALID-ENTRY, R-VALID-GOAL) |
| J3 | A running timer of a Numeric habit, or of an archived Duration habit | File rejected |
| J4 | An entry, a goal or a timer that refers to a habit missing from the file | File rejected |
| J5 | A valid file | Imported; all invariants hold afterwards |

## 6. User interface structure

Terminology: "habit" means a habit that is not
archived; "archived habit" is the exception. The word "active" is not used.
"Available" may be used where a qualifier is needed.

**UI principles (stated by the user, apply to every screen):**
1. The app must be intuitive and simple. Minimal friction (see "Key
   quality" in the requirements): the input method follows the habit type,
   the common action is one tap.
2. The app must support the user in building and quitting habits. Feedback
   is positive and motivating, not punishing or scolding (e.g. a green
   check for a kept limit instead of a text about what remains).

**Navigation (Decision 23):** bottom navigation bar with three tabs: Day,
Habits, Statistics. No swiping between tabs. Day is always the start
screen. Creating, editing, archiving and deleting habits happens on the
Habits screen only. Export / import is a menu item on the Habits screen
(Decisions 30 and 31). There is no separate
screen for past days: the Day screen shows the selected date, changed with
previous / next arrows or a calendar picker. A setting for the default
start screen is out of v1 (in "Ideas for later").

**Empty state (Decision 23):** the Day screen shows an empty state only
when there is no habit that is not archived. With no habits at all the text
says so and the button creates a habit. If all habits are archived the text
says so and the button leads to the Habits screen (to restore one). With at
least one habit there is no such button. Having no entries for a day is a
normal state and shows nothing special.

**Day screen (Decisions 24, 25, 26):** a list of all habits, in a fixed
order in v1: alphabetical by name, ignoring case (no manual ordering). In v1
every habit appears every day (no weekday schedule). The rules behind the
values are in section 5 (R-DAY-VALUE, R-ENTRY-DATE, R-PERCENT,
R-INDICATOR-PERIOD, R-TIME-REFRESH); this section describes the layout only.

*Row.* Each row has the habit name, an action control that depends on the
type, the value of the selected day (Decision 45) and a small progress
indicator. It must fit the width of a phone. Tapping the row (outside the
action control) opens the bottom sheet of the habit's entries (Decision 27);
the action control is a separate touch area that performs the action.

*Action controls.*
- Binary: tap toggles a check (entry exists or not; for a quit habit the
  check means "it happened"). The progress indicator uses the green check
  for a kept limit, so the two must look different: the Binary action
  control of a quit habit uses a symbol other than the check (UI principles 1
  and 2; the symbol is chosen in the plan, open question 16).
- Numeric: a "+" button adds one step as a separate entry, plus a way to
  type a value, which adds an entry too (nobody taps "+" 200 times).
- Duration: start / stop of the timer (only on today's date), plus manual
  entry of a time. While a timer runs, its session time is shown next to
  Stop.

*Value (R-DAY-VALUE).* Every row shows the value of the selected day as a
short text (e.g. "1.25 l", "1 h 20 min"; for Binary the check itself),
whether or not the habit has a goal. It is the value of the day, not the
result of the goal period; the period is shown by the indicator.

*Progress indicator (R-PERCENT, R-INDICATOR-PERIOD).* Compact, no descriptive
text. It shows the completion percentage of the period containing the
selected date. At 100% it shows a green check; otherwise the percentage. For
a quit habit it stays a green check while the limit is kept; after the limit
is exceeded it shows the percentage from R-PERCENT. If a fraction is ever
shown it reads "result / limit". Indicator and action control must look
different, so the indicator is not mistaken for a button (suggestion: a
ring with the number inside; the look is decided in the plan / UI work). If
the unfinished period must be marked as "in progress" (Decision 41), the Day
screen does it implicitly by showing the result so far; the detailed view is
on the Statistics screen.

*Other dates (R-ENTRY-DATE, R-TIME-REFRESH).* Entries apply to the selected
day, also a past one; the timer is offered on today's date only. The
selected date follows today until the user changes it. The indicator shows
the period that contains the selected date.

**Entries of one habit on one day (Decision 27):** tapping a habit row on
the Day screen opens a bottom sheet with the entries of that habit on the
selected date. A Numeric entry can be edited (value) and a Duration entry
too (start and end); a Binary entry has nothing to edit and can only be
deleted. Any entry can be deleted, and an entry can be added manually. Any date is
reached with the previous / next arrows or the calendar picker (Decision
23), so old entries need no separate screen. The picker has no markers
for days with entries in v1; a month calendar with habits marked on their
days is in "Ideas for later" ("Calendar screen") and would be a later
replacement of the picker, needing a Domain query for the days that have
entries (open question 18).

**Habits screen (Decision 28):**
- A list of habits (not archived), a "+" button to create one, a
  collapsible section "Archived" (restore; their statistics stay
  available). Tapping a row opens the edit form. Archiving and deleting
  (with confirmation) are in the edit form. Export / import is a menu item
  (Decisions 30 and 31).
- One form for creating and editing. A goal can be created, changed or
  switched off at any time in the edit form of an existing habit, not only
  when the habit is created (scenarios B1-B14). The form has: name, direction (Build / Quit),
  measurement type. For Numeric also the unit (built-in or custom label)
  and the "+" step. Last, an optional goal section, off by default: target
  value, period (day / week / month / quarter / year) and how long the goal
  lasts: it repeats every period (the default) or runs for a number of
  periods that the user types (e.g. 30 with the period "day"; Decision 53).
  The start defaults to the current period; the user can pick another date,
  and the form always shows the calendar period it selects (e.g. "Week 9-15
  Nov 2026", R-GOAL-VALIDITY). The form lists all goals valid today or pending (two
  can exist, e.g. after scenario B5); the user edits the one they tap, and a
  pending goal is edited in place (Decision 68). Changing the target or period
  of a goal valid today, or switching it off, asks for one of three options: abandon, delete the unfinished period, wait (R-GOAL-CHANGE,
  Decision 67). Switching it off keeps the history (Decision 42).
  Fields that cannot change are shown read-only with a short explanation:
  direction after creation, type after the first entry (Decision 17).
  Changing a numeric unit shows a warning (relabel only). Editing a goal
  changes it on the same habit (R-GOAL-CHANGE, Decision 15). Saving the form
  (habit and goal) is one all-or-nothing operation (section 3).

**Statistics screen (Decision 29):** one screen. At the top a date range
selector: presets this week, previous week, this month, previous month, this
quarter, this year, 6 months, 1 year, and a custom range chosen in a
calendar. "6 months" and "1 year" end today and start the same calendar day
that many months or years earlier plus one day (on 11 Nov 2026, "6 months"
is 12 May - 11 Nov 2026). The default is this week (Decision 41). Below, one card per habit, with archived habits in a
separate section. The range is common to all cards. A card shows only
figures that make sense for the habit:
- Duration: total time and average per day.
- Numeric: sum of values and average per day (REQ-M8d).
- Binary: number of days with an occurrence.
- If the habit has goals in the range: one line per period length, labeled
  with the period (and the target when all counted periods have the same
  one), with "achieved X of Y" (Y = the periods that count, including the
  current one), the average completion percentage and, if any, the number of
  abandoned periods (Decisions 62, 67). Different period lengths (e.g. a
  daily goal in January and a weekly one in February) get separate lines and
  are never summed (R-STATS-GOALS). The current unfinished period is included and
  marked as provisional, with the days left (Decisions 41, 55); the mark
  appears only when the range reaches today or later, because only then does
  the range contain the current period. If no period counts, the card says
  so (Decision 20).
Charts are out of v1 ("Ideas for later").

**Export / import:** a menu item on the Habits screen; behavior in
section 7 (Decisions 30 and 31).

## 7. Cross-cutting concerns

- **Localization (Decision 22):** Presentation exposes text identifiers
  and raw values (durations, dates, numbers, unit identifiers) in simple
  types or types owned by Presentation, not Domain types (Decision 61); the
  GUI resolves them from Android string resources and formats them for the
  locale. Adding a language means adding resource files only. Built-in
  unit names come from resources; a custom unit is a plain label.
- **Timer in the background:** see section 5 (Decision 21).

- **Export / import format (Decision 30):** a JSON file with a format
  version number. The content (export date, habits, goals with their
  validity, entries, running timers) is described by file-model classes owned by the
  Platform services adapter (Decision 36), not by the stored representation
  of Persistence. The adapter parses the JSON and maps it to domain
  entities, handing Domain the entities together with the format version
  number (and does the reverse on export). Domain owns the format rules: it
  checks the version is supported and the data before calling the "replace
  all" operation (Decision 49). It checks all `INV-*` and `R-VALID-*` rules
  of section 4, and also that the shape of every entry matches the
  Measurement of its habit, that a running timer belongs to a habit of type
  Duration that is not archived, and that every reference (of an entry, a goal
  or a timer) points to a habit in the file (scenarios J1-J5). Domain has no separate export model. Changing the
  persistence library or its schema does not invalidate old files. Export
  to CSV or other formats is not planned (add to "Ideas for later" if
  wanted).

- **Import behavior (Decision 31):** import replaces all data in the app
  with the content of the file. Before replacing, the app shows a short
  summary of the file (export date, number of habits and entries; the date is
  stored in the file) and
  asks for confirmation, with a warning that current data will be lost.
  The file is validated first (format, supported version, the rules above); import is
  all-or-nothing: an invalid file or one from a newer unsupported version
  is rejected and the current data stays unchanged. Merging with existing
  data is out of v1 ("Ideas for later" in the requirements). After an
  import, the timer notifications are synchronized with the imported timers
  (R-NOTIFICATION-SYNC). The file is chosen with the system picker in the
  GUI, which passes an opaque string identifier of the file through
  Presentation and Domain to the Backup file port; only Platform services
  can interpret it (Decision 60).

- **System backup (Decision 33):** Android Auto Backup (cloud backup and
  device-to-device transfer) is disabled for the app, so data leaves the
  device only through a user-initiated export (constraint in the
  requirements). Restoring on a new phone requires an earlier export. The
  exact manifest settings must be checked in the official Android docs
  before the plan (open question 21).

- **Local storage:** all data stays on the device in Room (Decision 35),
  behind the storage ports; no network access at all.

## 8. Testing strategy

Decision 38. Tests follow the components; the weight is on logic. Detailed
test cases belong to the plan.

- **Domain (JVM unit tests):** every row of the scenario table (end of
  section 5) is one test; besides that periods (day to year), goal
  validity and goal change, goal evaluation, completion percentage and its
  display rounding,
  exact decimal sums (0.7 + 0.1 reaches a target of 0.8), statistics,
  splitting a duration entry across days (also a running timer), timer
  logic, export file format. Ports are replaced by simple hand-written
  fakes (in-memory repositories, a clock with a fixed time and time zone
  and with ticks and day changes triggered by the test, Decision 13); no
  mocking library.
- **Presentation (JVM unit tests):** screen state and reactions to
  actions, with the same kind of fakes.
- **Persistence (instrumented, device or emulator):** Room with an
  in-memory database, mapping between stored and domain classes, schema
  migrations.
- **Export / import:** a round-trip test (export, import, same data) and
  sample files from older format versions; a rejected invalid file leaves
  data unchanged (Decision 31).
- **GUI and Platform services:** manual checks (notification, Stop button,
  timer surviving closing the app). No automated UI tests in v1; they can
  be added later without changing the architecture.

**Success criteria** (`docs/REQUIREMENTS.md`, section 4):
- No data loss: Persistence and round-trip tests, plus the week of real use.
- Statistics match a manual calculation: a Domain test replays the
  three-habit week (a duration build habit with a timer and a past-day
  entry, a numeric build habit with a daily goal, a binary quit habit with
  a limit of 3 days per week) with hand-computed expected results.
- Goals evaluated correctly: the same tests plus edge cases:
  - result equal to target, target 0;
  - goal change in the middle of a period with each of the three options,
    also with a changed period length and a retrospective start
    (R-GOAL-CHANGE, scenarios B1-B15);
  - a goal for 30 days that passes through February (R-GOAL-VALIDITY);
  - a range ending today with a weekly and a monthly goal, and "1 year"
    versus "this week" (R-STATS-RANGE);
  - a timer running over midnight, in the day value and in statistics;
  - the notification sync after a dismissal and after an import.
- Final confirmation: one week of real use by the author.

## 9. Decisions and patterns

The decisions log (Decisions 1-67, numbers 19 and 39 unused: the choice,
options considered, reason)
is in `docs/DESIGN-DECISIONS.md`. This document cites decisions by number.
The rules themselves are owned by sections 4-5 (section 6 describes the
layout and cites the rules); the log gives the reasoning.

**Patterns used** (design principle 3):

| Pattern | Where | Reason | Why not the alternatives |
|---|---|---|---|
| MVVM with unidirectional state flow | Presentation / GUI | See Decision 9. | MVI, MVP: see Decision 9. |
| Sealed hierarchy (sum type) | `Measurement` | See Decision 10. | Enum plus optional fields, Strategy: see Decision 10. |
| Ports and Adapters (dependency inversion) | Domain defines ports; Persistence and Platform services implement them | Required by principle 1: dependencies must point to Domain. See Decisions 48 and 66. | Domain calling Room or Android directly breaks principle 1. |
| Dependency Injection (manual, constructor) | App wires all components | Components receive their ports through constructors; only App knows concrete classes (principle 1). See Decision 36. | Service locator hides dependencies; Hilt / Koin add configuration the app does not need. |
| Repository | Shape of the storage ports | Domain sees a collection of entities, not a database. See Decision 66. | Ports shaped like SQL queries leak persistence details into Domain. |

## 10. Open questions

**Open, carried to the plan phase** (none changes the structure of the
design):

7. Verify in the official Android docs before the plan (the assistant is
   not sure): the exact API for a notification chronometer; whether an
   ongoing notification without a foreground service can be dismissed by
   the user on current Android versions; for a later boot receiver
   (Decision 57): its permission and restrictions, and whether it may post
   the notification without a foreground service. The decisions themselves
   are made (Decisions 21, 47 and 57); this is only verification.
12. App name (inherited from requirements).
16. Look of the progress indicator (ring or other), whether the Day
    screen needs any explicit "in progress" mark, and the symbol of the
    Binary action control of a quit habit, which must differ from the green
    check of the indicator (UI / plan phase).
18. Later iteration: a month calendar with habits marked on their days
    replaces or extends the date picker ("Ideas for later"). Needs a
    Domain query for days with entries and, for the marks, colors / icons
    of habits (also in "Ideas for later"). Not designed in v1.
21. Exact Android settings to disable Auto Backup and device transfer
    (to verify in the official docs; the assistant is not sure of the
    current details).

**Resolved** (numbers kept so that references stay valid): 1 (Decision 14),
2 (12, 13), 3 (15), 4 and 5 (17), 6 (18), 8 (22), 9 (the diagram shows App
wiring GUI and Presentation), 10 (34), 11 (37), 13 (20, 41), 14 (32),
15 (37), 17 (41), 19 (REQ-M8d in the requirements), 20 (57), 22 (R-GOAL-CHANGE),
23 (R-DAY, Decision 63).
