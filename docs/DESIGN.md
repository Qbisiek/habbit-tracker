# Design: Habit Tracker (working title)

> **STATUS (2026-11-14): CLEANED UP, NOT APPROVED.** The user-visible behavior
> (rules and scenarios) lives in `docs/REQUIREMENTS.md` section 7 (IDs `BHV-*`) and
> the screens in `docs/UX.md`; this document keeps the structure and the mechanisms.
> Parts marked **TO DESIGN** wait for the next step (state and plan:
> `docs/STATUS.md`):
> - the Goal entity and its invariants (BHV-GOAL-R1 to R7);
> - a Numeric entry with a date and a time (Decision 80), and the creation day and
>   time of habits and goals (BHV-HABIT-R4, BHV-EXPORT-R1);
> - the import in two modes (Decision 78);
> - how a later version adds each "accommodate" item of `docs/REQUIREMENTS.md`
>   section 6 (REQ-X1 to X6);
> - export reads "from the earliest entry to today" range by range (section 3,
>   Entries port), but entries after today can exist (BHV-ENTRY-R3,
>   BHV-ENTRY-21): such an entry would be lost;
> - the open questions of section 10.

## Summary and map

- **What:** Android app (Kotlin, Jetpack Compose, minimum API 33), fully offline,
  local Room storage, no analytics (section 2).
- **Architecture (section 3):** `GUI -> Presentation -> Domain <- Persistence`,
  `Domain <- Platform services`; App wires everything. Domain is a pure Kotlin/JVM
  Gradle module and owns the ports.
- **Data (section 4):** Habit (name, direction Build / Quit, creation day, archiving
  day) has one Measurement (Binary, Numeric with unit, or Duration), optional Goals
  (separate entities), Entries (a moment in time and a value; Binary: a date) and at
  most one RunningTimer (a start instant).
- **Mechanisms (section 5):** period, day and week parameters from the Clock port;
  exact decimals; the split of a Duration entry across days; time refresh through the
  Clock port; the timer without a background service; notification sync.
- **Screens (section 6):** in `docs/UX.md`.
- **Cross-cutting (section 7):** Polish and English via text identifiers; versioned
  JSON export / import; Auto Backup off.
- **Testing (section 8):** Domain and Presentation as JVM tests with fakes; the
  scenarios of `docs/REQUIREMENTS.md` section 7 are the Domain tests; the success
  criteria are checked by a replay test of the three-habit week.
- **Where things are:** behavior in `docs/REQUIREMENTS.md` section 7 (`BHV-*`);
  features and constraints in the same file (`REQ-*`); screens in `docs/UX.md`;
  decisions in `docs/DESIGN-DECISIONS.md` (cited as "Decision N"); open questions in
  section 10; diagram in `docs/diagrams/components.drawio`.

## 1. Overview

Source: `docs/REQUIREMENTS.md`. Design topics (derived from Must/Should features and
constraints):

1. Habit model: measurement type and direction.
2. Entries: any day, editing past days.
3. Timer: survives closing the app; ongoing notification with Stop (Should).
4. Goals and periods: fixed day/week/month/quarter/year, repeating or for a number of
   periods, evaluation per type and direction, goal completion percentage.
5. Statistics over a user-chosen date range.
6. Habit management: create, edit, archive, restore, delete.
7. Units: built-in and custom label (Should).
8. Screens and navigation (`docs/UX.md`).
9. Export / import to a file (Should).
10. Local, offline-only storage.
11. Localization: Polish and English, texts in resources.
12. Platform: Android only, minimum version.
13. No analytics or crash reporting (constrains library choices).
14. Extensibility: room for the "accommodate" items of `docs/REQUIREMENTS.md`
    section 6 (**TO DESIGN**).

Always part of the design: architecture with a component diagram, and the testing
strategy (including verification of the success criteria).

## 2. Tech stack

