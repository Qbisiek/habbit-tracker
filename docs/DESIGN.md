# Design: Habit Tracker (working title)

> **STATUS (2026-11-24): APPROVED BY THE USER.** The user-visible behavior (rules and
> scenarios) lives in `docs/REQUIREMENTS.md` section 7 (IDs `BHV-*`) and the screens
> in `docs/UX.md`; this document keeps the structure and the mechanisms. The checks
> carried to the plan phase are listed in section 10. State and plan:
> `docs/STATUS.md`.

## Summary and map

- **What:** Android app (Kotlin, Jetpack Compose, minimum API 33), fully offline,
  local Room storage, no analytics (section 2).
- **Architecture (section 3):** `GUI -> Presentation -> Domain <- Persistence`,
  `Domain <- Platform services`; App wires everything. Domain is a pure Kotlin/JVM
  Gradle module, owns the ports and offers the Domain operations, the only entry point
  for writes and for notification changes (also for the Stop receiver).
- **Data (section 4):** Habit (name, direction Build / Quit, creation day and time,
  archiving day) has one Measurement (Binary, Numeric with unit, or Duration), optional
  Goals (separate entities; last day and status derived), Entries (Binary: a date;
  Numeric: a value with a wall-clock date and time; Duration: a start instant and a
  length) and at most one RunningTimer (a start instant). Identities are UUIDs.
- **Mechanisms (section 5):** period, day and week parameters from the Clock port;
  exact decimals; the split of a Duration entry across days; time refresh through the
  Clock port; the timer without a background service and the Stop receiver;
  notification sync and permission state; the undo offer; entry times and "changed".
- **Screens (section 6):** in `docs/UX.md`.
- **Cross-cutting (section 7):** Polish and English via text identifiers; versioned
  JSON export (one consistent snapshot) and import in two modes, "Export and replace";
  Auto Backup off; extensibility for REQ-X1 to X6 (section 7.1).
- **Testing (section 8):** Domain and Presentation as JVM tests with fakes; the
  scenarios of `docs/REQUIREMENTS.md` section 7 are the Domain tests; the success
  criteria are checked by a replay test of the three-habit week.
- **Where things are:** behavior in `docs/REQUIREMENTS.md` section 7 (`BHV-*`);
  features and constraints in the same file (`REQ-*`); screens in `docs/UX.md`;
  decisions in `docs/DESIGN-DECISIONS.md` (cited as "Decision N"); open questions in
  section 10; diagram in `docs/diagrams/components.drawio`.
- **Map by topic:**
  - Timer and notification: 3 (Domain operations, Running timers, Timer
    notification, Data operations), 4.5, 5.3, 5.5, 5.6, 5.7, 8.
  - Entries: 3 (Entries port), 4.4, 5.3, 5.7, 5.8.
  - Goals: 4.3, 5.1, 7.1.
  - Habits and their order: 3 (pure functions), 4.1, 4.6, 6.
  - Statistics: 3 (pure functions: presets and computed values), 5.1, 5.2.
  - Export and import: 3 (Data operations, Backup file), 4.6, 7.
  - App-wide screen events (foreground, snackbars, one-time dialog): 3 (App shell), 6.

## 1. Overview

Source: `docs/REQUIREMENTS.md`. Design topics (derived from Must/Should features and
constraints):

1. Habit model: measurement type and direction.
2. Entries: any day, editing past days, undo of a deletion.
3. Timer: survives closing the app; ongoing notification with Stop (Should).
4. Goals and periods: fixed day/week/month/quarter/year, repeating or for a number of
   periods, evaluation per type and direction, goal completion percentage.
5. Statistics over a user-chosen date range.
6. Habit management: create, edit, archive, restore, delete.
7. Units: built-in and custom label (Should).
8. Screens and navigation (`docs/UX.md`).
9. Export / import to a file (Should), import in two modes.
10. Local, offline-only storage.
11. Localization: Polish and English, texts in resources.
12. Platform: Android only, minimum version.
13. No analytics or crash reporting (constrains library choices).
14. Extensibility: room for the "accommodate" items of `docs/REQUIREMENTS.md`
    section 6 (section 7.1).

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
- Two device flags of the notification permission (section 5.6): a small key-value
  store of the platform inside Platform services; SharedPreferences or Jetpack
  DataStore is chosen in the plan (Decision 97).
- Identities: `java.util.UUID`, available to the pure Kotlin/JVM Domain module
  (Decision 91).

To check in the official docs in the plan phase: versions of all libraries and the
minimum versions required by Compose and androidx.

## 3. Architecture

Level 1, confirmed by the user. Diagram: `docs/diagrams/components.drawio`. The
components and arrows were confirmed by the user; the arrow labels and box texts were
revised on 2026-11-24 (Domain operations, the Stop receiver, the notification sync,
Decision 71) and wait for the user to check them in draw.io. It shows App on the left
with dashed arrows (creates / wires) to every component, and solid arrows for code
dependencies.

**Module structure (Decision 1):** one Gradle module for Domain (pure Kotlin/JVM, no
Android) and one `app` module for everything else, separated by packages.

**Components:**

| Component | Responsibility |
|---|---|
| Domain | Entities and business rules: periods, goal evaluation and goal changes, statistics, timer logic (pure state based on a start instant), entry validation, export/import rules, the undo offer, the notification sync. Offers the Domain operations (below) and defines the interfaces (ports) for storage, clock, etc. No framework dependencies. |
| Persistence | Implements the storage ports and the Data operations port defined by Domain. Has its own stored representation and maps it to domain entities. |
| Platform services | Adapters for system features: timer notification and its Stop receiver (no background service, Decision 21), the notification permission state, system clock, file access and JSON serialization for export/import (Decisions 30, 36). |
| Presentation | Screen state (everything computed; texts as identifiers and raw values in simple types or types owned by Presentation, never Domain types, resolved and formatted by the GUI, Decisions 22 and 61) and handling of user actions on a screen (calls Domain operations, updates state, decides navigation). Pure Kotlin, no Android and no widgets. No business rules. Depends on Domain. |
| GUI | Compose widgets and navigation; renders state from Presentation and forwards user actions to it, without making decisions. Contains the thin androidx `ViewModel` holders, the system pickers, the permission request and the call of the per-app locale API (the language dialog, Decision 84), whose results it passes to Presentation; reports to Presentation when the app moves to the foreground and passes it the effective app language at every Activity creation. Replaceable without changes to Domain and Presentation. |
| App (composition root) | Wires everything: creates the port implementations and connects them to Domain and Presentation; calls the Domain operation "synchronize notifications" at process start. |

**Dependency direction:** `GUI -> Presentation -> Domain <- Persistence`, and
`Domain <- Platform services`. Domain depends on nothing. App knows all components;
no component knows App.

**App shell (Decision 94):** Presentation objects are per screen (Decision 9), except
one app-level Presentation object, the App shell. It lives in an Activity-scoped
`ViewModel` holder, so it survives a rotation and the recreation after a language
change, and with it a message or dialog it is showing. It alone handles what belongs to no screen: the move of the app to the
foreground (it calls the sync and shows a pending one-time message, section 5.6), the
snackbars of both Domain streams (the undo offer, section 5.7, and "timer not stopped",
section 5.5; shown above the current screen, so on the Day screen above the entries
sheet; that a snackbar can be shown above the modal sheet is checked on the prototype,
`docs/UX.md` section 8, item 4) and UI-DIALOG-NOTIFICATIONS. No screen subscribes to
these streams, so a snackbar is never shown twice.

