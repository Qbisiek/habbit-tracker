# UX: Habit Tracker (working title)

> **STATUS (2026-11-16): DESIGNED IN SESSION, NOT REVIEWED, NOT APPROVED.** This
> document was created from section 6 of `docs/DESIGN.md`. On 2026-11-16 the open UX
> points were settled with the user in blocks 1-6 (see section 7), but the document
> has not been reviewed by `docs-reviewer` and the user has not approved it. Plan and
> state: `docs/STATUS.md`.

**What this document is for:** the screens, the navigation and the layout of the
app, so that it is intuitive, simple, motivating and user-oriented. It does not
state behavior rules (they are in `docs/REQUIREMENTS.md` section 7, IDs `BHV-*`)
and not the structure of the code (`docs/DESIGN.md`). Choices are logged in
`docs/DESIGN-DECISIONS.md` (cited as "Decision N").

Terminology: "habit" means a habit that is not archived; "archived habit" is the
exception. The word "active" is used only for a goal (the active goal,
BHV-GOAL-R3). "Available" may be used where a qualifier is needed.

## 1. UI principles

Stated by the user; they apply to every screen.
1. The app must be intuitive and simple. Minimal friction (see "Key quality" in the
   requirements): the input method follows the habit type, the common action is one
   tap.
2. The app must support the user in building and quitting habits. Feedback is
   honest and motivating: no scolding text, but no false comfort either. Colour
   shows the real state, red included for an exceeded limit (user, 2026-11-16).