Decided:
- Android, Kotlin (CLAUDE.md and requirements).
- GUI toolkit: Jetpack Compose (Decision 8).
- A thin `ViewModel` from androidx (lifecycle library) in the GUI component, only to
  keep a Presentation object alive across configuration changes such as rotation
  (Decision 9). The exact artifact must be checked in the official docs before it is
  named in the plan.

- Minimum Android version: 13 (API 33) (Decision 34). `java.time` is available
  natively; the notification permission (`POST_NOTIFICATIONS`) is always required, so
  there is one code path. Lowering it later (e.g. for a Google Play release) would
  mostly affect Platform services.

- Local storage: Room (androidx), only inside Persistence; stored classes are
  separate from domain entities and mapped (Decision 35).

- JSON serialization for export / import: kotlinx.serialization, in the Platform
  services adapter only, with file-model classes separate from Domain (Decision 36).
- Dependency injection: manually, by constructor, in App (Decision 36).
- Navigation: Navigation Compose (Decision 36).

To check in the official docs in the plan phase: versions of all libraries and the
minimum versions required by Compose and androidx.

## 3. Architecture

Level 1, confirmed by the user. Diagram: `docs/diagrams/components.drawio`
(confirmed by the user). It shows App on the left with dashed arrows (creates /
wires) to every component, and solid arrows for code dependencies.

**Module structure (Decision 1):** one Gradle module for Domain (pure Kotlin/JVM, no
Android) and one `app` module for everything else, separated by packages.

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
`Domain <- Platform services`. Domain depends on nothing. App knows all components;
no component knows App.

**Objects created by Android (Decision 71):** Android creates the Activity and the
timer BroadcastReceiver, so they cannot get dependencies through a constructor. Each
of the two components that contain them (GUI, Platform services) defines a small
interface listing what it needs (for GUI, the factories of Presentation objects; for
the receiver, the Data operations and Timer notification ports). The Application
class in App implements both interfaces, and the Activity and the receiver reach it
by casting the application context to the interface. The dependency still points from
App to the components, never the other way.

**How the design principles are met:**
1. Business logic free of frameworks: Domain is a separate pure Kotlin/JVM module;
   storage and platform features are reached through ports defined by Domain and
   implemented in Persistence and Platform services.
2. Business logic separated from the GUI: GUI depends on Presentation only (state in,
   actions out; the state contains no Domain types, Decision 61); Presentation and
   Domain know nothing about Compose. Replacing the GUI does not touch them.
3. Patterns: see section 9 (MVVM with unidirectional state, Ports and Adapters,
   Repository, sealed hierarchy, manual dependency injection).

**Ports (Decisions 48, 59):** interfaces defined by Domain, implemented outside it.

| Port | Implemented by | Purpose |
|---|---|---|
| Habits | Persistence | Reads of habits. Every write of a habit is a Data operation. |
| Entries | Persistence | Single-entry writes: add, edit, delete (also the Binary toggle). Entries of a habit, read by date range (never "all entries", Decision 54). A read returns every entry whose time span overlaps the range (calendar days in the device time zone), not only entries that start in it (test G8, section 8). Also the date of the habit's earliest entry (for BHV-STATS-R3). Export reads from that date to today, range by range (see the banner: entries after today). |
| Goals | Persistence | Goals of a habit, active and archived. Reads only: every goal write is part of a Data operation. |
| Running timers | Persistence | Start instants of running timers (the source of truth, Decision 18). Single writes: start a timer, discard it without an entry. Stopping is a Data operation. |
| Data operations | Persistence | Composite operations that touch several of the ports above, each all-or-nothing (Decision 59). |
| Clock | Platform services | Current instant and device time zone; a tick stream that ticks while it has a subscriber (Presentation subscribes only while a timer runs); signals of a day or time zone change (section 5.4, Decision 58). |
| Timer notification | Platform services | Show and remove the notification with Stop; synchronize notifications with running timers (section 5.6). |
| Backup file | Platform services | Read and write the export file, which is identified by an opaque identifier (Decision 60): JSON parsing and serialization, mapping between file classes and domain entities with the format version (section 7). |

