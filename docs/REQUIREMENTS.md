# Requirements: Habit Tracker (working title)

> **STATUS (2026-11-13): REVISED, NOT APPROVED.** The document was reopened after
> approval and revised; `docs-reviewer` reviewed it three times on 2026-11-13 and its findings
> were applied. The user has not approved it yet. Plan, agreed decisions and next
> steps: `docs/STATUS.md`. Open points: section 5.

## Map

| Section | Content | IDs | Related REQ |
|---|---|---|---|
| 1 | Users and problem | | |
| 2 | Features: Must, Should, Won't | REQ-M*, REQ-S*, REQ-W* | |
| 3 | Constraints | REQ-C* | |
| 4 | Success criteria | REQ-SC* | |
| 5 | Open questions | | |
| 6 | Extensibility constraints (from section 8) | REQ-X* | |
| 7.1 | Evaluation of a goal period | BHV-EVAL-* | M2, M3, M8c, SC3 |
| 7.2 | Goals: start, change, archive (rules R1-R7, R4a) | BHV-GOAL-* | M3 |
| 7.3 | Periods and goal duration (rules R1-R2) | BHV-PERIOD-* | M3 |
| 7.4 | Day, entries, timer, notifications (rules ENTRY-R1-R8, DAY-R1-R2, TIMER-R1-R5) | BHV-ENTRY-*, BHV-DAY-*, BHV-TIMER-*, BHV-NOTIF-* | M4, M5, M6, M7, S3 |
| 7.5 | Statistics (rules R1-R7) | BHV-STATS-* | M8a-d, SC2 |
| 7.6 | Habits and goal validation (rules GOALVAL-R1, HABIT-R1-R4) | BHV-GOALVAL-*, BHV-HABIT-* | M1, M2, M3, M6, M7, S2 |
| 7.7 | Export and import (rules EXPORT-R1, IMPORT-R1-R4) | BHV-EXPORT-*, BHV-IMPORT-* | S1, SC1 |
| 7.8 | Language (rules R1-R3) | BHV-LANG-* | M9, C4 |
| 8 | Ideas for later | | |

## 1. Users and problem

**Users:** For now only the author (personal use, manual installation) and friends for
testing. Later: publication in Google Play for other users. iOS is a possible later step
(not v1).

**Problem:** Many habit-tracking apps exist, but none combines everything in one place:
- tracking time spent on an activity (e.g. reading),
- goals over a chosen time interval (not only weekly),
- simple done / not done habits (e.g. "went to bed early"),
- quitting unwanted habits (e.g. "scrolled less than X"),
- useful statistics.

The author currently uses "Simple time tracker" (Razeeman): it tracks time and supports
weekly goals, but has no done / not done habits and no tracking of quitting habits.

**Key quality:** easy to configure and customize. Minimal friction for the user:
adding and tracking habits must be as simple and pleasant as possible.

## 2. Features

Requirements have stable IDs (`REQ-...`) so that the design, the plan and
tests can refer to them. An ID is never reused; a removed requirement keeps
its number retired.

### Habit model
A habit is defined by two independent dimensions:
- **Measurement type** (how an entry is recorded):
  - binary (done / not done),
  - numeric (a quantity, fractional values allowed, with a unit, e.g. 1.5 l of fluids),
  - duration (time, e.g. 3 hours of scrolling). Both manual entry and a timer are
    input methods for this type.
- **Direction:**
  - build (goal: at least X),
  - quit (limit: at most X, possibly 0).

### Must
- **REQ-M1** Habits with measurement types: binary, numeric, duration.
- **REQ-M2** Both directions for every measurement type: build and quit.
- **REQ-M3** Goals per habit over a fixed period: day, week, month, quarter, year.
  By default a goal repeats automatically every period. Instead, the user can
  set a goal for a chosen number of periods (e.g. 30 days); it ends after them.
  For binary habits the goal counts days with an occurrence
  (e.g. "at most 3 days per week").
- **REQ-M4** Duration habits: manual time entry and a start/stop timer.
  A running timer survives closing the app (it stores its start time).
- **REQ-M5** Adding and editing entries for past days. Reached by switching to the
  previous / next day and by picking a date from a calendar.
- **REQ-M6** Habit management (create, edit, archive, delete) and a Day screen (it
  shows today by default) for quick logging.
- **REQ-M7** Built-in units for numeric habits: ml, l, glasses, g, kg, km, steps,
  kcal, pages, repetitions, count. Each unit has a default step for the "+" button
  on the Day screen (ml 250, l 0.25, glasses 1, g 100, kg 0.5, km 1, steps 1000,
  kcal 100, pages 1, repetitions 1, count 1); the user can change the step per
  habit. Duration is not a unit (it is its own measurement type). ml and l are
  separate units, with no conversion between them.
- **REQ-M8** Statistics over a user-chosen date range (e.g. last 6 months, last year):
  - **REQ-M8a** total time spent on a habit and the average per day
    (e.g. "6 months: X h Y min, on average Z per day"),
  - **REQ-M8b** number of occurrences (e.g. how many times went to bed early in a year),
  - **REQ-M8c** per goal of the habit: how many times the period goal was
    achieved and the average goal completion percentage (goals are not added
    together; see BHV-STATS-R4),
  - **REQ-M8d** for numeric habits: the sum of values over the range and the
    average per day.
- **REQ-M9** Language switch in the app: Polish or English (REQ-C4). Details in
  section 7.8.

### Should
- **REQ-S1** Export / import of data to / from a file.
- **REQ-S2** Custom unit for numeric habits, defined as a text label (no conversion
  between units).
- **REQ-S3** Ongoing notification while a timer is running (with a Stop button).

### Won't (v1)
- **REQ-W1** Reminder notifications (reminders to log a habit). The ongoing timer
  notification is not part of this: it is REQ-S3.
- **REQ-W2** Goal interval: custom length (e.g. every 10 days). A goal for a
  number of periods (REQ-M3, e.g. 30 days once) is not a custom interval length.
- **REQ-W3** Goal interval: arbitrary set of dates.
- **REQ-W4** (retired: moved to v1 as part of REQ-M3 on 2026-11-11.)
- **REQ-W5** Scale / rating habit type (e.g. mood 1-5).
- **REQ-W6** Checklist habit type (habit made of sub-tasks).
- **REQ-W7** iOS.
- **REQ-W8** Publication in Google Play (v1 is for personal use and friends testing).
- **REQ-W9** Dark theme.
- **REQ-W10** Home screen widget.
- **REQ-W11** Categories, grouping, tagging.

## 3. Constraints

- **REQ-C1** Fully offline. No account, no server.
- **REQ-C2** Data stored locally on the device only. Backup and restore via file
  export / import.
- **REQ-C3** Platform: Android phones only. Minimum Android version: 13 (API 33).
  Reason: all v1 users (the author and friends) have recent phones, so support for
  older versions would be code that never runs. Lowering the minimum later (e.g.
  for a Google Play release) must stay possible.
- **REQ-C4** UI in two languages from the start (Polish and English). All texts in
  resources, with the assumption that more languages will be added later.
- **REQ-C5** No ads, no analytics, no crash reporting. No data leaves the device except
  a user-initiated export.

## 4. Success criteria

v1 is successful if, for one week, the author can record and monitor three habits:
- a duration habit (build), e.g. working on a book, using the timer and one entry for a
  past day,
- a numeric habit (build) with a daily goal, e.g. litres of fluids drunk,
- a binary habit (quit) with a weekly goal, e.g. going to bed late, at most 3 days
  per week.

Checks:
- **REQ-SC1** All entries are saved without errors or data loss.
- **REQ-SC2** Statistics for the week match a manual calculation.
- **REQ-SC3** Daily and weekly goals are correctly evaluated as achieved or not.

Acceptance tests of individual features belong to the plan and implementation phase,
not to this document.

## 5. Open questions

- App name (working title: "Habit Tracker").
- Where the language switch lives (the design has no settings screen), and whether
  the Android per-app language setting can be used (verify in the official docs
  before the plan). Design question.
- The open questions in `docs/DESIGN.md` section 10 (7, 12, 16, 18, 21) are
  unchanged. Design questions.

## 6. Extensibility constraints

Derived from "Ideas for later". They do not add features to v1. They say what
the design must not make expensive: a later version should add each item without
rebuilding what v1 delivers and without losing or reinterpreting existing user
data. They state outcomes only; the model that achieves them is the design's job.
The design phase must read this section together with "Ideas for later".

Labels:
- **accommodate**: the design must make room for it on purpose (the cost of
  adding it later would otherwise be a rebuild).
- **not blocked**: nothing in the design may rule it out, but the design does not
  prepare for it beyond that.

Ideas not listed here are ignored by the design. An "accommodate" item is checked
in the design review: the design names, for each such item, what a later version
would change.

*Labels proposed by the assistant and confirmed by the user on 2026-11-13.*

