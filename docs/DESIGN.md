# Design: Habit Tracker (working title)

> **STATUS: APPROVED by the user (2026-11-08).** Basis for the plan phase.
> Decisions 1-39 are in section 9. Open questions that remain are carried
> to the plan (section 10).

## 1. Overview

Source: `docs/REQUIREMENTS.md`. Design topics confirmed by the user (derived
from Must/Should features and constraints):

1. Habit model: measurement type and direction.
2. Entries: any day, editing past days.
3. Timer: survives closing the app; ongoing notification with Stop (Should).
4. Goals and periods: fixed day/week/month, evaluation per type and
   direction, goal completion percentage.
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
| Presentation | Screen state (everything computed; texts as identifiers and raw values, resolved and formatted by the GUI, Decision 22) and handling of user actions on a screen (calls Domain, updates state, decides navigation). Pure Kotlin, no Android and no widgets. No business rules. Depends on Domain. |
| GUI | Compose widgets and navigation; renders state from Presentation and forwards user actions to it, without making decisions. Contains the thin androidx `ViewModel` holders. Replaceable without changes to Domain and Presentation. |
| App (composition root) | Wires everything: creates the port implementations and connects them to Domain and Presentation. |

**Dependency direction:** `GUI -> Presentation -> Domain <- Persistence`, and
`Domain <- Platform services`. Domain depends on nothing. App knows all
components; no component knows App.

**How the design principles are met:**
1. Business logic free of frameworks: Domain is a separate pure Kotlin/JVM
   module; storage and platform features are reached through ports defined
   by Domain and implemented in Persistence and Platform services.
2. Business logic separated from the GUI: GUI depends on Presentation only
   (state in, actions out); Presentation and Domain know nothing about
   Compose. Replacing the GUI does not touch them.
3. Patterns: see section 9 (MVVM with unidirectional state, Ports and
   Adapters, Repository, sealed hierarchy).

**Note on iOS:** pure Kotlin Presentation and Domain make a later move
easier, but sharing them with iOS would need Kotlin Multiplatform and a
change of the Domain module type. Not designed for in v1 (out of scope);
the choices only do not rule it out.

## 4. Data model

Level 1 (confirmed): names and relations only; attributes and invariants are Level 2.

- **Habit**: a pure definition of the activity. Identity, name, direction
  (Build / Quit), archived flag (yes / no). Direction is the habit's
  intent and exists even without a goal. It is not part of the goal.
- **Measurement**: how the habit is measured. A closed (`sealed`) set of
  variants: Binary, Numeric (with a unit), Duration. Exactly one per habit.
  The unit belongs to the Numeric variant, not to the Habit. A unit is
  data, not a type: either a built-in one (identifier, name from string
  resources) or a custom one (text label). Details are Level 2.
- **Goal**: optional (a habit may exist without any goal). Target value and
  period (day / week / month). Separate from Habit, so changing a goal does
  not change the habit definition. The comparison ("at least" / "at most")
  is derived from the habit's direction. The period is a separate attribute
  so a goal without a period can be added later (see Decision 7).
- **Entry**: a recorded value for a habit at a moment in time (date and
  time, not only a day). Its meaning comes from the habit's Measurement.
- **RunningTimer**: a habit plus the instant the timer was started.

Relations: Habit 1 - 1 Measurement; Habit 1 - 0..n Goal (versions, see
below); Habit 1 - 0..n Entry; Habit 1 - 0..1 RunningTimer (several timers
may run at once, at most one per habit; Decision 18).

Level 2 (business-relevant attributes and invariants, partial):
- **Habit**: direction is immutable after creation. Name is editable.
  Archived habits keep entries and statistics; deleting removes the habit
  with its entries and goals (Decision 17).
- **Measurement**: the type cannot change once the habit has its first
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
  - Binary: at most one per day (invariant); presence means "occurred",
    absence means "did not occur" (Decision 14).
  - The day of an entry is a calendar day in the device time zone
    (Decision 13).