The App shell also has a one-way channel from the screens (user decision
2026-11-24): a screen's Presentation object hands it "show this message or dialog"
when the message must outlive the screen or belongs to no screen. Through it go
UI-MSG-IMPORT-DONE (shown on the Habits screen after the import summary is closed,
`docs/UX.md` section 7) and UI-DIALOG-NOTIFICATIONS after a refusal of the permission
(section 5.6). Messages that stay on their screen come from that screen's Presentation
object (e.g. UI-MSG-TIMER-ARCHIVED on the read-only view of the archived habit,
`docs/UX.md` section 5). The shell never calls a screen.

**Domain operations (Decision 94):** Domain is used through operations named after
business actions (the Service Layer pattern, section 9). They are the only way to
change data and the timer notifications, for Presentation, for the Stop receiver and
for App alike:
- entries: add (typed value, "+", marking a binary day), edit, delete (with an undo
  offer, except unmarking on the Day row);
- timer: start, stop, stop from the notification, discard (with an undo offer);
- undo: undo the current offer, drop it (section 5.7);
- habits: save (with the goal changes), archive, restore, delete;
- backup: export, read an import file, import (*Replace all*, *Add to existing*),
  export and replace;
- notifications: synchronize notifications, the result of the permission request
  (it returns whether the one-time message must be shown), record that the one-time
  message was shown (called when the user confirms it with OK, not when it is
  displayed, so a recreation in between does not lose it; sections 5.5, 5.6).

Besides them Domain offers pure functions that Presentation uses to build screen
state: the validation of a form draft (which rules are broken), the defaults of a new
entry (section 5.8), the goal preview of the habit form (section 4.3), the date range
of a Statistics preset (BHV-STATS-R1: "6 months", "1 year", the last day of a shorter
month), the order of habits (BHV-HABIT-R4: by name with `java.text.Collator` for the
app language). The language is a tag of the effective app language that the GUI reads
from the current resource configuration (so it is also right when the user chose no
language and BHV-LANG-R1 applies) and passes to Presentation at every Activity
creation; the factories of Presentation objects hand the current tag also to objects
created later (e.g. Statistics opened for the first time). The Presentation objects
live in `ViewModel` holders that may survive the recreation, so they re-sort whenever
the tag differs from the last one. Further pure
functions give the computed values (value of a day, progress indicator,
statistics). Domain also exposes two in-memory streams:
the current undo offer (section 5.7) and "timer not stopped" events (section 5.5).

Domain runs its write operations one at a time (one lock in Domain). An operation
reads, writes through the ports and updates the notifications through the Timer
notification port before it releases the lock, so a check, its write and the
notifications that follow cannot interleave with another operation (e.g. a Stop from
the notification during the sync at process start, or during an Undo). The lock is
not reentrant: when an operation needs another one as a step (the sync after an import
or after the permission result), it runs that step as an internal step under its own
lock, not by calling the public operation.

**Objects created by Android (Decision 71):** Android creates the Activity and the
timer Stop receiver, so they cannot get dependencies through a constructor. Each of the
two components that contain them (GUI, Platform services) defines a small interface
listing what it needs: for GUI, the factories of Presentation objects; for the
receiver, the Domain operation "stop timer from the notification" (Decision 94). The
receiver never calls the Data operations port or the notification adapter directly.
The Application class in App implements both interfaces, and the Activity and the
receiver reach it by casting the application context to the interface. The dependency
still points from App to the components, never the other way.

**How the design principles are met:**
1. Business logic free of frameworks: Domain is a separate pure Kotlin/JVM module;
   storage and platform features are reached through ports defined by Domain and
   implemented in Persistence and Platform services.
2. Business logic separated from the GUI: GUI depends on Presentation only (state in,
   actions out; the state contains no Domain types, Decision 61); Presentation and
   Domain know nothing about Compose. Rules that decide what is stored (validation,
   the seconds of edited times, goal changes, the withdrawal of an undo) are in
   Domain, not in Presentation. Replacing the GUI does not touch them.
3. Patterns: see section 9 (MVVM with unidirectional state, Ports and Adapters,
   Repository, sealed hierarchy, manual dependency injection, Service Layer,
   Observer).

**Ports (Decisions 48, 59):** interfaces defined by Domain, implemented outside it.

| Port | Implemented by | Purpose |
|---|---|---|
| Habits | Persistence | Reads of habits. Every write of a habit is a Data operation. |
| Entries | Persistence | Single-entry writes: add, edit, delete (also the Binary toggle and the undo of a deletion). Entries of a habit, read by date range (never "all entries", Decision 54). A read returns every entry whose time span overlaps the range, not only entries that start in it. The range is in calendar days of a time zone that Domain passes with the query (from the Clock port), so Persistence and a test with a fake clock use the same zone (test G8, section 8). Also the date of the habit's earliest entry (for BHV-STATS-R3), in a zone passed the same way (the day of a Duration entry depends on it). |
| Goals | Persistence | Goals of a habit, archived ones too. Reads only: every goal write is part of a Data operation. |
| Running timers | Persistence | Start instants of running timers (the source of truth, Decision 18). Single writes: start a timer (also the undo of a discard, with the original start instant), discard it without an entry. Stopping is a Data operation. |
| Data operations | Persistence | Composite operations that touch several of the ports above, each all-or-nothing (Decision 59), and one consistent read, the export snapshot (Decision 95). |
| Clock | Platform services | Current instant and device time zone; a tick stream that ticks while it has a subscriber (Presentation subscribes only while a timer runs); signals of a day or time zone change (section 5.4, Decision 58); a wait until a given instant (used for the expiry of the undo offer, section 5.7; a fake clock completes it when the test moves the time). |
| Timer notification | Platform services | For a habit: show the notification with Stop, remove it, show the text of a failed Stop (UI-NOTIF-TIMER); list the habits that have a notification (for the sync, section 5.6); the permission state and the one-time message flag (section 5.6, Decision 97). It never reads timers: Domain gives it what to show. |
| Backup file | Platform services | Write the export file; read an import file in two steps, first the header (marker and format version), then the content (Decision 96). The file is identified by an opaque identifier (Decision 60). JSON parsing and serialization, mapping between file classes and domain entities (section 7). |

Rules for all storage ports:
- **Reads return `Flow`, writes are `suspend`.** Screens refresh by themselves when
  data change. Domain therefore depends on `kotlinx.coroutines`, and on nothing else
  outside the Kotlin standard library and the JDK (`java.time`, `java.math`,
  `java.text`, `java.util.UUID`). Passing time is not a data change: it reaches
  screens through the Clock port (section 5.4). Exceptions: the export snapshot and
  the reading of an import file are one-off `suspend` reads.