Rules for all storage ports:
- **Reads return `Flow`, writes are `suspend`.** Screens refresh by themselves when
  data change. Domain therefore depends on `kotlinx.coroutines`, and on nothing else
  outside the Kotlin standard library. Passing time is not a data change: it reaches
  screens through the Clock port (section 5.4).
- **Composite operations are the methods of the Data operations port (Decision 59),
  named after business actions; each runs in one database transaction.** They are
  all-or-nothing, so a crash in the middle leaves the previous state: for the timer,
  it keeps running and Stop can be tapped again. The operations are:
  - stop timer (create an entry and delete the running timer),
  - archive habit (stop its timer as above, set the archiving day, archive its active
    goal and delete a pending one, BHV-HABIT-R3),
  - restore habit (clear the archiving day; optionally create a new goal as a copy of
    the archived one, BHV-HABIT-R3),
  - delete habit (the habit with its entries, goals and running timer),
  - import (Replace all or Add to existing, Decision 78),
  - save habit (the habit and its goal changes from the form: create, edit, and the
    archiving, replacement or creation of goals by BHV-GOAL-R4 and R4a, all in one
    operation).

  **Domain computes the resulting entities** (the goals after a change, the length of
  a stopped timer, the validations); a Data operations method only writes them in one
  transaction. Domain does not know the term "transaction". Timer notifications are
  not part of the transaction: Domain updates them through the Timer notification
  port after the operation, and a mismatch left by a crash is repaired by the
  notification sync (section 5.6).

**Note on iOS:** pure Kotlin Presentation and Domain make a later move easier, but
sharing them with iOS would need Kotlin Multiplatform and a change of the Domain
module type. Not designed for in v1 (out of scope); the choices only do not rule it
out.

## 4. Data model

Level 1: names and relations only; attributes and invariants are Level 2. The Goal
and the Numeric entry are **TO DESIGN** (see the banner).

- **Habit**: a pure definition of the activity. Identity, name, direction (Build /
  Quit), creation day, archiving day (empty while the habit is not archived;
  Decision 43). Direction is the habit's intent and exists even without a goal. It is
  not part of the goal.
- **Measurement**: how the habit is measured. A closed (`sealed`) set of variants:
  Binary, Numeric (with a unit), Duration. Exactly one per habit. The unit belongs to
  the Numeric variant, not to the Habit. A unit is data, not a type: either a
  built-in one (identifier, name from string resources) or a custom one (text label).
- **Goal** (**TO DESIGN**): optional (Decision 2), a separate entity (Decision 3), so
  changing a goal does not change the habit definition. It refers to the habit and
  has a target value and a period (day / week / month / quarter / year), which is a
  separate attribute so a goal without a period can be added later (Decision 7). The
  comparison ("at least" / "at most") comes from the habit's direction, not from the
  goal. The rest (first and last day, mode, archive, creation day, the invariants
  and what a later version changes for REQ-X1 and X2) follows from BHV-GOAL-R1 to R7
  and is designed in the next step.
- **Entry**: a recorded value for a habit at a moment in time (date and time, not
  only a day; a Binary entry holds a date, Decision 44). Its meaning comes from the
  habit's Measurement.
- **RunningTimer**: a habit plus the instant the timer was started.

Relations: Habit 1 - 1 Measurement; Habit 1 - 0..n Goal (at most one active,
BHV-GOAL-R3); Habit 1 - 0..n Entry; Habit 1 - 0..1 RunningTimer (several timers may
run at once, at most one per habit; Decision 18).

Level 2 (attributes that the rules in `docs/REQUIREMENTS.md` section 7 need):
- **Habit**: the rules about a name, direction and type are BHV-HABIT-R1 and R2.
  Archived habits keep entries and statistics; deleting removes the habit with its
  entries, goals and running timer (BHV-HABIT-R3, Decision 17). Archiving sets the
  archiving day; restoring clears it.