- **Goal**: a target value, a period (day / week / month) and the instant
  from which this version applies. A change creates a new version that
  applies from the start of the current period; earlier periods keep the
  goal they had (Decision 15). Versions are an internal detail: for the
  user, changing a goal is just editing its value on the same habit (same
  identity, name and entries; no new habit is created, Decision 28).

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

- **Week start:** Monday. The first day of the week is a parameter of the
  period calculation, not a constant hard-coded in the rules, so a user
  setting can be added later without changing the rules (Decision 11).
  Day and month boundaries need no decision.

- **Day boundary (Decision 13):** a day is a calendar day in the device
  time zone. The time zone is a parameter of the calculation, supplied via
  a clock port, like the week start. A duration entry that crosses midnight
  contributes to each day the part of its length that falls in that day
  (Decision 12). A manual duration entry gets a default start of 00:00 of
  the chosen day, which the user can change. If the device time zone
  changes, days are recomputed from the stored instants (accepted for v1).
- **Period result (Decision 16):** Duration: total length attributed to the
  days of the period. Numeric: sum of entry values. Binary: number of days
  with an entry.
- **Achieved (Decision 16):** Build: result >= target. Quit: result <=
  target (the target may be 0).
- **Completion percentage (Decision 16):** Build: result / target, capped at
  100% (the cap is a parameter in Domain, always on in v1). Quit: 100% when
  the limit is kept, otherwise target / result (0% when the target is 0
  and the result is above 0). A period is achieved exactly when its capped
  percentage is 100%.
- **Goal change (Decision 15):** a changed goal applies from the start of
  the current period; completed earlier periods keep their goal. If the
  period length changes (e.g. week to day), the new goal applies from the
  start of the current period of the new length; the cut-off partial period
  of the old length is neither achieved nor failed and is left out of
  goal statistics.
- **Timers (Decision 18):** several timers may run at once, at most one per
  duration habit. A running timer is just a start instant, so it survives
  closing the app. Stopping it creates a duration entry (start, length).

- **Statistics, unfinished period (Decision 19):** the current unfinished
  period is excluded from "times the goal was achieved" and from the
  average completion percentage. It is shown separately as "in progress"
  (result so far vs. target).
- **Statistics, range edges (Decision 20):** only periods fully inside the
  chosen range count in the goal statistics. Total time, number of
  occurrences and average per day use all days of the range. The average
  per day divides by all days of the range (also days without entries);
  a range reaching into the future counts only up to today. If the range
  contains no full period, the screen shows that there are none.
  Statistics of archived habits stay available (Decision 17).
- **Timer vs. Android (Decision 21):** no background service. Domain holds
  the start/stop logic (length = now - stored start instant); Persistence
  holds the start instant; Platform services implements a "timer
  notification" port (show / hide). The notification is an ongoing system
  notification whose built-in chronometer only displays elapsed time from
  the start instant; the app never reads state from it. There is one
  notification per running timer (Decision 32). The Stop button
  in the notification calls the same stop logic as the Stop button in the
  app, through a receiver in Platform services. A later home screen widget
  would be one more adapter on the same ports (out of v1).

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
order in v1 (no manual ordering). In v1 every habit appears every day (no
weekday schedule). Each row has the habit name, an action control that
depends on the type, and a small progress indicator. It must fit the width
of a phone.
- Binary: tap toggles a check (entry exists or not; for a quit habit the
  check means "it happened").
- Numeric: a "+" button adds one step as a separate entry, plus a way to
  type a value (nobody taps "+" 200 times).
- Duration: start / stop of the timer, plus manual entry of a time.
- Progress indicator: compact, no descriptive text. It shows the
  completion percentage of the current period from Decision 16 (result so
  far). At 100% it shows a green check; otherwise the percentage. For a
  quit habit it stays a green check while the limit is kept; after the
  limit is exceeded it shows target / result as a percentage. A habit
  without a goal has no indicator. Indicator and action control must look
  different, so the indicator is not mistaken for a button (suggestion: a
  ring with the number inside; the look is decided in the plan / UI work).
- If the unfinished period must be marked as "in progress" (Decision 19),
  the Day screen does it implicitly by showing the result so far; the
  detailed view is on the Statistics screen.