- **Composite operations are the methods of the Data operations port (Decision 59),
  named after business actions; each runs in one database transaction.** They are
  all-or-nothing, so a crash in the middle leaves the previous state: for the timer,
  it keeps running and Stop can be tapped again. This list is the single place that
  names them:
  - stop timer (create an entry and delete the running timer; a stop whose length is
    not above 0 writes nothing, BHV-TIMER-R2),
  - archive habit (stop its timer as above, or delete it without an entry when the
    length is not above 0, BHV-TIMER-R3; set the archiving day; archive its active
    goal with the mark "archived with its habit"; delete a pending goal;
    BHV-HABIT-R3),
  - restore habit, in one of three forms that Domain chooses (BHV-HABIT-R3): on the
    archiving day, clear the archiving day and reactivate the goal archived with the
    habit (its archiving day and its mark are removed, so a goal for N periods has the
    last day of its N-th period again; BHV-HABIT-7f); on another day, clear the
    archiving day and either create a new active goal as a copy of the goal archived
    with it ("With goal") or nothing more ("Without goal", or no such goal),
  - delete habit (the habit with its entries, goals and running timer),
  - import with *Replace all* (delete all data, write the content of the file with
    its identities) and import with *Add to existing* (write the content of the file
    as new habits with new identities, change nothing that exists; Decisions 78, 91),
  - save habit (the habit and its goal changes from the form: create, edit, and the
    archiving, replacement or creation of goals by BHV-GOAL-R4 and R4a, all in one
    operation),
  - read the export snapshot: habits, goals, entries and running timers in one
    consistent read (Decision 95).

  **Domain computes the resulting entities** (the goals after a change, the length of
  a stopped timer, the validations, the new identities of an import); a Data
  operations method only writes them in one transaction. Domain does not know the
  term "transaction". Timer notifications and the undo offer are not part of the
  transaction: Domain updates them in the same Domain operation, right after the
  write, and a notification mismatch left by a crash is repaired by the next
  notification sync (section 5.6).

**Note on iOS:** pure Kotlin Presentation and Domain make a later move easier, but
sharing them with iOS would need Kotlin Multiplatform and a change of the Domain
module type. Not designed for in v1 (out of scope); the choices only do not rule it
out.

## 4. Data model

Level 1 (names and relations):
- **Habit**: a pure definition of the activity. Direction is the habit's intent and
  exists even without a goal; it is not part of the goal (Decision 5).
- **Measurement**: how the habit is measured. A closed (`sealed`) set of variants:
  Binary, Numeric (with a unit), Duration (Decision 10). Exactly one per habit.
- **Goal**: optional (Decision 2), a separate entity (Decision 3), so changing a goal
  does not change the habit definition.
- **Entry**: a recorded value for a habit; its shape follows the habit's Measurement.
- **RunningTimer**: a habit plus the instant the timer was started.

Relations: Habit 1 - 1 Measurement; Habit 1 - 0..n Goal (at most one active,
BHV-GOAL-R3); Habit 1 - 0..n Entry; Habit 1 - 0..1 RunningTimer (several timers may
run at once, at most one per habit; Decision 18).

Level 2 (sections 4.1 to 4.6) holds the attributes that the rules in
`docs/REQUIREMENTS.md` section 7 need. **Validations** are defined by BHV-GOALVAL-R1,
BHV-ENTRY-R5, BHV-HABIT-R1 and BHV-TIMER-R2; Domain checks them, and also, on import,
every rule of BHV-IMPORT-R4.

### 4.1 Habit

Identity (section 4.6); name (BHV-HABIT-R1); direction; measurement; creation day and
time, a wall-clock date and time like a Numeric entry (set when the habit is created,
kept from the file on import; BHV-HABIT-R4, BHV-EXPORT-R1, Decision 92); archiving
day (empty while the habit is not archived; Decision 43).

Invariants: direction and measurement type never change (BHV-HABIT-R2, Decision 77);
an archived habit has no active goal and no running timer and cannot be saved through
the form, only restored or deleted (BHV-HABIT-R3, Decision 89). Archived habits keep
entries and statistics; deleting removes the habit with its entries, goals and running
timer (Decision 17). Archiving sets the archiving day; restoring clears it.

### 4.2 Measurement and units

For Numeric the unit is a label on the habit; entries hold plain numbers. Changing the
unit relabels all values, without conversion (Decision 17). A unit is data, not a
type: either a built-in one (identifier, name from string resources) or a custom one
(text label). Numeric also has an optional "+" step, stored only when the user set it;
without it the step is the default of the unit, so a changed unit gives the default of
the new unit (BHV-HABIT-R1; BHV-ENTRY-R8). The step does not change how entries are
summed (Decision 25).

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

### 4.3 Goal

Decisions 3, 7, 73 to 76, 90. A goal is a definition only; progress is computed, never
stored (Decision 54). It has an identity and belongs to one habit for its whole life.

*Attributes with business meaning:*
- target: an exact decimal for Numeric (Decision 46), a whole number of days for
  Binary, a length in whole seconds for Duration (BHV-GOALVAL-R1); "at least" or "at
  most" comes from the habit's direction, not from the goal;
- period length: day, week, month, quarter or year; a separate attribute, so a goal
  without a period can be added later (Decision 7);
- first day: a stored date. The goal starts at the beginning of the period of its
  length that contains this date (the *computed start*, BHV-GOAL-R1); in v1 the stored
  date is always that beginning (invariant 1). The rules use the computed start and
  never the stored date directly, so a later first day of the week needs no
  conversion (REQ-X5, section 7.1);
- mode: repeat, or N periods (N >= 1, BHV-GOAL-R2);
- archiving day: empty for a goal that is not archived; otherwise the day it was
  archived by a change, by switching it off or with its habit (BHV-GOAL-R4,
  BHV-HABIT-R3);
- archived with its habit: a mark set when the goal is archived by the archiving of
  its habit. "The goal archived with the habit" is the goal with this mark whose
  archiving day equals the habit's archiving day. It decides the restore
  (BHV-HABIT-R3, UI-DIALOG-RESTORE) and the ordinary block of that goal on the
  Statistics card of an archived habit (`docs/UX.md` section 6);
- creation day: the day the goal was created; also the day it was replaced in place
  (BHV-GOAL-R4a) or created as a copy by a restore; kept from the file on import
  (BHV-IMPORT-19). It decides "created today" in BHV-GOAL-R4a;
- the order of addition (section 4.6).

*Derived, not stored:*
- last day: the archiving day when there is one; otherwise, for a goal for N periods,
  the last day of its N-th period counted from the computed start (BHV-PERIOD-R2);
  otherwise none;
- status: *archived* (it has an archiving day); *ended* (a goal for N periods whose
  last day is before today: it is archived by BHV-GOAL-R2 without any write, so
  nothing has to run on the day after); *pending* (neither, and the computed start is
  after today); *active* otherwise. The "active goal" of the requirements is the
  active or pending one; an ended goal counts as archived everywhere (the form,
  statistics, BHV-PERIOD-4).

*Invariants:*
1. in v1 the stored first day is the first day of a calendar period of the goal's
   length (BHV-IMPORT-R4); a later first day of the week relaxes this (section 7.1);
2. the target satisfies BHV-GOALVAL-R1 for the type and direction of the habit;
   N >= 1;
3. an archiving day is not before the computed start; for a goal for N periods it is
   not after the last day of its N-th period. An imported archived goal for N periods
   whose last day in the file is after the last day of its N-th period is accepted,
   and its last day is the last day of its N-th period (BHV-IMPORT-R4; user decision
   2026-11-24, question 26); it is stored with that day as its archiving day;
4. a habit has at most one goal that is neither archived nor ended (BHV-GOAL-R3), and
   an archived habit has none. In the stored data: at most one goal of a habit has no
   archiving day. Because a goal for N periods ends without a write, every Data
   operation that writes goals of a habit (save habit, archive habit, import) also
   gives an ended goal its own last day as the archiving day, which changes nothing it
   means. An import accepts such an ended goal without an archiving day
   (BHV-IMPORT-R4 counts only active goals) and stores it the same way.