- **Measurement**: for Numeric the unit is a label on the habit; entries hold plain
  numbers. Changing the unit relabels all values, without conversion (Decision 17).
  Numeric also has an optional step: the amount added by one tap of "+" on the Day
  screen (BHV-HABIT-R1; BHV-ENTRY-R8). The step does not change how entries are
  summed (Decision 25).
- **Entry**:
  - Duration: a start instant and a length. It can span midnight; the split per day
    is computed by Domain, not stored (Decision 12, section 5.3). Several per day.
  - Numeric: a value; several per day, summed. A date and a time kept as wall-clock
    time (Decision 80) are **TO DESIGN**.
  - Binary: stores a local date, not an instant (Decision 44); at most one per day
    (BHV-ENTRY-R1).
  - The day of a Duration entry is a calendar day in the device time zone
    (Decision 13, BHV-ENTRY-R2).
- **Validations** are defined by BHV-GOALVAL-R1, BHV-ENTRY-R5, BHV-HABIT-R1 and
  BHV-TIMER-R2. Domain checks them, and also, on import, every rule of BHV-IMPORT-R4.

**Built-in units (Decision 37):** each has an identifier, a name from string
resources (PL / EN) and a default "+" step. The identifiers and steps are business
data and live in Domain; the names live in resources. A custom unit is a text label
with a step of 1. The step can be changed per habit (Decision 25). Duration is not a
unit (it is its own type).

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

ml and l are separate units with no conversion (conversion is in "Ideas for later").

## 5. Mechanisms

The behavior itself is defined in `docs/REQUIREMENTS.md` section 7. This section
holds only what the design decides about how it is achieved.

### 5.1 Periods, day and week parameters

The first day of the week and the time zone are parameters of the period and day
calculations, supplied via the Clock port, not constants in the rules (Decision 11;
REQ-X5: a user setting for the first day of the week needs no conversion of stored
data). Periods, results, percentages and "achieved" are computed from entries and
goals, never stored (Decision 54).

### 5.2 Exact values

Values of Numeric entries, targets and sums are exact decimals (Decision 46;
BHV-EVAL-R3). Comparison with the target uses exact arithmetic, never floating point
and never a rounded percentage. A percentage is an exact fraction (1/3 has no finite
decimal expansion); it is truncated only for display, and the average completion
percentage is computed from the exact fractions and then truncated. The
representation (`BigDecimal`, an integer in thousandths or a fraction type) is chosen
in the plan.

### 5.3 Splitting a Duration entry across days

A Duration entry is one entry (Decision 12). Domain computes from its start instant,
its length and the device time zone the part that falls on each calendar day
(BHV-ENTRY-R2), and does the same for a running timer up to the current instant
(BHV-ENTRY-R4, BHV-TIMER-R1). After a time zone change the days are recomputed from
the stored instants.

### 5.4 Passing time

A `Flow` of data does not change when time passes, so time reaches the screens
through the Clock port (Decision 58). Besides the current instant and the time zone
it exposes a tick stream (every second, only while it has a subscriber; Presentation
subscribes only while at least one timer is running) and a signal on a change of day
or time zone. Presentation recomputes the time-dependent values (the value of the
day, the session time, the progress indicator) on a tick or signal. The selected date
of the Day screen is either "today" (it follows the clock) or a concrete date once
the user chose one (BHV-DAY-R1). A timer running over midnight keeps running and its
part after midnight belongs to the new day (section 5.3).

### 5.5 Timer and Android

No background service (Decision 21). Domain holds the start/stop logic (length = now
- stored start instant); Persistence holds the start instant; Platform services
implements a "timer notification" port (show / hide). The notification is an ongoing
system notification whose built-in chronometer only displays elapsed time from the
start instant; the app never reads state from it. There is one notification per
running timer (Decision 32). The Stop button in the notification calls the same stop
logic as the Stop button in the app, through a receiver in Platform services. A later
home screen widget would be one more adapter on the same ports (out of v1).