**Entries of one habit on one day (Decision 27):** tapping a habit row on
the Day screen opens a bottom sheet with the entries of that habit on the
selected date. Each entry can be edited (value; for duration start and
length) or deleted, and an entry can be added manually. Any date is
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
- One form for creating and editing: name, direction (Build / Quit),
  measurement type. For Numeric also the unit (built-in or custom label)
  and the "+" step. Last, an optional goal section (target value and
  period day / week / month), off by default. Fields that cannot change
  are shown read-only with a short explanation: direction after creation,
  type after the first entry (Decision 17). Changing a numeric unit shows
  a warning (relabel only). Editing a goal changes the value on the same
  habit (see section 4, Decision 15).

**Statistics screen (Decision 29):** one screen. At the top a date range
selector: presets 7 days, 30 days, 6 months, 1 year, and a custom range
chosen in a calendar. The default is 30 days (Decision 39). Below, one card per habit, with archived habits in a
separate section. The range is common to all cards. A card shows only
figures that make sense for the habit:
- Duration: total time and average per day.
- Numeric: sum of values and average per day (the requirements do not
  state this explicitly; it mirrors duration, see open question 19).
- Binary: number of days with an occurrence.
- If the habit has a goal: how many times the period goal was achieved,
  the average completion percentage (rules in section 5), and the current
  unfinished period shown separately as "in progress" (Decision 19). If
  the range contains no full period, the card says so (Decision 20).
Charts are out of v1 ("Ideas for later").

**Export / import:** a menu item on the Habits screen; behavior in
section 7 (Decisions 30 and 31).

## 7. Cross-cutting concerns

- **Localization (Decision 22):** Presentation exposes text identifiers
  and raw values (durations, dates, numbers, unit identifiers); the GUI
  resolves them from Android string resources and formats them for the
  locale. Adding a language means adding resource files only. Built-in
  unit names come from resources; a custom unit is a plain label.
- **Timer in the background:** see section 5 (Decision 21).

- **Export / import format (Decision 30):** a JSON file with a format
  version number. The content (habits, goals with their version history,
  entries, running timers) follows a data model defined by Domain, not the
  stored representation of Persistence. Domain owns the format rules
  (validation, supported versions); writing and reading the file is done
  by an adapter in Platform services through a file port. Changing the
  persistence library or its schema does not invalidate old files. Export
  to CSV or other formats is not planned (add to "Ideas for later" if
  wanted).

- **Import behavior (Decision 31):** import replaces all data in the app
  with the content of the file. Before replacing, the app shows a short
  summary of the file (export date, number of habits and entries) and
  asks for confirmation, with a warning that current data will be lost.
  The file is validated first (format, supported version); import is
  all-or-nothing: an invalid file or one from a newer unsupported version
  is rejected and the current data stays unchanged. Merging with existing
  data is out of v1 ("Ideas for later" in the requirements). After an
  import, running timers and the timer notification must match the
  imported timers (open question 20).

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

- **Domain (JVM unit tests):** periods, goal evaluation, completion
  percentage, statistics, splitting a duration entry across days, timer
  logic, export file format. Ports are replaced by simple hand-written
  fakes (in-memory repositories, a clock with a fixed time and time zone,
  Decision 13); no mocking library.
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
- Goals evaluated correctly: the same tests plus edge cases (result equal
  to target, target 0, goal change in the middle of a period).
- Final confirmation: one week of real use by the author.

## 9. Decisions log