*Changes:* the outcome of a Save is defined by BHV-GOAL-R4, R4a and R6. What the model
adds: Domain computes it from the stored goals, today and the form values; a goal
replaced in place gets today as its creation day; an archived goal gets today as its
archiving day and the new goal a new identity. The same computation gives the habit
form its preview: the start and the last day the new goal would get and whether the
confirmation of BHV-GOAL-R6 is needed (Decision 89).

*Order among goals:* BHV-DAY-R2 ends its tie-break with "the one archived later". Goals
created in the app are archived in the order they were added, since a habit has at
most one active goal at a time, so the order of addition (section 4.6) answers it:
for goals archived on the same day, the one added later counts as archived later (for
imported goals, later in the file), and the goal blocks of a Statistics card are listed
with the newest goal first (BHV-DAY-R2, `docs/UX.md` section 6; user decision
2026-11-24, question 24).

### 4.4 Entry

Decision 92. A closed set of shapes, one per measurement type, so the shape of an
entry always matches the type of its habit (BHV-IMPORT-R4). Every entry has an identity
and belongs to one habit.
- Binary: a local date, not an instant (Decision 44); at most one per habit and day
  (BHV-ENTRY-R1).
- Numeric: a value (an exact decimal above 0 with 3 decimal places, Decision 46) and a
  wall-clock date and time to the second (Decision 80). It is kept as entered and not
  recomputed after a time zone change (BHV-ENTRY-R2); its day is its date. Several per
  day, summed.
- Duration: a start instant and a length in whole seconds, above 0. The start is kept
  to the second: the start instant of a timer is cut to the second when its entry is
  created, while the length is computed from the exact start (BHV-TIMER-R2). It can
  span midnight; the split per day is computed by Domain, not stored (Decision 12,
  section 5.3). Several per day. The day of a part is a calendar day in the device
  time zone (Decision 13, BHV-ENTRY-R2).
- Times entered by hand are to the minute; which seconds an edited time keeps and how
  a local time at a clock change becomes an instant: section 5.8 (BHV-ENTRY-R6b).

### 4.5 RunningTimer

A habit and the exact start instant; only for a Duration habit that is not archived; at
most one per habit (Decision 18, BHV-IMPORT-R4). It is identified by its habit.

### 4.6 Identities and order

**Identities (Decision 91):** every habit, goal and entry has an identity that Domain
creates with the entity: a random UUID, never reused, written to the export file.
*Replace all* keeps the identities of the file; *Add to existing* gives every imported
habit, goal and entry a new identity and maps the references, so importing a file
twice creates distinct habits (BHV-IMPORT-14). Stable identities are what a later
merging import needs (REQ-X9).

**Order of addition (Decision 92):** Persistence keeps the order in which habits,
goals and entries were added. It is the last tie-break of three orders: habits with
equal names and equal creation day and time (an existing habit comes before an
imported one, BHV-HABIT-R4), entries with equal times (BHV-ENTRY-R6b) and goals in
BHV-DAY-R2 (section 4.3). An import adds in the order of the file, and the export
writes in this order.

## 5. Mechanisms

The behavior itself is defined in `docs/REQUIREMENTS.md` section 7. This section
holds only what the design decides about how it is achieved.

### 5.1 Periods, day and week parameters

The first day of the week and the time zone are parameters of the period and day
calculations, supplied via the Clock port, not constants in the rules (Decision 11;
REQ-X5: a user setting for the first day of the week needs no conversion of stored
data). Periods, results, percentages and "achieved" are computed from entries and
goals, never stored (Decision 54). One Domain function gives the counting periods of a
goal (BHV-GOAL-R5) and is used by the evaluation, the Day indicator and the statistics
(Decision 98).

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
the stored instants. The same split decides on which days a running timer is listed in
the entries sheet (BHV-TIMER-R1).

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

### 5.5 Timer, notification and the Stop receiver

No background service (Decision 21). Domain holds the start/stop logic (length = now
minus the stored start instant, rounded to the nearest second, half up, BHV-TIMER-R2);
Persistence holds the start instant; Platform services implements the Timer
notification port. The notification is an ongoing system notification whose built-in
chronometer displays the elapsed time from the start instant (`setUsesChronometer`
with `setWhen`, section 10); the app never reads state from it. There is one
notification per running timer (Decision 32).

**Stop from the notification (Decisions 71, 94):** the Stop button sends a broadcast,
one per habit, to the Stop receiver in Platform services. Through its interface
implemented by the Application class, the receiver gets the Domain operation "stop
timer from the notification" and runs it asynchronously (`goAsync`; the system expects
a receiver to finish within about 10 seconds, section 10). The operation is "stop
timer" plus what only a Stop from the notification needs, all inside the Domain lock
(section 3):
- stopped: the Data operation writes the entry and deletes the timer; Domain removes
  the notification and withdraws a conflicting undo offer (section 5.7);
- not stopped (the length is not above 0): nothing is written and the timer keeps
  running; Domain asks the Timer notification port to show the text of UI-NOTIF-TIMER
  for that habit (the adapter posts the notification again with the same identifier,
  section 10). The text stays until the next Stop. A failed Stop in the app does not
  change the notification text (`docs/UX.md` section 4 describes the text only for the
  notification);
- no running timer (a notification left over): Domain removes the notification.

Since the text is set inside the lock, a Stop in the app that succeeds at the same
moment cannot be followed by a "not stopped" text on a removed notification.

A failed "stop timer" or "stop timer from the notification" is also published by Domain
on its "timer not stopped" stream; while the app is open, the App shell (section 3)
shows UI-MSG-TIMER-NOT-STOPPED above the visible screen (Observer through `Flow`,
Decision 94). A stream without a subscriber drops the event. Only these two
operations publish on it.

The texts of the notification are built by the Platform services adapter. If the plan
shows that they do not follow the per-app language (`docs/UX.md` section 8, item 1),
the adapter builds them with a context configured to the app language; the change
stays inside Platform services.

Archiving or deleting a habit with a running timer, and a stop with a non-positive
length, are behavior (BHV-TIMER-R2, R3); archiving and deleting are Data operations
(section 3) and the notification is removed in the same Domain operation. Archiving
stops the timer even when its length is not above 0 (it is deleted without an entry):
the result of the Domain operation "archive habit" says so, and the habit form, which
then becomes the read-only view of the archived habit (UI-HABITFORM-ARCHIVED-VIEW,
`docs/UX.md` section 5), shows UI-MSG-TIMER-ARCHIVED there; nothing is published on the "timer not stopped" stream,
since the timer no longer runs.

### 5.6 Notification sync and permission

The notifications follow the running timers (BHV-TIMER-R5, Decision 57). The Domain
operation "synchronize notifications" runs inside the Domain lock (section 3): it reads
the running timers through the Running timers port (and the names through the Habits
port), asks the Timer notification port which habits have a notification, shows one
for every running timer that has none and removes every notification whose habit has
no running timer. Without the permission nothing can be shown, so it only removes. It
is idempotent. It is called:
- by App at every process start (also one started by the Stop button of a
  notification);
- by the App shell (section 3) when the GUI reports that the app moved to the
  foreground: the notification comes back every time the app is opened or returns to
  the foreground (BHV-TIMER-R5, BHV-NOTIF-1; user decision 2026-11-24, question 23);