Archiving or deleting a habit with a running timer, and a stop with a non-positive
length, are behavior (BHV-TIMER-R2, R3); archiving and deleting are Data operations
(section 3) and the notification is updated after them (section 5.6).

### 5.6 Notification sync

The notifications follow the running timers (BHV-TIMER-R5, Decision 57). One
idempotent operation in Platform services shows a notification for every running
timer that has none and removes notifications that have no timer (when the permission
was granted). App calls it at every process start (also one started by the Stop
button of a notification) and after an import. The operation is separate from its
callers, so a boot receiver (an extra component and permission) can be added later as
one more caller; its details are to verify in the plan (open question 7).

## 6. User interface

Screens, navigation and layout: `docs/UX.md`. What the architecture requires of them:
the GUI renders the state of Presentation and forwards actions without making
decisions; texts are identifiers resolved by the GUI (section 3, Decisions 22, 61).

## 7. Cross-cutting concerns

- **Localization (Decision 22):** Presentation exposes text identifiers and raw
  values (durations, dates, numbers, unit identifiers) in simple types or types owned
  by Presentation, not Domain types (Decision 61); the GUI resolves them from Android
  string resources and formats them for the locale. Adding a language means adding
  resource files only. Built-in unit names come from resources; a custom unit is a
  plain label. The behavior of the language choice is BHV-LANG-R1 to R3; where the
  switch lives is an open UX point (`docs/UX.md` section 7).
- **Timer in the background:** see section 5.5 (Decision 21).

- **Export / import format (Decision 30):** a JSON file with a format version number.
  The content (export date, habits, goals, entries, running timers, as BHV-EXPORT-R1
  lists them) is described by file-model classes owned by the Platform services
  adapter (Decision 36), not by the stored representation of Persistence. The adapter
  parses the JSON and maps it to domain entities, handing Domain the entities
  together with the format version number (and does the reverse on export). Domain
  owns the format rules: it checks that the version is supported and that the data
  satisfy BHV-IMPORT-R4 before calling the import operation of the mode the user
  chose (Decision 49, Decision 78). Domain has no separate export model. Changing the
  persistence library or its schema does not invalidate old files. Export to CSV or
  other formats is not planned (add to "Ideas for later" if wanted).

- **Import (Decisions 49, 60, 78):** the file is chosen with the system picker in the
  GUI, which passes an opaque string identifier of the file through Presentation and
  Domain to the Backup file port; only Platform services can interpret it. The
  behavior (the summary, the choice of the mode, all-or-nothing) is BHV-IMPORT-R1 to
  R3. After an import, the timer notifications are synchronized (section 5.6).

- **System backup (Decision 33):** Android Auto Backup (cloud backup and
  device-to-device transfer) is disabled for the app, so data leaves the device only
  through a user-initiated export (constraint in the requirements). Restoring on a new
  phone requires an earlier export. The exact manifest settings must be checked in the
  official Android docs before the plan (open question 21).

- **Local storage:** all data stays on the device in Room (Decision 35), behind the
  storage ports; no network access at all.

## 8. Testing strategy

Decision 38. Tests follow the components; the weight is on logic. Detailed test cases
belong to the plan.

- **Domain (JVM unit tests):** every scenario of `docs/REQUIREMENTS.md` section 7 that
  concerns Domain logic is one test (the rows about UI, persistence and notifications
  are tested at their level or by hand); besides that periods (day to year), goal
  change, goal evaluation, completion percentage and its display rounding, exact
  decimal sums (0.7 + 0.1 reaches a target of 0.8), statistics, splitting a duration
  entry across days (also a running timer), timer logic, export file format. Ports are
  replaced by simple hand-written fakes (in-memory repositories, a clock with a fixed
  time and time zone and with ticks and day changes triggered by the test, Decision
  13); no mocking library.
  - **G8 (Entries port):** an entry from 23:30 on 10 Nov to 00:20 on 11 Nov; the port
    reads the range 11 Nov - 11 Nov. The entry is returned (its span overlaps the
    range), so the 20 minutes fall on 11 Nov; an export that reads range by range
    loses no entry.