| # | Decision | Options considered | Reason |
|---|---|---|---|
| 1 | Separate Gradle module for Domain (pure Kotlin/JVM) + one `app` module; the rest separated by packages. | (a) separate Domain module + `app`; (b) single module, packages only; (c) several modules (domain / data / presentation / ui). | The compiler enforces that Domain cannot use Android (design principle 1), tests run fast on the JVM. (b) enforces nothing. (c) is more configuration than this app justifies. |
| 2 | A goal is optional. | (a) optional; (b) mandatory. | Tracking without goals must be possible; requirements do not say goals are mandatory. |
| 3 | Goal is separate from Habit. | Goal inside Habit. | Habit is a pure definition; changing a goal must not change the habit. Allows a goal history. (User decision.) |
| 4 | Measurement (type and unit) is separate from Habit. | Type and unit inside Habit. | Habit is a pure definition; the unit describes how a habit is measured. (User decision.) |
| 5 | Direction (Build / Quit) lives in Habit. | (a) in Goal; (b) in Habit; (c) in Measurement. | Direction is the intent of the habit and exists without a goal (e.g. tracking time one wants to reduce). Matches the wording of the requirements. The goal comparison is derived from it. (User decision; the initial recommendation was (a).) |
| 6 | Entry stores a moment in time, not only a day. | Day only. | Timer sessions and manual entries have a time; the day is derived from it. (User decision.) |
| 7 | A goal without a period (cumulative, e.g. "1000 books") is out of v1. Added to "Ideas for later" in `docs/REQUIREMENTS.md`. The period stays a separate attribute of the goal. | (a) out of v1; (b) in v1 (needs new rules and a change of the requirements); (c) leave open. | Not needed by the v1 success criteria; (b) adds rules with no backing in the requirements. (a) keeps the variant possible later. (User decision.) |
| 8 | GUI toolkit: Jetpack Compose. | (a) Jetpack Compose; (b) Android Views (XML) with ViewBinding; (c) Kotlin Multiplatform with Compose Multiplatform. | Mainstream, current standard for new Android apps, the "state down, events up" model fits the Presentation/GUI split. (b) means more boilerplate and imperative view updates. (c) designs for iOS, which is out of v1, and adds complexity. (User decision.) |
| 9 | Presentation pattern: **MVVM with unidirectional state flow**. Presentation classes are pure Kotlin (expose a `StateFlow` of screen state and action methods); a thin androidx `ViewModel` in the GUI component only keeps them alive across rotation. | (a) MVVM with unidirectional state; (b) MVI (single state, intents, reducer); (c) MVP (presenter drives the view through an interface). Variant: use androidx `ViewModel` directly as the Presentation class. | (a) gives the state/action split that principles 1 and 2 need with little ceremony and fits Compose. (b) adds ceremony not justified by simple screens. (c) is imperative and fits Views, not Compose. The direct `ViewModel` variant was rejected: pure Kotlin keeps Presentation independent of Android and testable on the JVM, at the cost of one small holder class per screen. (User decision.) |
| 10 | Measurement is a **closed `sealed` hierarchy** (Binary, Numeric with unit, Duration). A unit is data inside Numeric (built-in or custom label), not a new variant. | (a) sealed hierarchy (sum type); (b) enum plus optional fields; (c) open class hierarchy with polymorphism (Strategy pattern). | The set of types is known and closed in v1 (scale and checklist are Won't), so compiler-checked exhaustive handling is worth more than openness. (b) allows invalid states (Binary with a unit, Numeric without one). (c) would mix rules with presentation and adds flexibility v1 does not need. (User decision.) |
| 11 | The week starts on Monday. The first day of the week is a parameter of period calculation. | (a) always Monday; (b) from the device settings (needs another port, result depends on the phone); (c) user setting in the app (needs a settings screen, not in the v1 requirements). | v1 is for the author and friends in Poland; (b) and (c) add a port or a screen with no backing in the requirements. Making it a parameter keeps (b) and (c) possible later. (User decision.) |
| 12 | A duration entry always has a start instant and a length. Domain computes the split per day; the entry stays one. | (a) one entry, split computed in Domain; (b) split into several entries silently when the timer stops; (c) assign the whole entry to the day it started. | (a) keeps one source of truth, one session stays one session for editing and deleting, and works for sessions longer than a day. (b) modifies data nobody asked to change and leaves orphan halves after an edit. (c) is simplest but less accurate. (User decision; the user first proposed (a) or (b) and chose (a).) |
| 13 | A day is a calendar day in the device time zone; the time zone is a parameter supplied via a clock port. A manual duration entry gets a default start of 00:00 of the chosen day, editable. | (A) calendar day in the device time zone; (B) like A with splitting of sessions across days; (C) custom day boundary (e.g. 04:00). | (A) is simplest and matches the requirements; splitting was added by Decision 12; (C) needs a setting not in the requirements. (User chose B, which is A plus Decision 12.) |
| 14 | Entries per habit per day: duration many; numeric many, summed; binary at most one (invariant). | Numeric: one overwritten value per day; binary: several per day. | Summing keeps logging low-friction (0.5 l now, 0.3 l later). One binary entry per day keeps "occurrences" and "days with occurrence" the same number. A sub-day binary habit can be modeled as a numeric count; longer periods are aggregations of days. (User decision.) |
| 15 | Goals have versions. A change applies from the start of the current period; earlier periods keep their goal. A change of period length applies from the start of the current period of the new length; the cut-off partial old period is excluded from goal statistics. | (a) goal history, applies from the next period; (b) one current goal, history recalculated; (c) goal history, applies from the start of the current period. | Statistics such as "how many times the goal was achieved" are only meaningful if the past does not change (rules out b). The user rejected waiting for the next period (a): the change should take effect immediately and recompute the current period. (c) chosen. |
| 16 | Goal evaluation and percentage as in section 5. Capping is a Domain parameter, always on in v1; a user option is in "Ideas for later". | Percentage without a cap; quit with a linear penalty. | Capping stops overshoots from hiding missed periods in averages. Quit uses target / result because it needs no special case for a target of 0. The uncapped variant is not meaningful for quit. One formula feeds both "times achieved" and "average percentage". (User decision.) |
| 17 | Habit management: archiving keeps entries and statistics (available for archived habits) and the habit can be restored; deleting removes everything after confirmation; direction immutable after creation; measurement type immutable after the first entry; name editable; changing a numeric unit only relabels, with a warning, no conversion. Conversion of built-in units is in "Ideas for later". | Unit: (A) relabel only; (B) immutable after the first entry; (C) convert history; (D) unit per entry, convert at presentation. | Conversion is out of v1 and impossible for custom labels (C, D). (B) blocks fixing a typo or a wrong choice. (A) keeps one unit per habit, so statistics always have one meaning. Immutable direction protects the history and the meaning of goals. (User decision.) |
| 18 | Several timers may run at once, at most one per duration habit. | (a) one timer at a time; (b) several timers. | (a) adds a rule "what happens on starting a second one", which is friction (key quality). The model already allows (b). (User decision.) |
| 19 | The current unfinished period is excluded from "times achieved" and the average completion percentage; shown separately as "in progress". | (A) exclude and show separately; (B) include as it stands; (C) include only if already achieved. | A period whose result still changes is neither success nor failure. (B) lowers averages unfairly. (C) is inconsistent between build and quit. Consistent with Decision 15. (User decision.) |
| 20 | Goal statistics use only periods fully inside the chosen range; other statistics use all days of the range; average per day divides by all days in the range (to today at most). | (A) only full periods; (B) overlapping periods with the full period result; (C) overlapping periods evaluated on the part inside the range. | A period can only be judged on complete data. (B) makes the range statistic depend on data outside it. (C) judges a goal on part of its period. Cost: a short range may contain no full period. (User decision for A; the average-per-day default was proposed by the assistant and not objected to.) |
| 21 | Timer: no background service. Domain computes length from the stored start instant; a notification port in Platform services shows an ongoing notification with a system chronometer (display only) and a Stop button calling the same stop logic. | (A) no service, ongoing notification with chronometer; (B) foreground service that ticks and refreshes the notification; (C) periodic work (WorkManager). | The start instant is the source of truth, so nothing needs to run in the background. (B) adds a service and type declarations for no gain. (C) cannot refresh in seconds. Keeps principle 1 (Domain does not know notifications) and a later widget possible. (User decision.) |
| 22 | Localization: Presentation exposes text identifiers and raw values; the GUI resolves texts and formats dates, numbers and durations. | (A) identifiers and raw values, GUI resolves; (B) a `StringProvider` port, Presentation returns ready texts; (C) Presentation uses Android resources directly. | (A) keeps Presentation free of Android with no extra port, tests check identifiers, language change needs no state recompute. (B) adds a port and stale texts after a language change. (C) breaks principle 1. Cost: formatting logic is in the GUI and is rewritten if the GUI is replaced. (User decision.) |
| 23 | Navigation: bottom bar with 3 tabs (Day, Habits, Statistics), no swipe between tabs; Day always the default; empty state on Day only when there is no habit that is not archived; creating habits only on the Habits screen; default-start-screen setting out of v1 (added to "Ideas for later"). | (A) 3 tabs; (B) Day as base with a menu / drawer; (C) 4 tabs with Settings. For the first start: (a) a first-run flag opening the Habits screen; (b) empty state on Day derived from data; (c) a setting for the default screen. | (A) puts logging, management and statistics one tap apart and adds no screen the requirements do not need. (C) would have an almost empty tab. A first-run flag (a) is stored state and fails when the user deletes all habits; (b) follows from the data. (c) needs a settings screen not in the v1 requirements. In v1 every habit shows every day, so "no habits" is the only empty case. (User decision; the user proposed the first-run idea, the assistant proposed (b), the user agreed after clarification.) |
| 24 | Day screen: the input method follows the habit type (binary: check toggle; numeric: "+" and typed value; duration: timer and manual time); fixed order in v1. | One uniform input (a dialog for every type); manual ordering. | Fewest taps for the common action (key quality). Manual ordering is not in the v1 requirements. (User decision.) |
| 25 | Numeric habit has an optional step for "+", default from the unit (1 for a custom unit); each tap creates a separate entry. | (A) fixed step of 1; (B) step set per habit with a default from the unit; (C) no step, "+" opens a value dialog. | (A) is useless for 0.25 l or 200 ml. (C) needs typing every time. (B) makes the common case one tap at the cost of one more field. Consequence: the list of built-in units needs a default step per unit (open question 11). (User decision.) |
| 26 | Day progress indicator: compact, percentage of Decision 16 for the current period, green check at 100% (also for a quit habit while the limit is kept), no indicator without a goal. | (A) "result / target" text for all; (B) percentage for both directions; (C) percentage for build, "result / limit" for quit. | Text is too long for a phone row next to the action control. A quit habit that shows a green check while the limit is kept is positive and motivating (UI principle 2). One format for all habits is faster to read. After exceeding the limit it shows target / result as a percentage, e.g. limit 3, result 5 gives 60%. If a fraction is ever shown it should read "result / limit". (User decision.) |
| 27 | Entries of a habit on a day are edited and deleted in a bottom sheet opened by tapping the habit row on the Day screen; manual adding is there too. Old entries are reached through the date picker. No date markers in the picker in v1. | (A) bottom sheet from the Day row; (B) separate history screen of the habit, from the Habits screen; (C) long press on the row. For the sheet: bottom sheet vs. full screen. | (A) is one tap from the day being viewed and adds no screen to the 3-tab navigation. (B) is more code and navigation and forces leaving the Day screen to fix a day. (C) is hard to discover (UI principle 1). A bottom sheet closes faster and fits a short list. Cost: no view of all entries of a habit across days. Markers or a month calendar are a later iteration ("Calendar screen" in "Ideas for later"); this choice keeps that possible. (User decision; the user stated that a month calendar with marked habits is wanted in later iterations.) |
| 28 | Habit create / edit form: one form, fields depend on the type, optional goal section off by default; the same form edits. Editing a goal does not create a new habit; goal versions are an internal detail (Decision 15 unchanged). Habits screen: list, "+", collapsible archived section. | (A) one form; (B) step-by-step wizard; (C) minimal form, goal and unit added later in edit. | (A) fits "easy to configure, minimal friction": everything in one place, a simple habit needs three fields. (B) adds screens and taps. (C) leaves a numeric habit without a unit and needs a separate step for the goal. (User decision; the user clarified that changing a goal must not look like creating a new habit.) |
| 29 | Statistics screen: one screen with a common date range at the top and one card per habit (archived in a separate section); figures per type: duration total and average per day, numeric sum and average per day, binary days with an occurrence; with a goal also times achieved, average completion percentage and "in progress". | (A) one screen, cards for all habits; (B) pick a habit first, then its statistics; (C) one habit at a time with a switcher. | The range is common, so it is set once and all habits are visible without extra taps. (B) and (C) need more taps and make comparison harder. The numeric figures are a clarification of the requirements, not a change (open question 19). (User decision.) |
| 30 | Export / import format: JSON with a format version, based on a data model defined by Domain; file access through a port implemented in Platform services. | (A) versioned JSON from the Domain model; (B) copy of the SQLite database file; (C) CSV. | Backup is the only way to restore data (offline, no account), so old files must stay readable. (A) keeps the format independent of the persistence library and its schema (principle 1) and lets Domain own the validation rules. (B) is simplest but ties backups to the database schema. (C) loses information or needs several files for habits, goals and entries. Cost: mapping to and from the file model and compatibility tests. (User decision.) |
| 31 | Import replaces all data after a summary, a warning and a confirmation; the file is validated first and import is all-or-nothing. Merge is out of v1 (added to "Ideas for later" in the requirements). | (A) replace; (B) merge; (C) user chooses replace or merge. | The main scenario is restoring a backup, where replacing is natural and predictable. (B) needs conflict rules (same habit in both places, overlapping entries, a second binary entry on one day, different goals) that nobody specified, and a mistake silently corrupts statistics. (C) contains all of (B). Cost: entries made after the export are lost, hence the confirmation. (User decision; the user asked to move merge to "Ideas for later".) |
| 32 | One ongoing notification per running timer (habit name, system chronometer, own Stop button). | (A) one notification per timer; (B) one combined notification for all timers; (C) a notification only for the most recently started timer. | Timers are independent (Decision 18), so a notification mirrors one timer: start shows it, stop hides it. The notification port stays simple (show / hide for a habit) and Stop calls the same logic as in the app. (B) makes a Stop button per timer awkward. (C) hides running timers. Cost: more space in the notification shade with several timers, a rare case. (User decision.) |
| 33 | Android Auto Backup is disabled; backup is only the manual export / import. | (A) disabled; (B) enabled. | The requirements forbid any data leaving the device except a user-initiated export. (B) is more convenient on a phone change but sends data to Google's cloud. Cost: a new phone without a prior export means data loss. Cloud backup stays in "Ideas for later". (User decision.) |
| 34 | Minimum Android version: 13 (API 33). | (A) API 26 (Android 8); (B) API 33 (Android 13); (C) API 31 (Android 12). | All v1 users (the author and friends) have recent phones, so support for old systems would be code that never runs. (B) gives one notification-permission path and native `java.time`. (A) and (C) keep legacy branches without a user who needs them. Cost: phones that never got Android 13 are excluded; lowering later is a local change in Platform services. Google Play publication is a Won't for v1. (User decision; the user questioned the initial recommendation of API 26.) |
| 35 | Persistence library: Room, used only in Persistence behind the storage ports. | (A) Room; (B) SQLDelight; (C) plain SQLite (`SQLiteOpenHelper`); DataStore rejected as unfit for relational data. | Room is the mainstream, best documented choice (learning project), checks queries at compile time, supports coroutines / `Flow` and schema migrations, which are needed because data must survive app changes. (B) is multiplatform (iOS is out of v1) and has less learning material. (C) means much manual code and manual migrations. Cost: Room is Android-specific, which does not matter because it is confined to Persistence. Versions are checked in the plan. (User decision.) |
| 36 | (1) JSON: kotlinx.serialization in the Platform services adapter, with file-model classes separate from Domain. (2) Dependency injection: manual, by constructor, wired in App. (3) Navigation: Navigation Compose. | (1) Moshi; manual `org.json`. (2) Hilt; Koin. (3) hand-written screen state without a library. | (1) Kotlin-native, no reflection, and keeping its annotations out of Domain preserves principle 1; Moshi needs extra setup with Kotlin, `org.json` means much manual code. (2) The app has few components, so a framework adds configuration (generated code, modules) without a gain; constructor injection keeps a later switch possible; cost is manual wiring in one place that grows with the app. (3) Standard for Compose; the hand-written variant would work for three tabs but teaches less. Versions are checked in the plan. (User decision.) |
| 37 | Built-in units: ml, l, glasses, g, kg, km, steps, kcal, pages, repetitions, count, each with a default "+" step (table in section 4). Identifiers and steps in Domain, names in resources. | A longer list; a shorter list; no built-in units. | A short list covers the user's examples (fluids, books, steps), and anything missing is a custom unit, so nothing is blocked. Extending the list means adding entries. (User decision; the list was proposed by the assistant and accepted.) |
| 38 | Testing strategy: Domain and Presentation as JVM unit tests with hand-written fakes; Persistence instrumented; export round-trip tests; GUI and Platform services checked manually; success criteria via a replay test of the three-habit week and one week of real use. | (A) tests per component, weight on logic; (B) A plus Compose UI tests; (C) mostly end-to-end tests. | Most errors hide in rules (periods, percentages, midnight), which are the cheapest and fastest to test; the architecture was built for that. (B) adds maintenance for a GUI that is meant to be replaceable. (C) is slow and fragile. Cost: no automated screen tests, so screen regressions need manual checks. (User decision.) |
| 39 | Statistics range presets: 7 days, 30 days, 6 months, 1 year, plus a custom range from a calendar; default 30 days. | A different list of presets; custom range only. | 6 months and 1 year are the examples in the requirements; shorter presets suit a week-long trial; a default avoids an empty screen on opening. (User decision; proposed by the assistant.) |

**Patterns used** (design principle 3):

| Pattern | Where | Reason | Why not the alternatives |
|---|---|---|---|
| MVVM with unidirectional state flow | Presentation / GUI | See Decision 9. | MVI, MVP: see Decision 9. |
| Sealed hierarchy (sum type) | `Measurement` | See Decision 10. | Enum plus optional fields, Strategy: see Decision 10. |
| Ports and Adapters (dependency inversion) | Domain defines ports; Persistence and Platform services implement them | Required by principle 1: dependencies must point to Domain. | Domain calling Room or Android directly breaks principle 1. (Announced to the user, no objection.) |
| Dependency Injection (manual, constructor) | App wires all components | Components receive their ports through constructors; only App knows concrete classes (principle 1). See Decision 36. | Service locator hides dependencies; Hilt / Koin add configuration the app does not need. |
| Repository | Shape of the storage ports | Domain sees a collection of entities, not a database. | Ports shaped like SQL queries leak persistence details into Domain. (Announced to the user, no objection.) |

## 10. Open questions

1. (Resolved: Decision 14.)
2. (Resolved: Decisions 12 and 13.)
3. (Resolved: Decision 15.)
4. (Resolved: Decision 17.)
5. (Resolved: Decision 17.)
6. (Resolved: Decision 18.)
7. (Resolved: Decision 21.) Still to verify in the official Android docs
   before the plan (the assistant is not sure): the exact API for a
   notification chronometer; whether an ongoing notification without a
   foreground service can be dismissed by the user on current Android
   versions; whether a notification permission is required (Android 13+).
8. (Resolved: Decision 22.)
9. (Resolved: the diagram shows App wiring to GUI and Presentation.)
10. (Resolved: Decision 34.)
11. (Resolved: Decision 37.)
12. App name (inherited from requirements).
13. (Resolved: Decisions 19 and 20.)
14. (Resolved: Decision 32.)
15. (Resolved: Decision 37.)
16. Look of the progress indicator (ring or other) and whether the Day
    screen needs any explicit "in progress" mark (UI / plan phase).
17. (Resolved: Decision 39.)
18. Later iteration: a month calendar with habits marked on their days
    replaces or extends the date picker ("Ideas for later"). Needs a
    Domain query for days with entries and, for the marks, colors / icons
    of habits (also in "Ideas for later"). Not designed in v1.
19. Statistics for Numeric habits are not stated in the requirements
    (sum and average per day proposed, Decision 29); the user agreed to the
    proposal, to be kept in mind for `docs/REQUIREMENTS.md`.
20. After an import, how the running timers and the timer notification are
    brought in line with the imported timers (Platform services; detail
    for the plan).
21. Exact Android settings to disable Auto Backup and device transfer
    (to verify in the official docs; the assistant is not sure of the
    current details).

Carried to the plan phase: 7, 12, 16, 18, 19, 20, 21. None of them changes
the structure of the design.