- by Domain after an import, and after the permission request when the permission was
  granted; there it is an internal step under the lock of the calling operation
  (section 3).
App and the App shell call the public operation.
A later boot receiver (an extra component and permission) would be one more caller
(section 10, question 7). Starting a timer and the undo of a discard show the
notification of that timer directly; stopping, discarding, archiving and deleting
remove it; all of them inside their Domain operation.

**Permission and the one-time message (BHV-TIMER-R5, Decision 97):** Android does not
tell "never asked" from "refused", so Platform services keeps two device flags in a
small key-value store outside the database (not user data, not exported,
BHV-EXPORT-R1, and not touched by *Replace all*): the permission was requested once;
the one-time message is pending or was shown. The Timer notification port gives Domain
these flags and whether the permission is granted.
- A tap on Start starts the timer at once (the timer works without the permission,
  BHV-TIMER-R5). If the permission was never requested, the Domain operation tells
  the Day screen's Presentation to ask; the GUI runs the system request and
  Presentation passes the result to the Domain operation, which records "requested".
  When the permission was granted, the operation runs the sync, so the timer started
  before the answer gets its notification at once (BHV-NOTIF-5). After a refusal the
  operation returns "show the one-time message"; the Day screen's Presentation hands
  UI-DIALOG-NOTIFICATIONS to the App shell through its channel (section 3), which
  shows it at once, and Domain records it as shown (BHV-NOTIF-3). Returning from the
  system dialog is not an Activity start, so the foreground signal is not what shows
  it.
- After an import with running timers while the permission was never requested,
  Domain marks the message pending without asking. The App shell shows it the next
  time the app is opened or returns to the foreground and Domain records it as shown
  (BHV-NOTIF-4).
- Once shown, the message is never shown again.

### 5.7 Undo of a deletion and of a discarded timer