| ID | Later feature (see "Ideas for later") | Label | What must stay true |
|---|---|---|---|
| REQ-X1 | Parallel goals for one habit (possibly Pro) | accommodate | Goals, history and statistics created in v1 keep their meaning when several goals can run at once for one habit. |
| REQ-X2 | More goal kinds: custom interval length, arbitrary set of dates, a range of concrete dates, a goal without a period (cumulative) | accommodate | A new goal kind can be added without changing how existing goals, their history and their statistics behave. |
| REQ-X3 | Habits assigned to recurring weekdays (and "missed" days in a calendar) | accommodate | A habit can later apply only on some days; habits without a schedule behave exactly as in v1. |
| REQ-X4 | New measurement types (scale / rating, checklist) | accommodate | A new type can be added without changing the data or behavior of existing types. |
| REQ-X5 | User setting for the first day of the week | accommodate | Changing the first day of the week needs no conversion of stored data. |
| REQ-X6 | Calendar screen with habits marked on their days; colors and icons for habits | accommodate | The app can answer "on which days did this habit have entries" for a date range, and habits can later get display attributes (color, icon) without changing existing data. |
| REQ-X7 | Logging entries for future dates (possible Pro) | not blocked | Existing entries, goals and statistics stay valid when future dates become possible. |
| REQ-X8 | Conversion between built-in units; custom units with conversion (Pro) | not blocked | The unit of a habit remains data, so conversion can be added later. |
| REQ-X9 | Import that recognizes the same habit and merges its data (v1 only adds imported habits as new ones); cloud backup through provider APIs | not blocked | The export file stays versioned and old files stay importable; data have stable identities. |
| REQ-X10 | Monetization: free limits (e.g. number of habits) and a Pro version | not blocked | A limit or a gated feature can be added without restructuring the app. |
| REQ-X11 | Reminder notifications; reminder about a long-running timer; minimum session length per habit | not blocked | These can be added as new behavior around timers and notifications without changing how entries and timers are stored. |
| REQ-X12 | Streaks, charts in statistics, categories / grouping / tagging | not blocked | Streaks and charts can be computed from the stored entries, with no new stored data. |
| REQ-X13 | Dark theme; home screen widget; setting for the default start screen; user option for capping the completion percentage | not blocked | The GUI can be themed and extended with a widget; options can be added without changing stored data. |
| REQ-X14 | iOS version; publication in Google Play (lower minimum Android version) | not blocked | The choices of v1 do not rule these out (see REQ-W7, REQ-W8, REQ-C3). |
| REQ-X15 | Goal behavior options: user choice of how the first, partial period counts; retroactive correction of a goal with recalculation; restoring or editing an archived goal on its own; statistics that add up the goals of a habit; goal target "every day of the period" | not blocked | Goal history keeps its meaning if these are added. |

## 7. Behavior

User-visible behavior of the app, moved here from `docs/DESIGN.md` (section 5
and its scenario table) on 2026-11-13, because it is a requirement, not a design
choice. The design must satisfy every rule and scenario below. Domain tests are
derived from the scenarios; UI, persistence and notification rows (e.g. the Day
screen, import, language, notifications) are tested at that level or by hand. One
row is one test.

**Conventions (apply to every scenario):**
- One row is one case with concrete data. A row never describes a change over
  time ("first 3, then 4"); such cases are separate rows (e.g. 4a, 4b).
- Each row is: **Given** (the habit and its goal), **When** (what the user does
  or what happens), **Then** (what the user sees or what the app computes).
- Unless a row says otherwise, "today" is Wednesday 11 Nov 2026. A week is
  Monday to Sunday, so the current week is Mon 9 Nov - Sun 15 Nov 2026.
- For a build habit the goal is a target to reach (at least). For a quit habit
  the goal is a limit not to exceed (at most, possibly 0).
- The **Source** column says who decided: "user" (a decision of the user, with the
  number in `docs/DESIGN-DECISIONS.md`) or "assistant" (a proposal of
  the assistant, confirmed by the user on 2026-11-13). The **Was** column is the former ID in `docs/DESIGN.md` section
  5.5, kept so that references can be updated.

### 7.1 Evaluation of a goal period

Rules (were R-PERIOD-RESULT, R-ACHIEVED, R-PERCENT, R-EXACT-VALUES in the design):
- **BHV-EVAL-R1 Result of a period:** for a duration habit, the total time
  of its entries in the period; for a numeric habit, the sum of the values of its
  entries; for a binary habit, the number of days with an entry.
- **BHV-EVAL-R2 Achieved:** build: result >= goal. Quit: result <= limit.
- **BHV-EVAL-R3 Completion percentage:**
  - build: result / goal, at most 100%;
  - quit: 100% while the limit is kept, otherwise limit / result (so 0% when the
    limit is 0 and the result is above 0; limit 3 and result 5 give 60%);
  - a period is achieved exactly when its percentage is 100%;
  - when shown, the percentage is cut down (truncated) to a whole percent, and
    100% is shown only for an achieved period (99.6% is shown as 99%);
  - numeric values, goals and sums are exact decimals with 3 decimal places (input
    with more places is rounded half up: 0.0005 becomes 0.001, 0.0004 becomes 0),
    so 0.7 + 0.1 equals 0.8 exactly.

| ID | Given | When | Then | Source | Was |
|---|---|---|---|---|---|
| BHV-EVAL-1 | Build habit, numeric, unit l, daily goal 2 l | The user adds two entries on the same day: 1.25 l and 0.75 l | Result of the day is 2 l; goal achieved; completion 100% | user (Dec. 14, 16) | A1 |
| BHV-EVAL-2 | Build habit, binary, weekly goal 3 days | The week has entries on 2 different days | Goal not achieved; completion shown as 66% (66.6 truncated, not rounded up) | user (Dec. 16, 64) | A2 |
| BHV-EVAL-3 | Build habit, numeric, daily goal 0.8 | The user adds two entries on the same day: 0.7 and 0.1 | Goal achieved; completion 100% | user (Dec. 46) | A3 |
| BHV-EVAL-4a | Quit habit, binary, weekly limit 3 days | The week has entries on exactly 3 different days | Goal achieved; completion 100% | user (Dec. 16) | A4 |
| BHV-EVAL-4b | Quit habit, binary, weekly limit 3 days | The week has entries on 4 different days | Goal not achieved; completion 75% | user (Dec. 16) | A4 |
| BHV-EVAL-5a | Quit habit, numeric, unit ml, daily limit 0 | The day has no entries | Goal achieved; completion 100% | user (Dec. 16) | A5 |
| BHV-EVAL-5b | Quit habit, numeric, unit ml, daily limit 0 | The day has one entry of 100 ml | Goal not achieved; completion 0% | user (Dec. 16) | A5 |
| BHV-EVAL-6 | Build habit, numeric, unit ml, daily goal 1000 ml | The entries of the day sum to 996 ml | Goal not achieved; completion shown as 99%, never 100% | user (Dec. 64) | A6 |

### 7.2 Goals: start, change, archive

Rewritten with the user on 2026-11-13 (it replaces Decisions 15, 42, 52 in part,
67, 68 and 69 of the design; the new decisions are logged when the design
decisions are triaged). A goal is only a definition (target, period length, first
day, mode: repeat or N periods, and the last day once it is archived). Progress is never stored: it is computed from the entries of
the habit and the goals, so there is nothing to "finish" when a goal changes.

Rules:
- **BHV-GOAL-R1 Start of a goal:** a goal starts on the first day of a calendar
  period of its own length (day; week Mon-Sun; month; quarter; year). The user picks
  a date and the goal starts at the beginning of the period containing it. Default:
  the current period (but see R4, R4a). An earlier date (counts retrospectively) or a
  later one (the goal is pending) is allowed, with no restriction. The form shows
  the calendar period the date selects (e.g. "Week 9-15 Nov 2026"). Periods are
  evaluated whole: a weekly goal created on Wednesday also counts Monday's entries.
- **BHV-GOAL-R2 Repeat or N periods:** a goal either repeats every period (default)
  or runs for N periods (e.g. 30 days, N >= 1) and then ends; nothing repeats after
  it. A goal has a first day and a last day (both included) when it runs for N
  periods (the last day of the N-th period) or when it is archived by a change or
  with its habit (the day of that change, any day not before the first day; BHV-GOAL-R4,
  BHV-HABIT-R3). The goal is valid from its first to its last day. A goal for N
  periods is archived automatically on the day after its last day and keeps the last
  day of its N-th period. The form rejects a goal for N periods whose last day would
  be before today (it would end at once).
- **BHV-GOAL-R3 One active goal:** a habit has at most one active goal (not
  archived; it may be pending). Archived goals may overlap in time with each other
  and with the active goal. Every goal is evaluated on its own; no goal changes the
  result of another goal.
- **BHV-GOAL-R4 Changing a goal:** changing the target, the period length or the
  first day of the active goal, or switching it off, archives it: its last day
  becomes today. Unless the goal is switched off, a new active goal is created with
  the same mode as the old one: repeating, or for the same N periods counted in full
  from the new start (the form is pre-filled, shows the last day of a goal for N
  periods and lets the user change N). The user is not asked to choose anything.
  The default start of the new goal is the current period of its length
  (BHV-GOAL-R1), also when today is the last day of a period that the old goal
  counts; the user may pick any other date. A period counted by the old goal (R5)
  may be counted by the new goal too; each keeps its own target and result.
- **BHV-GOAL-R4a Replacing instead of archiving:** to leave no empty or mistaken
  archive entries, a goal is replaced (edited in place, not archived) when it has no
  completed period yet or when it was created today (a goal created by restoring a
  habit counts as created that day). A period is completed when its last day is
  before today. Switching off a replaced goal deletes it. When the period length
  does not change, the new goal keeps the first day of the replaced goal by default;
  otherwise the default is the current period. This exception does not apply when a
  habit is archived (BHV-HABIT-R3). A goal that was replaced today counts as
  created today. The form rejects a replacement whose last day would be before today
  (BHV-GOAL-R2).