- **Presentation (JVM unit tests):** screen state and reactions to actions, with the
  same kind of fakes.
- **Persistence (instrumented, device or emulator):** Room with an in-memory database,
  mapping between stored and domain classes, schema migrations.
- **Export / import:** a round-trip test (export, import, same data; BHV-EXPORT-1) and
  sample files from older format versions (BHV-EXPORT-2); a rejected invalid file
  leaves data unchanged (BHV-IMPORT-R1).
- **GUI and Platform services:** manual checks (notification, Stop button, timer
  surviving closing the app). No automated UI tests in v1; they can be added later
  without changing the architecture.

**Success criteria** (`docs/REQUIREMENTS.md`, section 4):
- No data loss: Persistence and round-trip tests, plus the week of real use.
- Statistics match a manual calculation: a Domain test replays the three-habit week
  (a duration build habit with a timer and a past-day entry, a numeric build habit
  with a daily goal, a binary quit habit with a limit of 3 days per week) with
  hand-computed expected results.
- Goals evaluated correctly: the same tests plus edge cases:
  - result equal to target, target 0 (BHV-EVAL-4, 5);
  - a goal change in the middle of a period, also with a changed period length and a
    retrospective start (BHV-GOAL-R4, BHV-GOAL-1 to 22);
  - a goal for 30 days that passes through February (BHV-PERIOD-2);
  - a range ending today with a weekly and a monthly goal, and "1 year" versus "this
    week" (BHV-STATS-R5);
  - a timer running over midnight, in the day value and in statistics
    (BHV-TIMER-1, 2, BHV-STATS-14);
  - the notification sync after a dismissal and after an import (BHV-NOTIF-1, 2).
- Final confirmation: one week of real use by the author.

## 9. Decisions and patterns

The decisions log (the choice, options considered, reason) is in
`docs/DESIGN-DECISIONS.md`. This document cites decisions by number. The behavior
rules are owned by `docs/REQUIREMENTS.md` section 7; the log gives the reasoning.

**Patterns used** (design principle 3):

| Pattern | Where | Reason | Why not the alternatives |
|---|---|---|---|
| MVVM with unidirectional state flow | Presentation / GUI | See Decision 9. | MVI, MVP: see Decision 9. |
| Sealed hierarchy (sum type) | `Measurement` | See Decision 10. | Enum plus optional fields, Strategy: see Decision 10. |
| Ports and Adapters (dependency inversion) | Domain defines ports; Persistence and Platform services implement them | Required by principle 1: dependencies must point to Domain. See Decisions 48 and 66. | Domain calling Room or Android directly breaks principle 1. |
| Dependency Injection (manual, constructor) | App wires all components | Components receive their ports through constructors; only App knows concrete classes (principle 1). See Decision 36. | Service locator hides dependencies; Hilt / Koin add configuration the app does not need. |
| Repository | Shape of the storage ports | Domain sees a collection of entities, not a database. See Decision 66. | Ports shaped like SQL queries leak persistence details into Domain. |

## 10. Open questions

**Open, carried to the plan phase** (none changes the structure of the design). The
numbers are stable:

7. Verify in the official Android docs before the plan (the assistant is not sure):
   the exact API for a notification chronometer; whether an ongoing notification
   without a foreground service can be dismissed by the user on current Android
   versions; for a later boot receiver (Decision 57): its permission and
   restrictions, and whether it may post the notification without a foreground
   service. The decisions themselves are made (Decisions 21, 47 and 57); this is only
   verification.
12. App name (inherited from requirements).
21. Exact Android settings to disable Auto Backup and device transfer (to verify in
    the official docs; the assistant is not sure of the current details).

Questions 16 (look of the progress indicator, symbol of the quit action control) and
18 (month calendar) moved to `docs/UX.md` section 7.