The behavior is BHV-ENTRY-R6a and BHV-TIMER-R1 (Decision 86); the mechanism is
Decision 93. Domain keeps the current undo offer in memory with an expiry instant: the
instant of its creation from the Clock port plus a fixed time defined in Domain ("a
few seconds"; the value is set in the plan). Domain is the only clock of the offer:
at the expiry instant (a wait on the Clock port) it emits "no offer" on its stream.
The Clock adapter turns the wait into a delay of the remaining time, measured on the
monotonic clock of the system, so moving the device clock does not shorten or lengthen
it. After the process ends there is nothing to undo.

There is at most one offer; a new deletion or discard replaces the previous offer and
its snackbar (`docs/UX.md` section 4; user decision 2026-11-24, question 22).

- Deleting an entry from the entries sheet creates an offer with a copy of the entry;
  discarding a timer creates one with its habit and its start instant. Unmarking a
  binary entry on the Day row creates none (BHV-ENTRY-R6a, BHV-ENTRY-22d).
- When the wait ends, Domain drops the offer (under the Domain lock) and the stream
  emits "no offer" (also while the app was in the background, so the snackbar does not
  come back hours later). An offer that still exists is valid: an Undo checks only
  that it exists, never the expiry instant against the device clock, so moving the
  clock forward cannot make a visible Undo do nothing.
- Domain withdraws the offer inside the write operation that conflicts with it,
  whoever calls the operation (the app or the Stop receiver):
  - an entry offer when the habit gets a new entry on a day the deleted entry covers:
    adding, "+", marking, or a stopped timer whose entry covers that day
    (BHV-ENTRY-22c; "a day that the deleted entry covers", BHV-ENTRY-R6a);
  - a timer offer when a new timer of the habit is started (BHV-TIMER-10c);
  - both when the habit is archived or deleted, and every offer on an import with
    *Replace all* (it replaces all data). *Add to existing* changes no existing habit
    and keeps the offer.
- Undo writes the copy back with its identity and values through the Entries port
  (BHV-ENTRY-22b), or starts the timer again with its original start instant through
  the Running timers port and shows its notification (BHV-TIMER-10b). The check that
  the offer still exists and the write are one Domain write
  operation (section 3), so no other operation can slip in between.
- Domain exposes the offer as a stream. The App shell (section 3) shows
  UI-MSG-UNDO-ENTRY or UI-MSG-UNDO-TIMER exactly while the stream holds an offer, also
  after the entries sheet is closed: the snackbar has no timeout of its own (an
  indefinite duration) and is hidden when the stream emits "no offer" (expiry,
  withdrawal, a new offer or Undo). So the snackbar and the offer cannot disagree, also
  after the Activity is recreated, and an "Undo" that is visible always works. The
  user can still dismiss the snackbar; the shell then asks Domain to drop the offer.

### 5.8 Entry times and "changed"

BHV-ENTRY-R5 and R6b decide what is stored, so Domain applies them, not Presentation
(Decision 92). Presentation only tracks which fields are *touched* (`docs/UX.md`
section 1) to decide whether a message is shown; the broken rules come from the Domain
validation of the draft, also for the OK button of the time picker
(UI-ENTRYFORM-TIME-OK).
- Defaults of a new manual entry: now cut to the whole minute on today's date, 12:00
  on a past date (`docs/UX.md` section 4, BHV-ENTRY-24). "+" stores now with its
  seconds on today's date and 12:00:00 on a past date (`docs/UX.md` section 3).
- On Save of an edited entry Domain compares each time of the form (its date, hour and
  minute) with the stored one: for a Numeric entry the stored wall-clock date and time
  as they are; for a Duration entry the stored start and end instants shown in the
  device time zone. A time whose hour and minute are unchanged keeps its stored
  seconds, also when its date changed; a changed time gets 0 seconds (BHV-ENTRY-23a,
  23b, 23c, 23d). The length of a Duration entry is the end minus the start.
- For a Duration time the form keeps the time as a local date, hour and minute plus
  its offset from UTC, and "changed" compares all four with the stored instant (the
  offset is part of "changed" in BHV-ENTRY-R6b and `docs/UX.md` section 1). A
  time that is not changed keeps its stored instant exactly; it is never recomputed
  from the local time. This matters at a clock change: an entry 02:10-02:50 in the
  second, repeated 02:xx hour of 25 Oct 2026 in Europe/Warsaw whose end alone is
  changed to 02:55 keeps its start and is 45 min long (BHV-ENTRY-26).
- The quick buttons (-5 min ... +1 h, `docs/UX.md` section 4) move the instant by
  that length, not the local time, so "earlier" is always earlier: 03:00 on 29 Mar
  2026 in Europe/Warsaw minus 15 min is 01:45 local (the hour 02:xx does not exist).
  Because the offset is compared too, +1 h on an end of 02:50 in the first 02:xx hour
  of 25 Oct 2026 gives 02:50 in the second one and counts as changed (the length grows
  by 1 h), although the hour and minute look the same. Like any changed time, the
  result gets 0 seconds.
- A time typed or picked by the user becomes an instant in the device time zone as a
  `java.time` local date-time with a preferred offset (e.g. `ZonedDateTime.ofLocal`;
  the exact call is checked in the plan): the preferred offset is the one the field
  already has (from the stored time or the time before the change), and it is kept
  when it is valid for the new local time. So the end of an entry 02:10-02:50 in the
  second, repeated 02:xx hour of 25 Oct 2026 in Europe/Warsaw typed as 02:55 stays in
  the second hour and the length is 45 min (BHV-ENTRY-26). Otherwise the default rule
  applies: a time that does not exist (the clocks go forward, e.g. 02:30 on 29 Mar 2026
  in Europe/Warsaw) is moved forward by the length of the gap (03:30, BHV-ENTRY-25);
  a time that occurs twice in a field without a valid offset (e.g. a new entry) takes
  the earlier offset. A Numeric entry keeps its wall-clock time as entered; it becomes
  an instant only to be compared with now, by the default rule.
- A moment is checked against now only when the entry is added, or the moment's date
  or its hour and minute changed; a value set back to the stored one is not changed
  (BHV-ENTRY-21a, 21d). The other rules (length above 0, value above 0) always apply.

## 6. User interface

Screens, navigation and layout: `docs/UX.md`. What the architecture requires of them:
the GUI renders the state of Presentation and forwards actions without making
decisions; texts are identifiers resolved by the GUI (section 3, Decisions 22, 61).
- The Quit ring of a Duration habit (UI-DAY-INDICATOR, `docs/UX.md` section 3):
  Presentation computes the numbers to show (the result cut down to the minute within
  the limit and rounded up above it, to the second when the limit has seconds or is
  under 1 min) as plain values from the Domain result; the GUI only writes them with
  the compact units. Binary and Numeric show the exact numbers.
- The read-only view of an archived habit (UI-HABITFORM-ARCHIVED-VIEW) is a state of
  the habit form in Presentation; Domain refuses to save an archived habit (section
  4.1).
- The system pickers (file, save dialog, dates) and the permission request run in the
  GUI; their results reach Domain through Presentation (Decision 60). The GUI also
  reports the move of the app to the foreground (the start of its single Activity) to
  the App shell, which runs the sync and shows a pending one-time message (section
  5.6).
- Snackbars of the Domain streams (undo, "timer not stopped"), UI-DIALOG-NOTIFICATIONS
  and the messages a screen hands over through the channel of section 3 (at least
  UI-MSG-IMPORT-DONE) are shown by the App shell only; snackbars that stay on the screen
  of their action (e.g. UI-MSG-TIMER-ARCHIVED on the read-only view, UI-MSG-EXPORT-DONE
  on the Habits screen) come from that screen's Presentation object.
- The app language: the GUI calls the per-app locale API from the language dialog
  (Decision 84) and passes the effective language tag to Presentation at every Activity
  creation; Presentation re-sorts the habits when the tag changes (section 3,
  BHV-HABIT-R4, BHV-HABIT-16).

## 7. Cross-cutting concerns

- **Localization (Decision 22):** Presentation exposes text identifiers and raw
  values (durations, dates, numbers, unit identifiers) in simple types or types owned
  by Presentation, not Domain types (Decision 61); the GUI resolves them from Android
  string resources and formats them for the locale. Adding a language means adding
  resource files only. Built-in unit names come from resources; a custom unit is a
  plain label. The behavior of the language choice is BHV-LANG-R1 to R3; where the
  switch is in `docs/UX.md` section 5 (Decision 84).
- **Timer in the background:** see section 5.5 (Decision 21).

- **Export / import format (Decision 30):** a JSON file that starts with its header: a
  marker that it is a backup of this app and a format version number. The content is
  the export date and, as BHV-EXPORT-R1 lists them, habits (with their identities and
  their creation day and time), goals (archived ones too, with the creation day, the
  archiving day and the mark "archived with its habit"), entries and running timers,
  all with the identities that link them (Decision 91), in the order of addition
  (section 4.6). It is described by file-model classes owned by the Platform services
  adapter (Decision 36), not by the stored representation of Persistence. The adapter
  maps between the file and domain entities. Domain owns the format rules: it holds
  the current format version and checks that a file's version is supported and that
  its data satisfy BHV-IMPORT-R4 (Decisions 49, 96). Domain has no separate export
  model. Changing the persistence library or its schema does not invalidate old
  files. Export to CSV or other formats is not planned (add to "Ideas for later" if
  wanted).

- **Export (Decision 95):** the Domain operation reads the export snapshot (one
  consistent read of all data through the Data operations port), adds the export date
  from the Clock port and gives it to the Backup file port, which writes it to the
  file chosen in the system save dialog. The port reports success only when the whole
  file was written. Reading everything at once means an entry after today
  (BHV-ENTRY-R3, BHV-ENTRY-21a) is exported, and a Stop from the notification cannot
  fall between two reads. A failed export changes no data (BHV-EXPORT-3).

- **Import (Decisions 49, 60, 78, 96):** the file is chosen with the system picker in
  the GUI, which passes an opaque string identifier of the file through Presentation
  and Domain to the Backup file port; only Platform services can interpret it.
  1. The port reads the header with a streaming read of the first fields, so a backup
     cut off later still has its header. No header with the marker of this app (e.g.
     a photo or another JSON file): rejected as "not a backup of this app"
     (BHV-IMPORT-9b). Domain
     checks the version: a newer one is rejected as "from a newer version of the app"
     (a newer format cannot be mapped by an older app, so the content is not read).
  2. The port maps the content of a supported version (older versions included,
     BHV-EXPORT-2); content that cannot be parsed, also a file cut off after its
     header, is rejected as "damaged" (BHV-IMPORT-9a). Domain checks BHV-IMPORT-R4 and that the
     identities in the file are unique (for the last day of an archived goal for N
     periods see invariant 3 in section 4.3); a broken rule is also "damaged"
     (UI-DIALOG-IMPORT-REJECTED, BHV-IMPORT-R1).
  3. Domain returns the summary (export date, number of habits and entries, and how
     many habits of the file have the same name as existing ones, compared ignoring
     case and spaces at the ends; BHV-IMPORT-R2, UI-IMPORT-SUMMARY) and the validated
     content (the import candidate), which Presentation keeps until the user chooses.
  4. The chosen mode passes the candidate to the Domain operation of that mode, which
     calls the Data operation *Replace all* or *Add to existing* (section 3). After a
     successful import, in the same Domain operation, Domain runs the notification
     sync, marks the one-time message when needed (section 5.6) and withdraws the
     undo offers that *Replace all* invalidates (section 5.7).
  A failure while writing leaves the data unchanged (BHV-IMPORT-R3, BHV-IMPORT-20).

- **Export and replace (Decisions 88, 96):** the button of UI-DIALOG-IMPORT-REPLACE
  opens the system save dialog in the GUI. If the user cancels it, nothing is
  replaced and UI-MSG-IMPORT-NOT-REPLACED is shown (BHV-IMPORT-5c). Otherwise
  Presentation calls the Domain operation "export and replace" with the chosen file and
  the import candidate: it runs the export and, only after the file was written, the
  *Replace all* import, holding the Domain lock from the snapshot to the end of the
  replacement, so no Stop or other write can fall between the backup and the
  replacement and be lost (a Stop from the notification waits; the data volume is
  small, Decision 54, and the plan checks that the whole operation stays well within
  the time a receiver may take, section 10, question 7). A failed export replaces nothing (UI-MSG-IMPORT-NOT-REPLACED,
  BHV-IMPORT-5d); a failed replacement leaves the data unchanged and the backup in
  place (UI-MSG-IMPORT-FAILED, BHV-IMPORT-5e). The two steps are not one transaction
  (the file is outside the database) and need not be: the export is complete before
  anything is replaced.

- **System backup (Decision 33):** Android Auto Backup (cloud backup and
  device-to-device transfer) is disabled for the app, so data leave the device only
  through a user-initiated export (REQ-C5). Restoring on a new phone requires an
  earlier export. Settings, verified in the official Android docs on 2026-11-24: in
  the manifest `android:allowBackup="false"` and `android:dataExtractionRules`
  pointing to rules whose `<cloud-backup>` and `<device-transfer>` sections exclude
  every domain (`root`, `file`, `database`, `sharedpref`, `external` and the `device_`
  ones). Both sections are needed: a missing section leaves that kind of transfer
  fully enabled, and on Android 12 and higher `allowBackup="false"` alone does not stop
  device-to-device transfer on some devices. The older `fullBackupContent` rules are
  not needed (minimum API 33). Still to check: section 10, question 21.

- **Local storage:** all data stay on the device in Room (Decision 35), behind the
  storage ports; the two notification flags are in a small key-value store of
  Platform services (Decision 97); no network access at all.

### 7.1 Extensibility: the "accommodate" items (REQ-X1 to X6)

`docs/REQUIREMENTS.md` section 6 asks the design to name, for each "accommodate" item,
what a later version changes. The seams (Decision 98) are named now; nothing is built
for them in v1. A later change of the stored data adds attributes with a default that
means "as in v1" and raises the export format version, so files and data of v1 keep
their meaning (BHV-EXPORT-R1).

| ID | What v1 already does | What a later version changes |
|---|---|---|
| REQ-X1 Parallel goals | Goals are separate entities with their own identity and history; every goal is evaluated on its own (BHV-GOAL-R3) and statistics are shown per goal (BHV-STATS-R4); the Day indicator already chooses among several goals that count a period (BHV-DAY-R2). | Relax invariant 4 of the Goal (section 4.3) in the save habit and import checks; the habit form shows several active goals; a rule for a change of one of them. Stored goals of v1 stay valid: a habit with one active goal is a special case. |
| REQ-X2 More goal kinds | The period is a separate attribute of the goal (Decision 7), and one Domain function gives the counting periods of a goal (section 5.1). | The period becomes a closed set of kinds (calendar period in v1; custom interval, set of dates, range of dates, no period); each new kind implements the counting periods. Goals of v1 are read as the calendar kind, so their history and statistics do not change. |
| REQ-X3 Weekday schedule | Every habit is shown every day (BHV-HABIT-R4), and one Domain function gives the days of a habit's life (BHV-STATS-R3), used by the averages and by statistics. | An optional schedule on Habit; empty means every day, exactly v1. The Day list and the days of the habit's life filter by it; "missed" days in a calendar come from the same function. |
| REQ-X4 New measurement types | Measurement is a closed `sealed` set (Decision 10) and entries have one shape per type (section 4.4). | A new variant of Measurement and a new entry shape; the compiler shows every place that must handle it. Existing types, their data and their files are not touched; the export format version adds the new shape. |
| REQ-X5 First day of the week | The first day of the week is a parameter from the Clock port (Decision 11); periods and results are computed, never stored (Decision 54); the rules use a goal's computed start, never its stored first day directly, and "pending" is decided by the computed start (section 4.3). | A user setting passes another first day of the week. Invariant 1 of the Goal is relaxed for weekly goals: the stored first day may be any date, and the start is the beginning of the week that contains it under the setting; the import check of a goal's first day (BHV-IMPORT-R4) changes the same way, so goals and files of v1 stay valid. No stored data are converted. |
| REQ-X6 Calendar, colours and icons | The Entries port reads by date range with overlapping spans (section 3), and Domain splits Duration entries per day (section 5.3), so "on which days did this habit have entries" is a computation over a range. The date header is the single entry point to a date (`docs/UX.md` section 2). | A calendar screen replaces the date picker and asks Domain for the days with entries in the shown month. Optional display attributes (colour, icon) on Habit, empty for existing habits; the Day row gets a leading element. |

The "not blocked" items (REQ-X7 to X15) need nothing beyond what is stated where they
touch the design: e.g. REQ-X9 relies on the stable identities (section 4.6) and the
versioned file, REQ-X7 on entries that may already lie after today (BHV-ENTRY-R3).

## 8. Testing strategy

Decision 38. Tests follow the components; the weight is on logic. Detailed test cases
belong to the plan.

- **Domain (JVM unit tests):** every scenario of `docs/REQUIREMENTS.md` section 7 that
  concerns Domain logic is one test (the rows about UI, persistence and notifications
  are tested at their level or by hand); besides that periods (day to year), goal
  change and the goal preview, the status of a goal (an ended goal for N periods, the
  normalization of invariant 4), goal evaluation, completion percentage and its display
  rounding, exact decimal sums (0.7 + 0.1 reaches a target of 0.8), statistics,
  splitting a duration entry across days (also a running timer), timer logic and the
  results of "stop timer" and "stop timer from the notification", the undo offer, its
  expiry and its withdrawal (BHV-ENTRY-22a to 22d, BHV-TIMER-10a to 10c, also a
  withdrawal by a Stop from the notification), the notification sync with interleaved
  operations (a Stop from the notification during the sync at process start, a timer
  started right after it) and after a granted permission (a timer started before the
  answer gets its notification, BHV-NOTIF-5), the seconds of edited times
  (BHV-ENTRY-23a to 23d, BHV-ENTRY-24), a manual entry at a missing or doubled local
  time (BHV-ENTRY-25), an edited entry in the second, repeated hour of 25 Oct 2026
  whose end is typed as 02:55 (the start keeps its instant, the end keeps the offset
  of the second hour, length 45 min, BHV-ENTRY-26), a quick button across the gap of
  29 Mar 2026 (-15 min from 03:00 gives 01:45), +1 h on an end of 02:50 in the first
  repeated hour of 25 Oct 2026 (the end moves to the second 02:50, the length grows by
  1 h), the offer stream emitting "no offer" at the
  expiry instant of the fake clock, archiving a habit whose timer has a non-positive
  length (result "stopped without an entry", nothing on the "timer not stopped"
  stream), the order of habits with a Polish and an English collator (BHV-HABIT-16),
  the preset ranges (BHV-STATS-R1), the three forms of restore, import modes (new identities and
  order on *Add to existing*, BHV-IMPORT-11, 14), the order of "Export and replace"
  with a failing fake file port (BHV-IMPORT-5b to 5e), export file format. Ports are
  replaced by simple hand-written fakes (in-memory repositories, a clock with a fixed
  time and time zone and with ticks and day changes triggered by the test, Decision
  13); no mocking library. A fake must behave like its port as specified in section 3
  (e.g. the Entries fake returns entries whose span overlaps the range).
- **Presentation (JVM unit tests):** screen state and reactions to actions, with the
  same kind of fakes; "touched" and the messages; the App shell: snackbars driven by
  the undo offer and the "timer not stopped" stream (the undo snackbar disappears at
  the expiry of the offer, shown once although several screens are open), the
  foreground signal and the pending one-time message, the channel from the screens
  (UI-MSG-IMPORT-DONE after the import summary is closed; after a refusal of the
  permission UI-DIALOG-NOTIFICATIONS is shown once, BHV-NOTIF-3); a change of the
  language tag re-sorts the habits (BHV-HABIT-16).
- **Persistence (instrumented, device or emulator):** Room with an in-memory database,
  mapping between stored and domain classes, schema migrations, the all-or-nothing
  Data operations, the order of addition.
  - **G8 (Entries port):** an entry from 23:30 on 10 Nov to 00:20 on 11 Nov; the port
    reads the range 11 Nov - 11 Nov in Europe/Warsaw. The entry is returned (its span
    overlaps the range), so the 20 minutes fall on 11 Nov.
- **Export / import:** a round-trip test (export, import, same data; BHV-EXPORT-1),
  including an entry after today and an ended goal for N periods; sample files from
  older format versions (BHV-EXPORT-2); a rejected invalid file leaves data unchanged
  (BHV-IMPORT-R1); a backup cut off after its header is "damaged" (BHV-IMPORT-9a), a
  photo is "not a backup of this app" (BHV-IMPORT-9b).
- **GUI and Platform services:** manual checks (notification, Stop button with the app
  open and closed, the changed text of a failed Stop, timer surviving closing the app,
  the permission request and the one-time message, a dismissed notification coming
  back when the app returns to the foreground). No automated UI tests in v1; they can
  be added later without changing the architecture.

**Success criteria** (`docs/REQUIREMENTS.md`, section 4):
- No data loss: Persistence and round-trip tests, plus the week of real use.
- Statistics match a manual calculation: a Domain test replays the three-habit week
  (a duration build habit with a timer and a past-day entry, a numeric build habit
  with a daily goal, a binary quit habit with a limit of 3 days per week) with
  hand-computed expected results.
- Goals evaluated correctly: the same tests plus edge cases:
  - result equal to target, target 0 (BHV-EVAL-4a, 4b, 5a, 5b);
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
| Sealed hierarchy (sum type) | `Measurement`, the entry shapes | See Decision 10; the entry shape follows the type (Decision 92). | Enum plus optional fields, Strategy: see Decision 10. |
| Ports and Adapters (dependency inversion) | Domain defines ports; Persistence and Platform services implement them | Required by principle 1: dependencies must point to Domain. See Decisions 48 and 66. | Domain calling Room or Android directly breaks principle 1. |
| Dependency Injection (manual, constructor) | App wires all components | Components receive their ports through constructors; only App knows concrete classes (principle 1). See Decision 36. | Service locator hides dependencies; Hilt / Koin add configuration the app does not need. |
| Repository | Shape of the storage ports | Domain sees a collection of entities, not a database. See Decision 66. | Ports shaped like SQL queries leak persistence details into Domain. |
| Service Layer (application services) | The Domain operations: named business actions, the only entry point for writes and notification changes, run one at a time | One place decides what an action does (rules, ports, notifications, undo), for the app, the Stop receiver and App alike. See Decision 94. | Callers composing ports themselves (the receiver calling the Data operations port, as before Decision 94) spread the rules and allow races; a generic command bus adds ceremony. |
| Observer (through Kotlin `Flow`) | Domain streams of the undo offer and of "timer not stopped", observed by the App shell (Presentation) | Domain must tell the open app about something that changes no data (a failed Stop from the notification) or that is not stored (the undo offer). See Decision 94. | Callbacks registered by Presentation duplicate what `Flow` already gives; a broadcast inside the app is discouraged by the Android docs; polling wastes work. |

## 10. Open questions

The numbers are stable and never reused.

**Carried to the plan phase** (none changes the structure of the design). By the
user's decision of 2026-11-24 the checks below are the first tasks (spikes) of the
plan:
- the language of the timer notification texts (`docs/UX.md` section 8, item 1; the
  fallback is in section 5.5);
- the API that lists the app's shown notifications, for the sync (question 7,
  section 5.6);