- **BHV-GOAL-R5 Which periods count for a goal:** a period counts for a goal only
  if the goal is valid on the last day of the period, or the goal is still active
  and the period is the current one (its result so far is provisional). A period
  cut by the last day of a goal is neither achieved nor failed, is not prorated and
  carries no mark: it is simply not counted. Completed periods keep their result;
  the past is not rewritten by a change.
- **BHV-GOAL-R6 Changes without a choice:**
  - changing only the habit name, the unit or the "+" step changes no goal;
  - a pending goal (not yet started) is edited in place or deleted;
  - changing only N, or switching between "repeat" and "N periods", moves or
    removes the last day of the goal (N >= 1, so the last day is never before the
    end of the first period);
  - when the new last day is before today (for a goal that is replaced, R4a, the
    form rejects such N instead, R2), the periods after it stop counting, so
    the user must confirm once ("the last day of the goal will be X; later periods
    will not count"). Declining leaves the goal unchanged.
- **BHV-GOAL-R7 Form and history:** the form shows the active goal (if any),
  editable, and a read-only section with the archived goals of the habit. When
  there is no active goal, the form of a new goal opens empty with defaults (it is
  not filled from an archived goal). The app
  never deletes an archived goal. Editing an archived goal is out of v1; the only
  way to get a goal back is the offer made when its habit is restored
  (BHV-HABIT-R3). Statistics are shown per goal (section 7.5).

Fixture S: build habit, binary, weekly goal 3 days, repeating, first day Mon 26 Oct
2026; weeks of 26 Oct and 2 Nov are completed, the week 9-15 Nov is in progress.
Unless a row says "created today", a goal in the Given was created on its first day
(so not today); BHV-GOAL-R4a depends on it. The source of all rows is stated below
the table.

| ID | Given | When | Then | Was |
|---|---|---|---|---|
| BHV-GOAL-1 | Fixture S | The user changes the target to 4 | Old goal (target 3) is archived, last day 11 Nov; weeks of 26 Oct and 2 Nov keep target 3; week 9-15 Nov does not count under it. New weekly goal (target 4) starts Mon 9 Nov; week 9-15 counts under it. No question asked | B1, B2, B3 |
| BHV-GOAL-2 | Fixture S | Period changed to day, target 1 | Old goal archived, last day 11 Nov, week 9-15 not counted under it. New daily goal starts 11 Nov. 9 and 10 Nov have no counted period under the new goal | B4, B5, B6 |
| BHV-GOAL-3 | Build binary habit, daily goal (target 1), first day Mon 9 Nov | Period changed to week, target 3 | Old goal archived, last day 11 Nov; daily periods 9, 10 and 11 Nov count under it. New weekly goal starts Mon 9 Nov; week 9-15 counts under it | B7 |
| BHV-GOAL-4 | Build habit, yearly goal from 1 Jan 2026, no completed period | Period changed to day | Old goal is replaced (not archived). New daily goal starts 11 Nov, not 1 Jan | B8 |
| BHV-GOAL-5 | As GOAL-4; the habit was created before 1 Nov | As GOAL-4, and the user picks start 1 Nov | New daily goal from 1 Nov; days 1-11 Nov are daily periods | B9 |
| BHV-GOAL-6a | Fixture S; the habit was created before 26 Oct | Target to 4, the user picks start 2 Nov | Old goal archived and counts 26 Oct and 2 Nov under target 3. New goal (target 4) counts 2 Nov and the current week. The week of 2 Nov appears in both goals, each with its own target and result | B10 |
| BHV-GOAL-6b | As GOAL-6a; the habit was created before 19 Oct | Target to 4, the user picks start 19 Oct | New goal counts 19 Oct, 26 Oct, 2 Nov and the current week. The old (archived) goal keeps 26 Oct and 2 Nov under target 3 | new |
| BHV-GOAL-7a | Build binary habit, weekly goal target 3 created today (first day Mon 9 Nov, no completed period) | Target to 4 | Old goal replaced, not archived; new goal from 9 Nov | B11 |
| BHV-GOAL-7b | As GOAL-7a | The user switches the goal off | The goal is deleted; no archive entry; no active goal | new |
| BHV-GOAL-8 | Fixture S | The user switches the goal off | Goal archived, last day 11 Nov; weeks of 26 Oct and 2 Nov keep their result; week 9-15 does not count; no new goal | B12 |
| BHV-GOAL-9 | Fixture S; today is Sun 15 Nov | Target to 4 | Old goal archived, last day 15 Nov; the goal is valid on the last day of week 9-15, so the week counts under it with target 3. New goal (target 4) starts by default Mon 9 Nov and counts the same week with target 4; the week is in both goals, each with its own target and result | new |
| BHV-GOAL-10 | Weekly goal with the last day Sun 1 Nov (archived); no active goal | The user opens the habit form and enables the goal | The goal section was off; the new goal starts Mon 9 Nov. The archived goal stays in the archive | B13 |
| BHV-GOAL-11a | Fixture S | Only the habit name changes | No goal changes; nothing asked | B14 |
| BHV-GOAL-11b | Build numeric habit with a daily goal | Only the unit changes | Goal unchanged; nothing asked (the warning about relabeling values still appears) | B14 |
| BHV-GOAL-12a | Daily goal for 30 days from 11 Nov (last day 10 Dec); today is 11 Nov | N changed to 40 | The last day moves to 20 Dec; nothing asked | B15 |
| BHV-GOAL-12b | Same goal; today is 25 Nov | N changed to 10 and the user confirms | The last day is 20 Nov and the goal is archived; days 11-20 Nov count, 21-25 Nov do not | B15 |
| BHV-GOAL-12c | Same goal; today is 25 Nov | N changed to 10 and the user declines the confirmation | The goal stays unchanged (N = 30) | new |
| BHV-GOAL-12d | Same goal; today is 25 Nov | N changed to 14 and the user confirms | The last day is 24 Nov, before today: the confirmation was asked; the goal is archived; days 11-24 Nov count, 25 Nov does not | new |
| BHV-GOAL-12e | Same goal; today is 25 Nov | N changed to 15 | The last day is 25 Nov, not before today: nothing asked; the goal stays active until the end of today and is archived on 26 Nov; days 11-25 Nov count | new |
| BHV-GOAL-13 | Fixture S changed once as in GOAL-1 | The user opens the habit form | The form shows the active goal (target 4), editable, and a read-only section with the archived goal (target 3, 26 Oct - 11 Nov); there is no restore action in the form (a goal is offered for restore only when its habit is restored, BHV-HABIT-R3) | new |
| BHV-GOAL-14 | Build binary habit, weekly goal target 3 created Mon 9 Nov (first day 9 Nov); today is Sun 15 Nov | Target to 4 | The week 9-15 is not completed (its last day is not before today): the old goal is replaced, not archived. New goal (target 4) from 9 Nov | new |
| BHV-GOAL-15 | Build numeric habit (l), daily goal 2 l created today (first day 11 Nov) | Target to 2.5 | The old goal is replaced, not archived; new goal 2.5 l from 11 Nov | new |
| BHV-GOAL-16 | Build numeric habit (l), daily goal 2 l, first day 10 Nov; today is 11 Nov | Target to 2.5 | Old goal archived, last day 11 Nov; days 10 and 11 Nov count under it with 2 l. New goal (2.5 l) starts 11 Nov (the change works from today); 11 Nov is in both goals | new |
| BHV-GOAL-17 | Daily goal 2 l for 30 days, first day 1 Nov (last day 30 Nov); today is 11 Nov | Target to 2.5 | Old goal archived, last day 11 Nov. The form of the new goal is pre-filled with 30 days; it starts 11 Nov and its last day is 10 Dec (the user may change N) | new |
| BHV-GOAL-18 | Fixture S; the habit was created before 19 Oct | Only the first day is changed, to 19 Oct | Same as a change of the target (BHV-GOAL-R4): the old goal is archived, last day 11 Nov, and counts 26 Oct and 2 Nov under target 3; the new goal (target 3) starts 19 Oct and counts 19 Oct, 26 Oct, 2 Nov and the current week | new |
| BHV-GOAL-19 | Build numeric habit (l); today the user created a weekly goal 200 l with the start 19 Oct (3 completed periods) | The user corrects the target to 20 the same day | The goal was created today, so it is replaced, not archived; the new goal (20 l) keeps the first day 19 Oct; no archive entry | new |
| BHV-GOAL-20 | As GOAL-19, but the goal was created yesterday | The user corrects the target to 20 | The old goal (200 l) is archived, last day today; the new goal starts by default in the current week (the user may pick 19 Oct) | new |
| BHV-GOAL-21 | State of BHV-GOAL-1 (the weekly goal with target 4 was created today, the old goal is archived) | The same day the user changes the period to day, target 1 | The goal with target 4 was created today, so it is replaced; the new daily goal starts 11 Nov (the period length changed); the archived goal (target 3) is unchanged | new |
| BHV-GOAL-22 | Today is 11 Nov | The user creates a daily goal for 5 days with the start 1 Nov (last day 5 Nov) | Rejected with a message (the goal would end before today) | new |

Source of all rows: user (2026-11-13), based on Decisions 15, 42, 52, 67-69 as
reworked in this conversation.

### 7.3 Periods and goal duration

Rules:
- **BHV-PERIOD-R1 Periods:** a period is a calendar period in the device time
  zone: a day; a week (Monday to Sunday; in v1 the week always starts on Monday);
  a month; a quarter (Jan-Mar, Apr-Jun, Jul-Sep, Oct-Dec); a year.
- **BHV-PERIOD-R2 Goal for N periods:** the goal lasts N whole periods of its own
  length, counted from the period in which it starts (BHV-GOAL-R1). Its last day is
  the last day of the N-th period. Months and years have no special rule: "30
  days" is 30 calendar days, so it passes through February like any other days.

Today is Wed 11 Nov 2026 unless a row says otherwise.

| ID | Given | When | Then | Source | Was |
|---|---|---|---|---|---|
| BHV-PERIOD-1 | Today is 11 Nov | The user creates a daily goal for 30 days, default start | First day 11 Nov; last day 10 Dec; nothing repeats afterwards | user (Dec. 53) | C1 |
| BHV-PERIOD-2 | Today is 20 Jan 2027 | The user creates a daily goal for 30 days, default start | First day 20 Jan; last day 18 Feb 2027; February needs no special rule | user (Dec. 53) | C2 |
| BHV-PERIOD-3 | Today is 11 Nov | The user creates a weekly goal for 8 weeks, default start | First day Mon 9 Nov; last day Sun 3 Jan 2027 | user (Dec. 53) | C3 |
| BHV-PERIOD-4 | The goal of PERIOD-1 (last day 10 Dec) | The date is 11 Dec | The goal is archived; the habit has no active goal; the form shows the goal section off (as BHV-GOAL-10) | user (Dec. 53) | C4 |
| BHV-PERIOD-5 | Today is 11 Nov | The user creates a monthly goal for 3 months, default start | First day 1 Nov; last day 31 Jan 2027 | assistant (new) | new |
| BHV-PERIOD-6 | Build habit, binary, weekly goal target 5 created today (default start) | The habit has entries on Mon 9 Nov and Tue 10 Nov | The first period is the whole week 9-15 Nov; both entries count (result so far 2) | user (Dec. 52, 53) | D1 |
| BHV-PERIOD-7 | Today is Fri 20 Nov | The user creates a monthly goal, default start | The first period is the whole of November (first day 1 Nov) | user (Dec. 53) | D2 |
| BHV-PERIOD-8 | Today is 11 Nov | The user creates a yearly goal, default start | First day 1 Jan 2026 | user (Dec. 53) | D3 |
| BHV-PERIOD-9 | Today is 11 Nov | The user creates a quarterly goal, default start | First day 1 Oct 2026 (Q4 = Oct-Dec) | assistant (new) | new |

### 7.4 Day, entries, timer and notifications

Not in this block: the defaults of the entry form (e.g. the default start of a
manual duration entry) are UI details and stay in `docs/DESIGN.md`; the timer in
statistics is in section 7.5.

Rules:
- **BHV-ENTRY-R1 Entries per day:** a duration or numeric habit can have several
  entries per day (numeric entries add up). A binary habit has at most one entry per
  day: an entry means "occurred", no entry means "did not occur".
- **BHV-ENTRY-R2 The day:** a day is a calendar day in the device time zone. A
  duration entry that crosses midnight contributes to each day the part of its
  length that falls in that day. A numeric entry has a date and a time, kept as
  entered (wall-clock time). If the device time zone changes, the days of duration
  entries are recomputed (accepted); numeric and binary entries keep their date
  (and the time of a numeric entry).
- **BHV-ENTRY-R3 Dates:** entries apply to the selected day, also a past one. The
  user can go to any date, also a future one. In v1 no entry can be added on a date
  after today and none can be moved to a later date or time (BHV-ENTRY-R5): the habits are shown with their value (normally 0; a
  duration entry that ends after midnight adds its part, BHV-ENTRY-R2) and without
  action controls (no check, no "+", no timer, no manual entry). The timer works only on
  today's date; on a past date a duration habit offers manual entry only. An existing
  entry that lies after today (possible after a change of the time zone, BHV-ENTRY-R2)
  can be edited (its value) and deleted from the entries list of its habit.
- **BHV-ENTRY-R4 Value of a day:** the total of the entries of that day plus, for a
  duration habit, the part of a running timer that falls on that day (up to now). It
  is the value of the day, not the result of a goal period. For binary: occurred /
  not occurred.
- **BHV-ENTRY-R5 Valid entries:** a manual duration entry is entered as a start and
  an end (date and time); its length (end minus start, precision one second) must be
  above 0. The start cannot be in the future; the end may be. A numeric value must be
  above 0 after rounding to 3 decimal places; negative values are not possible. A
  numeric entry has a date and a time (default: the selected date, and now on
  today's date); when an entry is added, or its date or time is changed, they cannot
  be in the future. The rule does not apply to editing only the value of an entry or
  to an import: after a change of the time zone an existing entry may lie in the
  future. Numeric input accepts both a comma and a dot as the decimal separator.
- **BHV-ENTRY-R6 Editing and deleting (REQ-M5):** a numeric entry is edited by its
  value, date and time, a duration entry by its start and end, a binary entry can only be deleted;
  every entry can be deleted. An edited entry must satisfy BHV-ENTRY-R5. After an
  edit or a deletion the results of all periods that contain the day (also completed
  ones and those of archived goals) are computed again from the entries.
- **BHV-ENTRY-R7 Overlapping time:** duration entries, and a running timer, may
  overlap in time; their lengths add up.
- **BHV-ENTRY-R8 Adding a numeric value:** the "+" button of a numeric habit adds one
  separate entry of the step size of the habit on the selected date (at the current
  time on today's date); a typed value adds one separate entry of that value with
  the date and time given (default: the selected date, and now on today's date).
  The entries of a day are listed by time. The default time of an entry added on a
  past date is a UI detail (`docs/DESIGN.md`).
- **BHV-DAY-R1 Selected date:** the Day screen follows today until the user picks a
  date. After midnight a screen that follows today moves to the new day. A date
  picked by the user stays. Picking today's date puts the screen back to following
  today. A running timer updates the displayed values live.
- **BHV-DAY-R2 Progress indicator:** the indicator of a habit on the Day screen shows
  the completion percentage of the goal period that contains the selected date (a
  period not completed yet is provisional). If several goals count a period that
  contains the date, the active goal is used when it counts that period; otherwise
  the goal with the latest first day (on equal first days the one with the shorter
  period, then the one with the later last day, then the one archived later). If no
  goal counts a period that contains the date (e.g. the date is in a period that has
  not started, before every goal, after the last goal ended, or in a period cut by a
  change), there is no indicator.
- **BHV-TIMER-R1 Timers:** several timers may run at once, at most one per duration
  habit. A timer survives closing the app (REQ-M4). Stopping a timer creates one
  duration entry (start, length). A running timer counts toward the value of its day
  and the result of its goal period up to now. In the entries list of the habit it is
  a live item that can be stopped or discarded; editing its start is not in v1. A
  timer has no maximum length.
- **BHV-TIMER-R2 Stop with a non-positive length:** if the device clock was moved back
  before the start, Stop creates no entry, a message is shown and the timer keeps
  running.
- **BHV-TIMER-R3 Habit changes:** archiving a habit with a running timer stops the
  timer normally (an entry is created, or none if the length is not positive, with a
  message). Deleting a habit discards its timer without an entry.
- **BHV-TIMER-R4 Notification (REQ-S3):** a running timer has an ongoing
  notification with the elapsed time and a Stop button; Stop there does the same as
  Stop in the app. One notification per timer.
- **BHV-TIMER-R5 Permission and dismissal:** the app asks for the notification
  permission at the first timer start, not at first launch. After a refusal the timer
  works and a one-time message says that Stop is only available in the app. If the
  user dismisses the notification, the timer keeps running; the notification comes
  back at the next start of the app (as it does after a phone restart or an app
  update). After an import, notifications match the imported timers.

Today is Wed 11 Nov 2026 unless a row says otherwise.

| ID | Given | When | Then | Source | Was |
|---|---|---|---|---|---|
| BHV-ENTRY-1 | Binary habit, today marked as done | The user taps the check again | The entry is removed; the day is "not done" | user (Dec. 14, 24) | new |
| BHV-ENTRY-2 | The Day screen shows 11 Nov (today) | The user goes to 12 Nov (arrow or calendar) | The screen shows 12 Nov with every habit at its value (0 when there are no entries) and no way to add entries | user (Dec. 40; 2026-11-13) | new |
| BHV-ENTRY-3 | Duration habit, the Day screen shows the past date 10 Nov | The user looks at the habit's controls | Manual entry is offered; starting a timer is not | user (Dec. 50) | new |
| BHV-ENTRY-4 | Numeric habit (unit l), device time zone Europe/Warsaw; an entry of 0.25 l was added on the past date 5 Nov at 23:30 | The device time zone changes to Pacific/Auckland | The entry is still on 5 Nov at 23:30 | user (Dec. 13, 50; 2026-11-13) | G6 |
| BHV-ENTRY-5 | Duration entry 20:00-21:00 on 10 Nov in Europe/Warsaw; binary entry on 10 Nov | The device time zone changes to Pacific/Auckland | The duration entry now falls on 11 Nov (days are recomputed, accepted); the binary entry stays on 10 Nov | user (Dec. 13, 44) | G5 |
| BHV-ENTRY-6 | Duration habit | The user enters a manual entry with start 10:00 and end 09:00 | Rejected (length must be above 0) | user (Dec. 51, 63) | new |
| BHV-ENTRY-7 | Duration habit; it is 11:30 today | The user enters a manual entry for tomorrow 09:00-10:00 | Rejected (the start cannot be in the future) | user (Dec. 70) | new |
| BHV-ENTRY-8 | Duration habit; it is 11:30 today | The user enters a manual entry today 11:00-15:00 | Accepted (the end may be in the future) | user (Dec. 63) | new |
| BHV-ENTRY-9a | Numeric habit | The user enters 0 | Rejected | user (Dec. 51) | new |
| BHV-ENTRY-9b | Numeric habit | The user enters -1 | Rejected | user (Dec. 51) | new |
| BHV-ENTRY-9c | Numeric habit | The user enters 0.0004 | Rejected (rounds to 0.000) | user (Dec. 46, 51) | new |
| BHV-ENTRY-9d | Numeric habit (unit l) | The user types 1,5 (Polish decimal comma) | One entry of 1.5 l | assistant (new) | new |
| BHV-ENTRY-10 | Numeric habit (unit l, step 0.25) | The user taps "+" twice on one day | Two separate entries of 0.25 l; value of the day 0.5 l | user (Dec. 25) | new |
| BHV-ENTRY-11 | Numeric habit (unit l) | The user types 1.5 on one day | One entry of 1.5 l | user (Dec. 24, 25) | new |
| BHV-ENTRY-12a | A numeric entry of 2 l | The user edits it to 3 l | The day shows 3 l | user (Dec. 27) | new |
| BHV-ENTRY-12b | A numeric entry of 2 l | The user edits it to 0 | Rejected; the entry stays 2 l | user (Dec. 51) | new |
| BHV-ENTRY-13 | A binary entry | The user opens its entry list | The entry cannot be edited, only deleted | user (Dec. 27) | new |
| BHV-ENTRY-14 | Build binary habit, weekly goal target 3; last week (2-8 Nov) has entries on 3 days, so it was achieved | The user deletes one of those entries | Last week now has 2 days: not achieved, 66%; the statistics show the new result | user (Dec. 15; 2026-11-13) | new |
| BHV-ENTRY-15 | Duration habit | Two entries on one day: 10:00-11:00 and 10:30-11:30 | The value of the day is 2 h (the overlap is counted twice) | user (2026-11-13) | new |
| BHV-ENTRY-16 | Numeric habit (unit l), 10 Nov | Two entries: 0.25 l at 8:00 and 0.3 l at 13:30 | Two separate entries listed by time (8:00, 13:30); the value of the day is 0.55 l | user (2026-11-13) | new |
| BHV-ENTRY-17 | Numeric habit; it is 11:30 on 11 Nov | The user adds an entry today at 14:00 | Rejected (the time cannot be in the future) | user (2026-11-13) | new |
| BHV-ENTRY-18 | Numeric habit | The user adds an entry for 12 Nov (any time) | Not possible (no entries after today) | user (2026-11-13) | new |
| BHV-ENTRY-19 | Duration habit; it is 23:30 on 11 Nov | The user enters a manual entry 23:00-01:00 (the end is in the future) | Accepted; 12 Nov shows 1 h on the Day screen (without action controls) and 11 Nov 1 h | assistant (new) | new |
| BHV-ENTRY-20 | Numeric habit (unit l, step 0.25); the Day screen shows the past date 10 Nov | The user taps "+" | An entry of 0.25 l is added on 10 Nov (the selected date), not on today | assistant (new) | new |
| BHV-ENTRY-21 | A numeric entry on 12 Nov at 08:30 made in Pacific/Auckland; the device is now in Europe/Warsaw, 11 Nov 21:00 (the entry lies in the future) | The user opens it from the entries list of the habit and edits only its value to 0.3 | Accepted (the future rule applies only when the date or time is changed); changing its time to 08:45 is rejected; the entry can also be deleted | assistant (new) | new |
| BHV-DAY-1 | The Day screen follows today (the user has not picked a date); it is 23:59 on 11 Nov | Midnight passes | The screen shows 12 Nov | user (Dec. 58) | new |
| BHV-DAY-2 | The user picked 5 Nov on the Day screen | Midnight passes | The screen still shows 5 Nov | user (Dec. 58) | new |
| BHV-DAY-3 | Duration habit with a manual entry of 30 min today and a running timer started 20 min ago | The user looks at the Day screen | The value of today is 50 min and keeps growing while the timer runs | user (Dec. 45, 56) | new |
| BHV-DAY-4 | Habit with an active weekly goal (first day Mon 9 Nov) | The user goes to Tue 17 Nov (the week 16-22 Nov has not started) | The habit shows value 0 and no progress indicator | user (2026-11-13) | new |
| BHV-DAY-5 | As BHV-DAY-4 | The user goes to Fri 13 Nov (the current week) | The indicator shows the progress of the current week, as on today's date | user (2026-11-13) | new |
| BHV-DAY-6 | State of BHV-GOAL-6a (old goal target 3 archived; active goal target 4 from 2 Nov) | The user goes to 3 Nov | The indicator shows the week of 2 Nov under the active goal (target 4) | user (2026-11-13) | new |
| BHV-DAY-7 | State of BHV-GOAL-1 (old goal target 3 archived, last day 11 Nov, counting 26 Oct and 2 Nov; active goal target 4 from 9 Nov) | The user goes to 28 Oct | The active goal does not count that week, so the indicator shows the week of 26 Oct under the archived goal (target 3) | user (2026-11-13) | new |
| BHV-DAY-8 | State of BHV-GOAL-2 (old weekly goal archived, last day 11 Nov; new daily goal from 11 Nov) | The user goes to 10 Nov | No goal counts a period that contains 10 Nov (the week 9-15 is cut), so there is no indicator | assistant (new) | new |
| BHV-TIMER-1 | Duration habit, timer started 23:30 on 10 Nov and still running | It is 00:20 on 11 Nov | Value of 10 Nov: 30 min; value of 11 Nov: 20 min and growing | user (Dec. 12, 56) | G1 |
| BHV-TIMER-2 | As BHV-TIMER-1 | The timer is stopped at 00:20 | One entry (start 23:30 on 10 Nov, length 50 min); 30 min fall on 10 Nov and 20 min on 11 Nov | user (Dec. 12) | G2 |
| BHV-TIMER-3 | Timer started 10:00 on 11 Nov | The device clock is moved back to 09:00 and the user taps Stop | No entry; a message is shown; the timer keeps running | user (Dec. 51) | G3 |
| BHV-TIMER-4 | Duration habit with a running timer | The habit is archived | The timer is stopped with an entry; its notification is removed | user (Dec. 51) | G7 |
| BHV-TIMER-5 | Duration habit with a running timer | The habit is deleted | The timer is discarded without an entry, together with the habit's entries; its notification is removed | user (Dec. 51) | new |
| BHV-TIMER-6 | Two duration habits, each with a running timer | The user looks at the notifications | There is one notification per timer, each with its own Stop | user (Dec. 18, 32) | new |
| BHV-TIMER-7 | Duration habit, timer started 10:00 | The app is closed (or killed by the system) and opened at 10:45 | The timer is still running; the value of today includes 45 min and keeps growing | user (REQ-M4) | new |
| BHV-TIMER-8 | Duration habit, timer started 10:00 | The phone is restarted and the app opened at 10:45 | As BHV-TIMER-7; the notification is back | user (REQ-M4, REQ-S3) | new |
| BHV-NOTIF-1 | A running timer with a notification | The user dismisses the notification | The timer keeps running; the notification comes back at the next start of the app | user (Dec. 47, 57) | H1 |
| BHV-NOTIF-2 | The app has running timers and notifications | An import replaces all data | There is one notification per imported running timer; all other notifications are removed | user (Dec. 57) | H2 |
| BHV-NOTIF-3 | The notification permission has not been decided | The user starts the first timer and refuses the permission | The timer works; a one-time message says Stop is only available in the app | user (Dec. 47) | H3 |

### 7.5 Statistics

Statistics are shown per goal (decided 2026-11-13): goals of one habit are never
added together.

Rules:
- **BHV-STATS-R1 Range:** the user chooses a date range. Presets: this week,
  previous week, this month, previous month, this quarter, this year, 6 months,
  1 year; or a custom range chosen in a calendar. Default: this week. "6 months" and
  "1 year" end today and start the same calendar day that many months or years
  earlier plus one day (on 11 Nov 2026 "6 months" is 12 May - 11 Nov 2026). When
  that calendar day does not exist, the last day of that month is used (31 Aug 2026
  minus 6 months is 28 Feb 2026, so the range is 1 Mar - 31 Aug 2026).
- **BHV-STATS-R2 Figures of a habit:** computed from the entries (a running timer
  counts up to now). Duration: total time and average per day (REQ-M8a). Binary:
  number of occurrences (REQ-M8b). Numeric: sum of values and average per day
  (REQ-M8d). Days after today are not counted. The average divides by the days of
  the range that lie within the life of the habit; if there are none, the total is
  0, there is no average and the card says so.
- **BHV-STATS-R3 Life of the habit:** from its creation day (or the day of its
  earliest entry, if earlier) to its archiving day (inclusive) or today. A habit
  restored from the archive keeps the gap as ordinary days. Statistics of archived
  habits stay available (in a separate section).
- **BHV-STATS-R4 Goal blocks (REQ-M8c):** for each goal of the habit (archived goals
  too) that has at least one counting period in the range, the card shows a block:
  the target and period length of the goal, "achieved X of Y" (Y = the counting
  periods, including the current one) and the average completion percentage. Goals
  are never added together; a goal without a counting period in the range has no
  block; if no goal has one, the card says so.
- **BHV-STATS-R5 Which periods count in a range:** a period counts in the block of a
  goal when (a) it counts for the goal (BHV-GOAL-R5), (b) its first day is in the
  range and not after today, (c) its last day is in the range, or it is the current
  period and the range reaches today or later, (d) it contains at least one day of
  the habit's life. Which periods of a goal archived with its habit count is decided
  by BHV-GOAL-R5 alone (see BHV-HABIT-R3). Periods that stick out of the range, also at its start, do not
  count. A period that starts before the habit was created is evaluated whole (as
  BHV-PERIOD-6).
- **BHV-STATS-R6 The unfinished period:** the current period counts with its result
  so far (BHV-EVAL-R2, R3) and is marked as provisional with the number of days left
  (the days after today up to the end of the period; 0 when the period ends today,
  e.g. the last period of a goal archived today). For a quit habit it counts as
  achieved while the limit is kept and can change before the period ends. The mark
  appears only when the range reaches today or later.
- **BHV-STATS-R7 Average percentage:** computed from the exact fractions of the
  counting periods and then truncated to a whole percent for display
  (BHV-EVAL-R3).

Today is Wed 11 Nov 2026; "counts" means counts in the block of that goal; the habit
was created long before the range unless a row says otherwise.

| ID | Given | When | Then | Source | Was |
|---|---|---|---|---|---|
| BHV-STATS-1 | Active daily goal | Range "this week" (9-15 Nov) | 9, 10, 11 Nov count (11 Nov provisional); 12-15 Nov start after today and never count. "X of 3" | user (Dec. 41, 55) | E1 |
| BHV-STATS-2 | Active weekly goal (first day 26 Oct) | Range "this week" | Week 9-15 Nov counts as the current period, provisional, 4 days left. "X of 1" | user (Dec. 41) | E2 |
| BHV-STATS-3 | Monthly goal, first day 1 May 2026 | Range "6 months" (12 May - 11 Nov) | June-Nov count (Nov provisional); May starts before the range and does not. "X of 6" | user (Dec. 20, 41, 55) | E3 |
| BHV-STATS-4 | Monthly goal, first day 1 Jan 2025 | Range "1 year" (12 Nov 2025 - 11 Nov 2026) | Dec 2025 - Nov 2026 count, Nov provisional. "X of 12" | user (Dec. 20, 41, 55) | E4 |
| BHV-STATS-5 | Quarterly goal, first day 1 Jan 2026 | Range "this quarter" (1 Oct - 31 Dec) | Q4 counts as the current period, provisional; only days up to today are used for the habit's figures | user (Dec. 55) | E5 |
| BHV-STATS-6 | Weekly goal, first day 26 Oct | Custom range 3-8 Nov | No period counts (the week 2-8 Nov starts before the range); no block, the card says no goal period is in the range | user (Dec. 20) | E6 |
| BHV-STATS-7 | Weekly goal, first day 7 Sep | Custom range 1-31 Oct | Weeks of 5, 12 and 19 Oct count; the week of 26 Oct sticks out of the range and does not. "X of 3" | user (Dec. 20) | E7 |
| BHV-STATS-8 | Daily goal with the first day 1 Oct and the last day 31 Oct (archived); weekly goal from 2 Nov | Custom range 1 Oct - 11 Nov | Two goal blocks, not added together: daily goal "X of 31"; weekly goal "X of 2" (week 9 Nov provisional) | user (Dec. 62; 2026-11-13) | E8 |
| BHV-STATS-9 | Fixture S after the change of BHV-GOAL-1 (old goal archived 11 Nov, new goal target 4 from 9 Nov) | Range "this week" | Only the new goal has a block ("X of 1", provisional); the old goal has no counting period (week 9-15 is cut) and no block | user (2026-11-13) | E9 |
| BHV-STATS-10 | State of BHV-GOAL-6a (old goal target 3 archived; new goal target 4 from 2 Nov) | Custom range 2-15 Nov | Old goal block: week of 2 Nov, target 3, "X of 1". New goal block: weeks of 2 and 9 Nov, target 4, "X of 2" (9 Nov provisional). The week of 2 Nov is in both blocks, each with its own target | user (2026-11-13) | E10 |
| BHV-STATS-11a | Habit created 11 Nov | Range "previous month" (October) | Total 0, no average per day; the card says the range has no day of the habit's life | user (Dec. 43) | E11 |
| BHV-STATS-11b | Habit archived on 30 Sep | Range "previous month" (October) | As BHV-STATS-11a | user (Dec. 43) | E11 |
| BHV-STATS-12 | Habit with a weekly goal, archived on 11 Nov | Range "this week" | The goal was archived with the habit (last day 11 Nov), so the week 9-15 is cut: neither achieved nor failed, not counted | user (Dec. 43; 2026-11-13) | F1 |
| BHV-STATS-13 | Habit created 11 Nov with no entries before 11 Nov, daily goal with the retrospective start 1 Nov | Range "this month" | Only 11 Nov counts ("X of 1"); 1-10 Nov are before the habit's life. For a quit binary habit they are not free successes either | user (Dec. 43) | F2 |
| BHV-STATS-14 | Duration build habit, weekly goal 5 h, 2 h of entries this week, a timer running for 30 min | Range "this week" | Total 2 h 30 min; the current week's goal result is 2 h 30 min (50%, provisional) | user (Dec. 45, 56) | G4 |
| BHV-STATS-15 | Duration habit, 1 h on Mon 9 Nov and 2 h on Tue 10 Nov, created before | Range "this week" | Total 3 h; average per day 1 h (3 days counted: 9, 10, 11 Nov) | assistant (new) | new |
| BHV-STATS-16 | Binary habit with entries on 10 different days in October | Custom range 1-31 Oct | 10 occurrences | assistant (new) | new |
| BHV-STATS-17 | Numeric habit (l), 14 l in total on 1-7 Nov, created before | Custom range 1-7 Nov | Sum 14 l; average 2 l per day | assistant (new) | new |
| BHV-STATS-18 | Habit created on 11 Nov with an entry dated 1 Nov (earlier than its creation); no other entries | Custom range 1-11 Nov | The life of the habit starts on 1 Nov (the earliest entry), so the average divides by 11 days | user (Dec. 43) | new |
| BHV-STATS-19 | Build numeric habit (l), daily goal 2 l, created long before; 2 l logged on 11 Nov | The habit is archived on 11 Nov; range "this week" | The goal's last day is 11 Nov, the last day of the period 11 Nov, so the day counts; 9, 10, 11 Nov count, 12-15 Nov do not. "X of 3"; 11 Nov is provisional with 0 days left | user (2026-11-13) | new |
| BHV-STATS-20 | Today is Sun 15 Nov; habit with a weekly goal, created long before | The habit is archived today; range "this week" | The last day of the goal is the last day of week 9-15, so the week counts. "X of 1", provisional with 0 days left | user (2026-11-13) | new |
| BHV-STATS-21 | Today is Mon 31 Aug 2026; monthly goal, first day 1 Jan 2026 | Range "6 months" | The range is 1 Mar - 31 Aug 2026 (28 Feb + 1 day); March-August count, August provisional. "X of 6" | assistant (new) | new |
| BHV-STATS-22 | Today is Tue 29 Feb 2028; monthly goal, first day 1 Jan 2027 | Range "1 year" | The range is 1 Mar 2027 - 29 Feb 2028; March 2027 - February 2028 count, February provisional. "X of 12" | assistant (new) | new |
| BHV-STATS-23 | Build binary habit, weekly goal 3 days, first day 26 Oct; entries on 3 days of the week 26 Oct, 2 days of the week 2 Nov, 1 day of the week 9 Nov; today is Wed 11 Nov | Custom range 26 Oct - 15 Nov | Weeks of 26 Oct, 2 Nov and 9 Nov count. "Achieved 1 of 3", average completion 66% ((100% + 66.6% + 33.3%) / 3 = 66.6%, truncated); the week of 9 Nov is provisional, 4 days left. Occurrences: 6 | assistant (new) | new |

### 7.6 Habits and goal validation

Rules:
- **BHV-GOALVAL-R1 Valid goal:**
  - the target of a build goal is above 0; the target of a quit goal is 0 or above
    (a limit of 0 is allowed);
  - a binary target is a whole number of days and cannot exceed the largest number
    of days of its period (day 1, week 7, month 31, quarter 92, year 366); a target
    above the length of a shorter period (e.g. 31 in February) is accepted and
    simply unreachable there;
  - a duration target has a precision of one second, is entered as hours, minutes
    and seconds (no days), and a daily goal cannot exceed 24 h;
  - the number of periods of a goal for N periods is a whole number >= 1.
- **BHV-HABIT-R1 A habit:** has a name (not empty after trimming white space;
  duplicates are allowed), a direction (build / quit) and a measurement type. A
  numeric habit also has a unit (built-in or a custom text label, not empty after
  trimming white space) and an optional "+" step (above 0, precision 3 decimal
  places). Without a step set by the user, the "+" step is the default step of the
  unit (REQ-M7; 1 for a custom unit). When the unit changes, a step that the user
  did not set follows the default of the new unit; a step set by the user stays. A
  goal is optional.
- **BHV-HABIT-R2 What can change:** the name can always change. The direction and
  the measurement type cannot change after the habit is created (a mistake is fixed
  by deleting the habit and creating a new one). Changing the unit of a numeric
  habit only relabels the existing values (no conversion) and shows a warning.
- **BHV-HABIT-R3 Archive, restore, delete:**
  - archiving keeps the entries and the statistics, removes the habit from the Day
    screen, stops its timer (BHV-TIMER-R3) and archives its active goal (its last
    day is the archiving day; this holds also for a goal without a completed
    period; this is an accepted exception to BHV-GOAL-R4a, so that the restore
    offer below has a goal to copy); a goal that has not started yet is deleted;
  - restoring brings the habit back to the Day screen; the days in between are
    ordinary days without entries. If the habit is restored on the day it was
    archived, its goal is reactivated unchanged (the archive entry is removed; a goal
    for N periods keeps its last day) and nothing is asked. Otherwise, if a goal was
    archived together with the habit, the user is asked whether to restore it too. If
    yes, a new active goal with the same target, period length and mode (repeat, or
    the same N) is created and starts in the current period (BHV-GOAL-R1); the old
    goal stays in the archive. The restored goal can be edited afterwards like any goal. If no, the habit has no active goal;
  - deleting removes the habit with its entries, goals and running timer, after a
    confirmation.
- **BHV-HABIT-R4 Order:** the Day screen lists every habit that is not archived every
  day (v1 has no weekday schedule), alphabetically by name ignoring case, with the
  letter order of the app language (in Polish Ł comes after L); habits with equal
  names are ordered by creation day, then by creation time. An imported habit keeps
  its creation day and time from the file; when these are equal too, an existing
  habit comes before an imported one.

| ID | Given | When | Then | Source | Was |
|---|---|---|---|---|---|
| BHV-GOALVAL-1a | Binary habit | The user sets a weekly target of 8 | Rejected (maximum 7) | user (Dec. 65) | I1 |
| BHV-GOALVAL-1b | Binary habit | The user sets a weekly target of 7 | Accepted | user (Dec. 65) | I1 |
| BHV-GOALVAL-2 | Binary habit with a monthly goal, target 31 | The month is February | The goal was accepted; in February it is unreachable, so the period is not achieved | user (Dec. 65) | I2 |
| BHV-GOALVAL-3a | Duration habit | The user sets a daily target of 25 h | Rejected | user (Dec. 51) | I3 |
| BHV-GOALVAL-3b | Duration habit | The user sets a daily target of 24 h | Accepted | user (Dec. 51) | I3 |
| BHV-GOALVAL-4 | Build numeric habit | The user sets a target of 0 | Rejected (a build target is above 0) | user (Dec. 51) | new |
| BHV-GOALVAL-5 | Quit numeric habit | The user sets a limit of 0 | Accepted | user (Dec. 16, 51) | new |
| BHV-GOALVAL-6 | Goal form | The user sets a goal for 0 periods | Rejected (at least 1) | user (Dec. 53) | new |
| BHV-HABIT-1 | The habit form | The user enters a name of only spaces | Rejected; the habit is not created | user (Dec. 72) | new |
| BHV-HABIT-2 | Two habits named "Reading", created on 9 Nov and 10 Nov | The user looks at the Day screen | Both are listed, the older one first | user (Dec. 72) | new |
| BHV-HABIT-3 | Habits "banana", "Apple", "cherry" | The user looks at the Day screen | Order: Apple, banana, cherry (ignoring case) | user (Dec. 24) | new |
| BHV-HABIT-4 | An existing habit | The user opens its form | The direction is shown read-only with a short explanation | user (Dec. 17) | new |
| BHV-HABIT-5 | Numeric habit (l) with an entry of 2 | The user changes the unit to ml and confirms the warning | The warning said that values are relabeled, not converted; the entry now reads 2 ml | user (Dec. 17) | new |
| BHV-HABIT-6 | Habit with entries and an active weekly goal | The habit is archived on 11 Nov | It disappears from the Day screen; its statistics stay available in the archived section; the entries are kept; the goal is archived with the last day 11 Nov | user (Dec. 17, 43; 2026-11-13) | new |
| BHV-HABIT-7a | The habit of BHV-HABIT-6, archived | The user restores it and answers yes to "restore the goal too" | The habit is on the Day screen again; a new active weekly goal with the same target starts in the current period; the old goal stays in the archive | user (2026-11-13) | new |
| BHV-HABIT-7b | As BHV-HABIT-7a | The user answers no | The habit is on the Day screen with no active goal; the archived goal stays in the archive | user (2026-11-13) | new |
| BHV-HABIT-7c | An archived habit that had no goal | The user restores it | No question about a goal is asked | user (2026-11-13) | new |
| BHV-HABIT-7d | Archived habit; its days between archiving and restoring | The user looks at its statistics after the restore | The days in between are ordinary days without entries (in the average per day); no goal period that lies entirely in the gap is counted | user (Dec. 43; 2026-11-13) | new |
| BHV-HABIT-7e | Weekly goal; the habit is archived on Tue 10 Nov and restored on Wed 11 Nov with the goal | The user looks at the statistics for "this week" | The new goal starts Mon 9 Nov and counts the whole week 9-15; the old goal (last day 10 Nov) has no counted period that week | assistant (new) | new |
| BHV-HABIT-7f | Habit with a repeating weekly goal (first day 26 Oct, two completed weeks); it is archived by mistake on 11 Nov | The user restores it the same day | The goal is active again unchanged, first day 26 Oct; no archive entry, no question; statistics show one block | assistant (new) | new |
| BHV-HABIT-8 | Habit with entries, goals and a running timer | The user deletes it and confirms | The habit, its entries, goals and timer are gone; the notification is removed | user (Dec. 17, 51) | new |
| BHV-HABIT-9 | A habit without any entry | The user opens its form | The measurement type is shown read-only with a short explanation | user (2026-11-13) | new |
| BHV-HABIT-10 | A habit with a weekly goal that has not started yet (pending) | The habit is archived | The pending goal is deleted; no archived goal remains from it | user (2026-11-13) | new |
| BHV-HABIT-11 | The form of a numeric habit | The user enters a custom unit of only spaces | Rejected | user (2026-11-13) | new |
| BHV-HABIT-12 | The form of a numeric habit | The user sets the "+" step to 0 | Rejected (the step must be above 0) | user (2026-11-13) | new |
| BHV-HABIT-13a | Numeric habit (l) with no step set by the user (step 0.25) | The user changes the unit to ml | The "+" step is now 250 (default of ml) | assistant (new) | new |
| BHV-HABIT-13b | Numeric habit (l) with the step set by the user to 0.5 | The user changes the unit to ml | The step stays 0.5 | assistant (new) | new |
| BHV-HABIT-14 | Numeric habit with a custom unit "cups", no step set by the user | The user taps "+" | An entry of 1 cup is added | assistant (new) | new |
| BHV-HABIT-15 | Habit created today with a weekly goal (no completed period) | The habit is archived | The goal is archived with the habit (last day today), not deleted; restoring the habit later offers it (on the same day the goal is reactivated, BHV-HABIT-R3) | assistant (new) | new |
| BHV-HABIT-16 | Habits "Łyżwy" and "Zumba" in the app language Polish | The user looks at the Day screen | Order: Łyżwy, Zumba | assistant (new) | new |

### 7.7 Export and import (REQ-S1)

Not in this block: the file format (JSON, version number, file model), the
identifier of the chosen file and the place of the menu items are design
(`docs/DESIGN.md` sections 3, 6 and 7).

Rules:
- **BHV-EXPORT-R1 Export:** the user can write all data of the app to a file:
  habits (with the creation day and time), their goals (archived ones too, with the
  creation day), entries and running timers. A file made
  by an earlier version of the app can be imported by a later version (backups stay
  valid after updates).
- **BHV-IMPORT-R1 Checking the file:** the file is checked first. An invalid file, a
  file that is not a backup of this app, or a file from a newer, unsupported version
  is rejected with a message; no choice is offered and the current data stay
  unchanged.
- **BHV-IMPORT-R2 Choice of the import mode:** for a valid file the app shows a short
  summary (export date, number of habits and entries) and, when some habits in the
  file have the same name as habits already in the app (ignoring case and spaces at
  the ends), a line saying how many. The user chooses one of three actions, none
  preselected:
  - *Replace all*: all current data are replaced by the content of the file, after a
    warning that current data will be lost and a confirmation;
  - *Add to existing*: the habits of the file are added as new habits, each with its
    entries, goals and running timer, next to the existing ones. Nothing existing is
    changed or deleted. Habits with equal names are allowed, so importing the same
    file twice duplicates its habits. Imported habits are not matched with existing
    ones;
  - *Cancel*: nothing changes.
- **BHV-IMPORT-R3 All or nothing:** either mode is completed fully or not at all.
  After an import the timer notifications match the running timers (BHV-TIMER-R5).
- **BHV-IMPORT-R4 What a valid file satisfies:** every rule of the app holds for its
  content:
  - values and goals are valid (BHV-ENTRY-R5 except every rule "not in the future":
    the start of a duration entry, the date and time of a numeric entry;
    BHV-GOALVAL-R1); a numeric entry has a date and a time; a goal starts on the
    first day of a period of its length; the last day of an active goal for N
    periods is the last day of its N-th period; the last day of an archived goal is
    any day not before its first day; every habit and goal has a creation day (an
    imported one keeps it from the file);
  - a habit satisfies BHV-HABIT-R1 (name, unit, step);
  - a binary habit has at most one entry per day; the shape of every entry matches
    the measurement type of its habit;
  - a habit has at most one active goal, and an archived habit has none;
  - a running timer belongs to a duration habit that is not archived, and there is
    at most one per habit;
  - every entry, goal and timer refers to a habit that is in the file.

| ID | Given | When | Then | Source | Was |
|---|---|---|---|---|---|
| BHV-IMPORT-1 | A file with a numeric entry (value 3) of a binary habit | The user picks it | Rejected with a message; no choice offered; current data unchanged | user (Dec. 31, 49) | J1 |
| BHV-IMPORT-2a | A file with a negative numeric value | The user picks it | Rejected; current data unchanged | user (Dec. 49) | J2 |
| BHV-IMPORT-2b | A file with a binary goal with target 8 and a weekly period | The user picks it | Rejected; current data unchanged | user (Dec. 49) | J2 |
| BHV-IMPORT-3a | A file with a running timer of a numeric habit | The user picks it | Rejected; current data unchanged | user (Dec. 49) | J3 |
| BHV-IMPORT-3b | A file with a running timer of an archived duration habit | The user picks it | Rejected; current data unchanged | user (Dec. 49) | J3 |
| BHV-IMPORT-4a | A file with an entry of a habit that is not in the file | The user picks it | Rejected; current data unchanged | user (Dec. 49) | J4 |
| BHV-IMPORT-4b | A file with a goal or a timer of a habit that is not in the file | The user picks it | Rejected; current data unchanged | user (Dec. 49) | J4 |
| BHV-IMPORT-5 | A valid file; the app has other data | The user chooses *Replace all* and confirms | The data of the app are replaced by the file's; all rules hold afterwards | user (Dec. 31, 49) | J5 |
| BHV-IMPORT-6 | A file with two active goals of one habit | The user picks it | Rejected; current data unchanged | assistant (new) | new |
| BHV-IMPORT-7 | A file with two entries of one binary habit on the same day | The user picks it | Rejected; current data unchanged | assistant (new) | new |
| BHV-IMPORT-8 | A file with an active goal on an archived habit | The user picks it | Rejected; current data unchanged | assistant (new) | new |
| BHV-IMPORT-9 | A file from a newer, unsupported version, or a file that is not a backup | The user picks it | Rejected with a message; current data unchanged | user (Dec. 31) | new |
| BHV-IMPORT-10 | A valid file | The user sees the summary and chooses *Cancel* | Nothing changes | user (Dec. 31) | new |
| BHV-IMPORT-11 | The app has habit "Reading" with 5 entries; the file has habit "Reading" (created 10 Nov) with 3 entries and a weekly goal; the existing "Reading" was created 9 Nov | The user chooses *Add to existing* | The app has two habits "Reading": the existing one still has its 5 entries and its goals unchanged; the new one has 3 entries and the goal and keeps its creation day 10 Nov from the file. The existing one is listed first (older creation day) | user (2026-11-13) | new |
| BHV-IMPORT-12 | The app has habits; the file has a running timer of a duration habit | The user chooses *Add to existing* | The imported habit has a running timer; there is one notification per running timer, existing and imported | user (2026-11-13) | new |
| BHV-IMPORT-13 | The app has habits; the file has an invalid entry | The user picks it | Rejected before any choice; current data unchanged | user (2026-11-13) | new |
| BHV-IMPORT-14 | The app already contains the data of a file | The user chooses *Add to existing* for that file, twice | Each import adds its habits again: after two imports every habit of the file exists three times (original and two copies) | user (2026-11-13) | new |
| BHV-IMPORT-15 | The app has habits "Reading" and "Water"; the file has "reading ", "Sport" and "Water" | The user picks the file | The summary says that 2 habits in the file have the same name as existing habits | user (2026-11-13) | new |
| BHV-IMPORT-16 | A file with a numeric entry dated 12 Nov 08:30; it is 11 Nov 21:00 | The user picks it | Accepted (the "not in the future" rule does not apply to an import) | assistant (new) | new |
| BHV-IMPORT-17 | A file with a weekly goal, first day Mon 26 Oct, archived, last day Wed 11 Nov | The user picks it and chooses *Replace all* | Accepted; the goal is in the archive with the last day 11 Nov | assistant (new) | new |
| BHV-IMPORT-18 | A file with a duration entry whose start is later than now (the clock was moved back before the export); it is 11 Nov 21:00 | The user picks it | Accepted (the "not in the future" rules do not apply to an import) | assistant (new) | new |
| BHV-IMPORT-19 | A file with a weekly goal (target 3, first day 26 Oct, created 26 Oct, two completed weeks); the user chose *Add to existing* on 11 Nov | The same day the user changes the target of the imported goal to 4 | The goal keeps its creation day from the file, so it is archived (not replaced); the new goal (target 4) starts 9 Nov | assistant (new) | new |
| BHV-EXPORT-1 | An app with habits of all three types, goals (active and archived), entries and a running timer | The user exports and then imports the file into an empty app with *Replace all* | The same habits, goals (also archived), entries and running timer | user (Dec. 30) | new |
| BHV-EXPORT-2 | A file made by an earlier format version of the app | The user imports it in a later version | Imported; the data are as in the file | user (Dec. 30) | new |

### 7.8 Language (REQ-M9, REQ-C4)

Where the switch lives in the app (a settings screen, a menu item) is a design
decision; the current design has no settings screen. Whether the system's per-app
language setting (available from Android 13) can be used must be verified in the
official Android documentation before the plan.

Rules:
- **BHV-LANG-R1 Default:** until the user chooses a language in the app, the app
  uses the language of the device when it is Polish or English, and English for any
  other language; this follows a later change of the device language.
- **BHV-LANG-R2 Switch:** the user can switch the language of the app between Polish
  and English at any time. All texts of the app, including the timer notifications,
  use the chosen language. The choice stays after the app is closed and from then
  on does not follow the device language.
- **BHV-LANG-R3 Data:** the language is not part of the exported data. Names of
  habits and custom unit labels are the user's text and are never translated.

| ID | Given | When | Then | Source | Was |
|---|---|---|---|---|---|
| BHV-LANG-1 | The device language is Polish; first start | The user opens the app | The app is in Polish | user (REQ-C4) | new |
| BHV-LANG-2 | The device language is German; first start | The user opens the app | The app is in English | assistant (new) | new |
| BHV-LANG-3 | The device language is Polish; the app is in Polish | The user switches to English in the app | All texts of the app are in English; the names of the user's habits are unchanged | user (2026-11-13) | new |
| BHV-LANG-4 | The user switched to English on a Polish device | The app is closed and opened again | The app is still in English | user (2026-11-13) | new |
| BHV-LANG-5 | The app is in English; a backup was exported earlier in Polish | The user imports it | The language of the app is unchanged | assistant (new) | new |
| BHV-LANG-6 | The device language is Polish; the user never chose a language in the app | The device language is changed to English | The app is in English | user (2026-11-13) | new |
| BHV-LANG-7 | The user chose Polish in the app; the device language is English | The device language is changed to German | The app stays in Polish | user (2026-11-13) | new |

## 8. Ideas for later

- iOS version.
- Publication in Google Play.
- Monetization (e.g. limited number of habits in the free version, paid Pro version).
- Reminder notifications (reminders to log a habit).
- Reminder about a long-running timer: a system alarm and notification after a
  set time (e.g. 12 h), useful when the user forgot both the timer and the app
  (v1: no limit, a forgotten timer is corrected by editing the entry).
- Minimum session length per habit: when a timer is stopped below the set
  threshold (e.g. under 1 minute for reading), the session is discarded and
  no entry is created (v1: every stopped timer creates an entry).
- Goal interval: custom length (e.g. every 10 days).
- Goal interval: arbitrary set of dates.
- Goal target "every day of the period" for binary habits (v1: a number of days,
  so a monthly target of 29-31 is unreachable in shorter months).
- Goal for a range of concrete dates.
- Parallel goals for one habit (possibly Pro).
- Restoring or editing an archived goal on its own (v1: archived goals are
  read-only; the only restore is the offer made when a habit is restored, which
  creates a new goal as a copy; a retroactive correction is done by creating a new
  goal with an earlier start).
- Statistics that add up the goals of one habit with the same period length
  across target changes, or a user choice between that and the per-goal view
  (v1: statistics per goal, never aggregated).
- User choice of how the first, partial period of a new goal counts (v1: the
  whole period counts, also days before the goal was created).
- Habit configuration for recurring weekdays.
- Scale / rating habit type (e.g. mood 1-5).
- Checklist habit type (habit made of sub-tasks).
- Calendar screen: minimalist calendar marking on which days habits were done, and
  which were missed if assigned to fixed days.
- Colors and icons for habits (needed for the calendar screen, to mark completed habits
  in day cells).
- Streaks.
- Charts in statistics.
- Dark theme.
- Home screen widget.
- Categories, grouping, tagging.
- Import that recognizes the same habit in the file and in the app and merges their
  data (v1: import either replaces all data, after a warning and confirmation, or
  adds the imported habits as new ones; it never matches habits).
- Cloud backup / import through provider APIs (Google Drive, Dropbox, etc.).
- Custom units with conversion between them (Pro).
- Conversion between built-in units of the same kind (e.g. ml and l): converting
  existing history when the unit of a habit is changed, and showing statistics
  in a unit other than the habit's own (v1: changing the unit only relabels
  the values, with a warning).
- Goal without a period (cumulative, e.g. "1000 books").
- User setting for the first day of the week (v1: always Monday).
- Statistics option: let the user choose whether the goal completion percentage
  of build habits is capped at 100% (v1: always capped).
- Settings screen with a user choice of the default start screen (v1: always the
  Day screen).
- Logging entries for future dates, e.g. planned values (possible Pro feature;
  v1: not possible, see BHV-ENTRY-R3).