3. Feedback is given in context, when it arises (user, 2026-11-16). An invalid choice
   is prevented where it is made: a value that would be invalid is disabled (greyed
   out) and, where the reason is not obvious, a short message stands next to it. This
   holds for blocks, warnings, confirmations and notifications alike; the screen
   carries no permanent hint text and an error is not left to appear after Save. A
   confirmation dialog is used only where a rule requires one (BHV-GOAL-R6, "Replace
   all" in BHV-IMPORT-R2).

*Identifiers of UI elements (Decision 85).* An element that the plan or a test refers
to has an identifier `UI-<SCREEN>-<ELEMENT>`, in capital letters and without numbers,
given once in the text where the element is described (in brackets). The plan cites
them like `BHV-*`; identifiers in the code follow from the implementation and need not
be the same.

## 2. Navigation (Decision 23)

Bottom navigation bar (UI-NAV-BAR) with three tabs: Day, Habits, Statistics. No swiping between
tabs. Day is always the start screen. Creating, editing, archiving and deleting
habits happens on the Habits screen only. Export / import is a menu item on the
Habits screen (Decision 30). There is no separate screen for past days: the Day
screen shows the selected date, changed with previous / next arrows or a calendar
picker (BHV-DAY-R1). A setting for the default start screen is out of v1 (in "Ideas
for later").

*Room for later (REQ-X6; no design now).* The date header of the Day screen is the only
entry point to a date, so a month calendar with marked habits can replace the picker
later without changing the rest of the Day screen. Nothing is reserved on the row for an
icon or a colour in v1 (an empty slot would cost width); the row can get a leading
element later without a new layout.

**Empty state (UI-DAY-EMPTY, Decision 23):** the Day screen shows an empty state only when there is
no habit that is not archived. With no habits at all the text says so and the button
creates a habit. If all habits are archived the text says so and the button leads to
the Habits screen (to restore one). With at least one habit there is no such button.
Having no entries for a day is a normal state and shows nothing special.

## 3. Day screen (Decisions 24, 25, 26, 45)

A list of all habits not archived. The order and the habits shown are defined by
BHV-HABIT-R4. The rules behind the values are BHV-ENTRY-R3, R4, R8, BHV-DAY-R1,
R2 and BHV-EVAL-R3; this section describes the layout only.

*Header (UI-DAY-HEADER).* The selected date, previous / next arrows and the calendar
picker (BHV-DAY-R1).

*Row (UI-DAY-ROW).* Each row has the habit name, an action control that depends on the type, the
value of the selected day and a small progress indicator. It must fit the width of a
phone. Tapping the row (outside the action control) opens the bottom sheet of the
habit's entries (Decision 27); the action control is a separate touch area that
performs the action.

*Action controls (UI-DAY-ACTION).*
- Binary: tap toggles a mark (entry exists or not).
  - Build: a square checkbox with a check.
  - Quit: a chip with the text "It happened" (Polish: "Zdarzyło się"); once marked,
    the chip is shown pressed, in a neutral colour, without a check. Text instead of
    a symbol, so it cannot be mistaken for the green check of the indicator.
- Numeric: a "+" button adds one step as a separate entry (BHV-ENTRY-R8), plus a way to
  type a value, which adds an entry too (nobody taps "+" 200 times).
- Duration: start / stop of the timer, plus manual entry of a time. While a timer
  runs, its session time is shown next to Stop. On a date where the timer is not
  offered (BHV-ENTRY-R3) only manual entry is shown.

*Value (UI-DAY-VALUE).* Every row shows the value of the selected day as a short text (e.g.
"1.25 l", "1 h 20 min"; for Binary the check itself), whether or not the habit has a
goal (BHV-ENTRY-R4). It is the value of the day, not the result of the goal period;
the period is shown by the indicator.

*Progress indicator (UI-DAY-INDICATOR, Decision 82).* A round ring with the number inside, compact, no
descriptive text, not tappable and without a button frame; the square checkbox and the
chip of the action controls look different, so the indicator is not mistaken for a
button. What it shows is defined by BHV-DAY-R2.
- Build: the ring fills with the completion percentage; at 100% it is a full green ring
  with a check, otherwise the percentage is inside.
- Quit: the ring fills with result / limit and the text inside is the fraction (e.g.
  "1/3"). Colour: green at 0, towards amber as the result grows, amber at the limit,
  red above it. The numbers are always shown, so colour is not the only carrier
  (colour-blind users).
- There is no separate "in progress" mark: a partly filled ring says it. When there is
  no indicator (BHV-DAY-R2) the row shows none, but an empty place of the width of the
  ring stays, so the rows do not shift between dates.

*Other dates.* The selected date follows today until the user changes it
(BHV-DAY-R1). On a date after today the habits are shown with their value and without
action controls (BHV-ENTRY-R3); no explanatory text is added (UI principle 3).

## 4. Entries of one habit on one day (Decision 27)

Tapping a habit row on the Day screen opens a bottom sheet (UI-ENTRIES-SHEET) with the entries of that
habit on the selected date. What can be edited is defined by BHV-ENTRY-R6; any entry
can be deleted and an entry can be added manually. A running timer is a live item
(BHV-TIMER-R1). Any date is reached with the previous / next arrows or the calendar
picker (Decision 23), so old entries need no separate screen. The picker has no
markers for days with entries in v1; a month calendar with habits marked on their
days is in "Ideas for later" ("Calendar screen", REQ-X6) and would be a later
replacement of the picker, needing a query for the days that have entries.

*Forms of a manual entry (Decision 82).* Binary has no form: the tap adds or removes the
mark (BHV-ENTRY-R6).
- Duration (UI-ENTRY-FORM-DURATION): two rows, Start and End, each with a date and a time field (editable by
  hand; the date is needed because an entry can cross midnight). Under Start the
  buttons -5 min, -15 min, -30 min, -1 h move the start earlier; under End the buttons
  +5 min, +15 min, +30 min, +1 h move the end later, but never after now: a button that
  would do so is disabled, so on today's date, with the end at now, they are disabled
  until the end is moved earlier by hand (BHV-ENTRY-R5). Taps add up. The calculated
  length is shown. Defaults: start = end = now on today's date, 12:00 on a past date.
  In the date and time pickers a moment after now is greyed out and cannot be chosen
  (UI principle 3). While the length is 0, Save is disabled and the length field says
  "Length must be above 0" (BHV-ENTRY-R5). The buttons are fixed in v1
  (configurable per habit is in "Ideas for later"). Quick check on a prototype: eight
  buttons in two rows of four must fit the width of a phone.
- Numeric (UI-ENTRY-FORM-NUMERIC): the value, and one moment (date and time field). Default: now on today's
  date, 12:00 on a past date. A moment after now is greyed out in the pickers. No
  increment buttons ("+" is on the Day screen row).

*Stop from the notification with a non-positive length (BHV-TIMER-R2).* The text of
the timer notification (UI-NOTIF-TIMER) changes to "Not stopped: the length is 0. The timer is running." When
the app is open, a snackbar shows the same message. Assumption to verify before the
plan: a receiver can update the text of the notification in a simple way.

## 5. Habits screen (Decision 28)

- A list of habits (UI-HABITS-LIST; not archived), alphabetical as on the Day screen
  (BHV-HABIT-R4), a "+" button to create one, a collapsible section "Archived"
  (UI-HABITS-ARCHIVED; also alphabetical; restore; their statistics stay available).
  Tapping a row opens the edit form. Archiving and deleting (with confirmation) are in
  the edit form. Export / import is an item of the menu (UI-HABITS-MENU, Decision 30); the choice of the import mode is
  BHV-IMPORT-R2.
- One form for creating and editing (UI-HABIT-FORM). The form has: name, direction (Build / Quit),
  measurement type; for Numeric also the unit (built-in or custom label) and the "+"
  step; last, an optional goal section, off by default: target value, period (day /
  week / month / quarter / year) and how long the goal lasts (repeats, or runs for a
  number of periods). The start defaults to the current period; the form always
  shows the calendar period the chosen date selects (BHV-GOAL-R1).
- The goal section shows the active goal, editable, and a read-only section with the
  archived goals of the habit (BHV-GOAL-R7). A goal is created, changed or switched
  off at any time in the edit form, not only when the habit is created. What a change
  does is defined by BHV-GOAL-R4, R4a and R6 (no question is asked, except the one
  confirmation of BHV-GOAL-R6). Restoring an archived habit asks whether to restore
  its goal (BHV-HABIT-R3).
- Fields that cannot change are shown read-only with a short explanation: direction
  and measurement type after creation (BHV-HABIT-R2; BHV-HABIT-4, 9). Changing a
  numeric unit shows a warning (relabel only, BHV-HABIT-5). Editing a goal changes it
  on the same habit; no new habit is created. Saving the form (habit and goal) is one
  all-or-nothing operation (`docs/DESIGN.md` section 3).

*Layout of the form (Decision 83).* One scrolling screen in three parts, "Save" at the
bottom; archiving and deleting are in the "⋮" menu of the top bar, not among the fields.
1. Basics: name, direction, type (read-only after creation, with a short
   explanation), for Numeric the unit and the "+" step.
2. Goal (UI-HABIT-GOAL): a switch "Goal". When on: target, period, duration (repeats / N periods). Under
   the period field the calendar period that the start selects is shown, e.g. "Period:
   16-22 Nov" (BHV-GOAL-R1).
3. Archived goals (UI-HABIT-GOALS-ARCHIVED): a collapsible, read-only section, one line per goal, e.g. "target 3 /
   week, 26 Oct - 11 Nov". Not editable (BHV-GOAL-R7). Absent when the habit has none.

*Dialogs of the Habits screen.*
- Restoring an archived habit (UI-RESTORE-DIALOG, BHV-HABIT-R3), only when the habit is restored on another
  day than it was archived and a goal was archived with it: "Restore the goal too
  (3 / week, repeats)? It starts in the current period." Buttons: "With goal",
  "Without goal", "Cancel". The goal is described so the user knows what comes back.
- Change of the last day of a goal (UI-GOAL-CONFIRM, BHV-GOAL-R6): "The last day of the goal will be X;
  later periods will not count." Buttons: "Change", "Cancel".

*Language (BHV-LANG-R2, Decision 84).* The menu of the Habits screen has an item
"Language" next to export and import. It opens a dialog (UI-LANGUAGE-DIALOG) with two choices, Polski and
English; the choice calls the per-app locale API of Android, so it stays after the app is
closed and is the same as in the system settings (Settings > Apps > the app > Language,
available from Android 13 through `generateLocaleConfig`). Going back to "follow the
device language" is possible only in the system settings. There is no settings screen.

*Import (UI-IMPORT-SUMMARY, BHV-IMPORT-R2).* After a valid file is picked, a summary screen: export date,
number of habits and entries and, when some names equal those in the app, a line "N habits
have the same name as existing ones". Three equal buttons, none highlighted: "Replace
all" (then the warning and confirmation of BHV-IMPORT-R2), "Add to existing", "Cancel".

## 6. Statistics screen (Decision 29)

One screen. At the top a date range selector (UI-STATS-RANGE, BHV-STATS-R1: presets and a custom
range chosen in a calendar; the default is this week). Below, one card per habit
(UI-STATS-CARD), with archived habits in a separate section; the cards are in alphabetical order, as on
the Day screen (BHV-HABIT-R4). The range is common to all cards. A card
shows only figures that make sense for the habit (BHV-STATS-R2): total time and
average per day for Duration, the number of occurrences for Binary, the sum and the
average per day for Numeric. If the habit has goals with a counting period in the
range, the card shows one block per goal (BHV-STATS-R4): the target and the period
length, "achieved X of Y" and the average completion percentage. The current
unfinished period is included and marked as provisional, with the days left
(BHV-STATS-R6). When no goal period counts, or the range has no day of the habit's
life, the card says so (BHV-STATS-R2, R4). Charts are out of v1 ("Ideas for later",
REQ-X12).

*Card with several goals (Decision 83).* The card has a fixed header (total or number)
and below it one block per goal (UI-STATS-GOAL): e.g. "Goal 3 / week - achieved 2 of 3 - average 87%".
The blocks of archived goals are in a collapsible sub-block "Earlier goals" on the same
card. The current, unfinished period is marked "in progress, 4 days left".

## 7. Open UX points

All points that were open are settled (below); only the verification before the plan
remains.

Settled in blocks 1-3 (2026-11-16, sections 3-6 above): defaults and forms of manual
entries, the look of the indicator, no "in progress" mark, the Binary control of a quit
habit, the order of the Habits list and Statistics cards, the message of a failed Stop
from the notification.

Settled in block 5 (2026-11-16): the language switch (section 5, Decision 84) and the
room for the calendar, colours and icons (section 2). To verify before the plan: that the
timer notification texts follow the app language set with the per-app locale API
(BHV-LANG-R2; the official page does not say).

Settled in block 6 (2026-11-16): identifiers of the UI elements (section 1, Decision 85).

New with the revised requirements: settled in block 4 (sections 3-6 above): the habit form
with goals, the restore and BHV-GOAL-R6 dialogs, the import mode screen, the statistics
card with several goals, the Day screen on a date after today (no action controls, no
text, UI principle 3), the row with no indicator (empty place of the ring's width) and
the entries list with an entry after today (no special layout; possible only after a
change of the time zone, BHV-ENTRY-R3).