- the widths on a phone prototype (`docs/UX.md` section 8, item 3);
- a snackbar of the App shell above the modal bottom sheet (`docs/UX.md` section 8,
  item 4).

12. App name (inherited from requirements; the user's choice).
21. Auto Backup: the settings are verified and in section 7 (2026-11-24). Left: the
    official page does not say whether the cross-platform transfer to iOS (Android 16
    QPR2, `<cross-platform-transfer>`) is on when the section is missing; check it in
    the plan and, if so, exclude every domain there too.

**Resolved**

7. Verified in the official Android docs on 2026-11-24:
   - chronometer: `NotificationCompat.Builder.setUsesChronometer(true)` shows the
     `when` field, set with `setWhen` to the start instant, as a stopwatch;
   - dismissal: on Android 13 an ongoing notification (`setOngoing(true)`) cannot be
     dismissed; from Android 14 the user can dismiss it, except when the phone is
     locked or with "Clear all". Both are handled by BHV-NOTIF-1 and the sync;
   - updating the text: calling `notify()` again with the same identifier updates the
     notification (if it was dismissed, a new one is created). Item 2 of `docs/UX.md`
     section 8 is marked verified on this basis;
   - receivers: `onReceive` runs on the main thread; `goAsync()` lets the work continue
     on another thread, and the system expects it to finish in under 10 seconds;
   - the later boot receiver (not v1, Decision 57): `ACTION_BOOT_COMPLETED` is exempt
     from the limits on manifest-declared receivers. The pages read do not name its
     permission; check it when the receiver is added.
   Not checked yet: the API that lists the app's shown notifications for the sync
   (section 5.6); the plan names it after checking the official docs.
16. Look of the progress indicator and symbol of the quit action control: settled in
    `docs/UX.md` section 3 (Decision 82).
18. Month calendar: in "Ideas for later" (REQ-X6, `docs/UX.md` section 2).
22. Undo offers (answered by the user 2026-11-24): one offer at a time, a new deletion
    or discard replaces it; "that day" of BHV-ENTRY-R6a is every day the deleted entry
    covers. Written into `docs/UX.md` section 4 and BHV-ENTRY-R6a; design: section 5.7.
23. "The next start of the app" (answered 2026-11-24): every time the app is opened or
    returns to the foreground, for the notification and for the one-time message.
    Written into BHV-TIMER-R5, BHV-NOTIF-1, BHV-NOTIF-4; design: section 5.6.
24. "Archived later" and the order of goal blocks (answered 2026-11-24): for goals
    archived on the same day, the one added later; goal blocks on a Statistics card
    newest first. Written into BHV-DAY-R2 and `docs/UX.md` section 6; design: section
    4.3.
25. Scenario rows (answered 2026-11-24): all added to `docs/REQUIREMENTS.md` as
    BHV-NOTIF-5, BHV-IMPORT-9a, BHV-IMPORT-9b, BHV-ENTRY-25 and BHV-ENTRY-26; cited in
    sections 5.6, 5.8, 7 and 8.
26. An imported archived goal for N periods with a last day after its N-th period
    (answered 2026-11-24): the file is accepted, and the goal's last day is taken as
    the last day of its N-th period. Written into BHV-IMPORT-R4; design: section 4.3,
    invariant 3.

Also answered by the user on 2026-11-24, in the review of the second design loop: the
App shell accepts "show message or dialog" from the screens, one way only (section 3);
after "Archive" the habit form becomes the read-only view of the archived habit, where
UI-MSG-TIMER-ARCHIVED is shown (`docs/UX.md` section 5; section 5.5).

Not resolved by the docs and still open in `docs/UX.md` section 8, item 1: whether the
texts of the timer notification follow the per-app language. The official page on
per-app languages says that the framework API applies the locale and restarts the
app, but nothing about notifications built outside an Activity.
