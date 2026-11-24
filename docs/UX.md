# UX: Habit Tracker (working title)

> **STATUS (2026-11-24): APPROVED.** Designed with the user on 2026-11-16, reviewed by
> `docs-reviewer` four times (2026-11-16 to 2026-11-23), all findings resolved with the user, approved on 2026-11-24. To verify before the
> plan: section 8. State: `docs/STATUS.md`.

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
   tap. A message or dialog that suggests a way out offers it as one tap (e.g. "Export
   and replace", "Archive" instead of a hint to go and find that action; user,
   2026-11-23, Decision 88).
2. The app must support the user in building and quitting habits. Feedback is
   honest and motivating: no scolding text, but no false comfort either. Colour
   shows the real state, red included for an exceeded limit (user, 2026-11-16).
3. Feedback is given in context, when it arises (user, 2026-11-16). An invalid choice
   is prevented where it is made: a value that would be invalid is disabled (greyed
   out) and, where the reason is not obvious, a short message stands next to it. This
   holds for blocks, warnings, confirmations and notifications alike; the screen
   carries no permanent hint text and an error is not left to appear after Save. A
   dialog is used only where a rule requires a confirmation, a question or a choice:
   the change of a goal's last day (BHV-GOAL-R6), deleting a habit (BHV-HABIT-R3),
   changing the unit of a numeric habit when values are relabeled (BHV-HABIT-R2), "Replace all"
   (BHV-IMPORT-R2), restoring a habit with its goal (BHV-HABIT-R3) and the language
   (BHV-LANG-R2). A message that the user must not miss is a dialog with one button
   too: a rejected import file (BHV-IMPORT-R1) and the one-time message about
   notifications (BHV-TIMER-R5). All others are snackbars or text at the field. Every
   dialog and message is listed in section 7. An action that can be undone (deleting
   an entry, discarding a timer; BHV-ENTRY-R6a, BHV-TIMER-R1) gets no dialog but a
   snackbar with "Undo" for a few seconds (Decision 86).

*"Rejected" in the requirements (Decision 87).* Where a `BHV-*` rule or row says an input
is rejected, the UI does not let it be saved:
- a rule on one field: a choice from a list or a picker that would be invalid is
  disabled (principle 3); a typed value cannot be disabled, so Save is disabled and a
  short message stands at the field;
- a rule on two or more fields (e.g. the binary target against the period, the start
  and N against today, a date and a time against now): no choice is disabled, since
  each value may be valid with another one; Save is disabled and the message stands
  at a fixed place where the problem is seen (the target; the "Last day" line; the
  time field);
- two terms are used for a field of a form:
  - *touched*: its value has differed from the value it had when the form opened, at
    any moment; it stays touched. It decides only whether a message is shown: a
    message appears only after the user has touched one of the fields of its rule
    (typing "R" in an empty name and deleting it shows "Enter a name"). A form that
    opens with an invalid default (an empty name, a Duration entry of length 0) only
    has Save disabled, without a message;
  - *changed*: its value at Save differs from the stored value (for a time, the hour
    and minute shown). It decides which rule applies and what is stored
    (BHV-ENTRY-R5, R6b). A field set back to its stored value is not changed;
- a check runs only on what the user changes: an existing entry that already lies
  after now (after a change of the time zone) opens with Save enabled, and only a
  change of its date or time is checked against now (BHV-ENTRY-R5, BHV-ENTRY-21a,
  21b, 21d).
Where an input filter makes a value impossible (no minus sign, whole numbers only),
no message is needed. The validations of the habit form and the entry forms are
listed in section 5.

*Identifiers of UI elements (Decision 85).* An element that the plan or a test refers
to has an identifier `UI-<SCREEN>-<ELEMENT>`, in capital letters and without numbers,
given once in the text where the element is described (in brackets). `<SCREEN>` is
one word from a closed list: NAV (navigation bar), DAY, ENTRIES (entries sheet),
ENTRYFORM (manual entry forms), HABITS (Habits screen), HABITFORM (habit form, also its
read-only view of an archived habit), STATS,
IMPORT (import summary), DIALOG (the dialogs of the app listed in section 7; a picker
belongs to the screen it is opened from, e.g. UI-DAY-PICKER), MSG (snackbars), NOTIF
(the system notification). `<ELEMENT>` may have hyphens (UI-HABITFORM-ARCHIVED-GOALS).
Each situation has its own identifier, also when two look alike (UI-MSG-EXPORT-DONE,
UI-MSG-EXPORT-FAILED). Dialogs, snackbars and empty states are defined in the table of
section 7 (some of them are also named, as a reference, where their screen is
described); the other elements are defined in sections 2-6. The plan cites
them like `BHV-*`; identifiers in the code follow from the implementation and need not
be the same.

## 2. Navigation (Decision 23)

Bottom navigation bar (UI-NAV-BAR) with three tabs: Day, Habits, Statistics. No swiping between
tabs. Day is always the start screen. Creating, editing, archiving and deleting
habits happens on the Habits screen only. Export / import is a menu item on the
Habits screen (the file format is Decision 30). There is no separate screen for past
days: the Day screen shows the selected date, changed with previous / next arrows or
a calendar picker (BHV-DAY-R1, Decision 27). A setting for the default start screen is out of v1 (in "Ideas
for later").

*Room for later (REQ-X6; no design now).* The date header of the Day screen is the only
entry point to a date, so a month calendar with marked habits can replace the picker
later without changing the rest of the Day screen. Nothing is reserved on the row for an
icon or a colour in v1 (an empty slot would cost width); the row can get a leading
element later without a new layout.

**Empty state (UI-DAY-EMPTY-NONE, UI-DAY-EMPTY-ARCHIVED; Decision 23):** the Day screen shows an empty state only when there is
no habit that is not archived. With no habits at all the text says so and the button
goes to the Habits tab and opens the form of a new habit. If all habits are archived the text says so and the button leads to
the Habits screen (to restore one). With at least one habit there is no such button.
The texts, and the empty states of the Habits and Statistics screens, are in section 7.
Having no entries for a day is a normal state and shows nothing special.

## 3. Day screen (Decisions 24, 25, 26, 45)

A list of all habits not archived. The order and the habits shown are defined by
BHV-HABIT-R4. The rules behind the values are BHV-ENTRY-R3, R4, R8, BHV-DAY-R1,
R2 and BHV-EVAL-R3; this section describes the layout only.

*Header (UI-DAY-HEADER).* The selected date, previous / next arrows and the calendar
picker (UI-DAY-PICKER, BHV-DAY-R1).

*Row (UI-DAY-ROW).* Each row has the habit name, an action control that depends on the type, the
value of the selected day and a small progress indicator. It must fit the width of a
phone. Tapping the row (outside the action control) opens the bottom sheet of the
habit's entries (Decision 27); the action control is a separate touch area that
performs the action.

*Action controls (UI-DAY-ACTION).*
- Binary: tap toggles a mark (entry exists or not). Unmarking shows no "Undo"
  snackbar: tapping again is the undo (BHV-ENTRY-R6a).
  - Build: a square checkbox with a check.
  - Quit: a chip with the text "It happened" (Polish: "Zdarzyło się"); once marked,
    the chip is shown pressed, in a neutral colour, without a check. Text instead of
    a symbol, so it cannot be mistaken for the green check of a build habit's
    indicator.
- Numeric: a "+" button adds one step as a separate entry (BHV-ENTRY-R8; at now on
  today's date, at 12:00:00 on a past date, like the default of the numeric form), plus a way to
  type a value, which opens UI-ENTRYFORM-NUMERIC and adds an entry too (nobody taps
  "+" 200 times).
- Duration: start / stop of the timer, plus manual entry of a time, which opens
  UI-ENTRYFORM-DURATION. While a timer
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
- Quit (the three states of BHV-DAY-R2):
  - Text: "result/limit" in a short format: Binary and Numeric as exact numbers
    without the unit ("1/3", "1.5/3"); Duration with compact units h, m, s and no
    spaces ("1h20m/3h"), since a bare "0:40" could be minutes or seconds. Units that
    are 0 are left out ("3h", "1h30s"); a duration of 0 is "0". The letters are the
    same in English and Polish. With limit 0 only the result ("0", "1", "40s"), since
    "1/0" reads as an error.
  - Duration rounding, so that the text always tells the state (colour is never the
    only carrier): within the limit the result is cut down to the minute; above the
    limit it is rounded up ("3h1m/3h" for 3 h 0 min 20 s). When the limit has
    seconds or is under 1 min (limit 0 included), the result and the limit are shown
    to the second, without rounding ("40s" for 40 s at limit 0, "20m/1h30s").
    BHV-DAY-11, 12a, 12b. Long
    numbers (e.g. "1250/2000") are to be checked on the prototype; if they do not
    fit, the fallback is the result in the ring and the limit as small text beside it.
  - Fill: result / limit, at most a full ring. With limit 0: empty at result 0, full
    above 0.
  - Colour: from green at result 0 to amber at the limit, linear in result / limit;
    red above the limit. With limit 0: green at 0, red above.
  - The numbers are always shown, so colour is not the only carrier (colour-blind
    users).
- There is no separate "in progress" mark: a partly filled ring says it. When there is
  no indicator (BHV-DAY-R2) the row shows none, but an empty place of the width of the
  ring stays, so the rows do not shift between dates.

*Other dates.* The selected date follows today until the user changes it
(BHV-DAY-R1). On a date after today the habits are shown with their value and without
action controls (BHV-ENTRY-R3); no explanatory text is added (UI principle 3).

## 4. Entries of one habit on one day (Decision 27)

Tapping a habit row on the Day screen opens a bottom sheet (UI-ENTRIES-SHEET) with the entries of that
habit on the selected date. What can be edited is defined by BHV-ENTRY-R6.
- Every entry has a delete icon (one tap, then "Undo", below); swiping is not used,
  since it is a hidden gesture.
- Tapping a Numeric or Duration entry opens the same form as a manual entry
  (UI-ENTRYFORM-NUMERIC, UI-ENTRYFORM-DURATION), filled with the entry's values. A
  Binary entry has only the delete icon.
- An "Add" button (UI-ENTRIES-ADD) opens the manual entry form; on a date after today
  it is absent, since no entry can be added there (BHV-ENTRY-R3).
- A running timer is a live item (UI-ENTRIES-TIMER, BHV-TIMER-R1) on every day it
  covers, with its session time, Stop and Discard; e.g. a timer started at 23:30 on
  10 Nov is listed on 10 Nov and on 11 Nov.

Deleting an entry or discarding the timer happens at once, without a dialog; a
snackbar with "Undo" stays for a few seconds (UI-MSG-UNDO-ENTRY, UI-MSG-UNDO-TIMER,
section 7; Decision 86). The snackbar is shown on the Day screen, above the sheet, and
stays when the sheet is closed, until it times out or is withdrawn:
- the undo of a deleted entry disappears when the habit gets a new entry on that day,
  or is archived or deleted (BHV-ENTRY-R6a);
- the undo of a discarded timer disappears when a new timer of the habit is started,
  or the habit is archived or deleted (BHV-TIMER-R1).
Any date is reached with the previous / next arrows or the calendar picker
(Decision 27), so old entries need no separate screen. The picker has no
markers for days with entries in v1; a month calendar with habits marked on their
days is in "Ideas for later" ("Calendar screen", REQ-X6) and would be a later
replacement of the picker, needing a query for the days that have entries.

*Forms of a manual entry (Decision 82).* Binary has no form: the tap adds or removes the
mark (BHV-ENTRY-R6, R6a).
- Duration (UI-ENTRYFORM-DURATION):
  - Fields: two rows, Start and End, each with a date and a time field (editable by
    hand; the date is needed because an entry can cross midnight). The time fields are
    hh:mm. The calculated length is shown, with seconds only when they are not 0
    (e.g. "2 s", "45 min 25 s").
  - Quick buttons: under Start -5 min, -15 min, -30 min, -1 h move the start earlier;
    under End +5 min, +15 min, +30 min, +1 h move the end later, but never after now: a
    button that would do so is disabled, so on today's date, with the end at now, they
    are disabled until the end is moved earlier by hand (BHV-ENTRY-R5). Taps add up.
    The buttons are fixed in v1 (configurable per habit is in "Ideas for later").
  - Defaults: start = end = now cut to the whole minute on today's date, 12:00 on a
    past date (BHV-ENTRY-R6b, BHV-ENTRY-24).
  - Seconds of an edited entry: a time the user does not change keeps its seconds,
    also when only its date changes; a time changed by typing, the picker or a quick
    button gets 0 seconds (BHV-ENTRY-R6b, BHV-ENTRY-23a-c).
  - Validation: while the length is 0, Save is disabled; the message "Length must be
    above 0" (BHV-ENTRY-R5) appears only after the user has touched a field, so the
    form does not open with an error (Decision 87).
  - A changed moment after now cannot be saved (UI principle 3). In the date picker of
    the entry forms the days after today are greyed out. The time picker cannot
    disable hours (verified in the Compose Material3 docs, 2026-11-23: `TimePicker`
    has no min / max), so its OK button (UI-ENTRYFORM-TIME-OK) is disabled while the
    chosen time is after now, with the message "Time cannot be after now" in the
    dialog. A moment after now can still arise when only the date is changed to
    today (e.g. 15:00 at 11:30): then Save is disabled and the same message stands at
    the time field (section 1, rule on two fields; BHV-ENTRY-7, 8). A moment that was
    already after now when the form opened and is not changed is accepted
    (BHV-ENTRY-21a).
- Numeric (UI-ENTRYFORM-NUMERIC): the value, and one moment (date and time field).
  Default: now cut to the whole minute on today's date, 12:00 on a past date. A changed
  moment after now cannot be saved, as in the Duration form (BHV-ENTRY-17, 21a, 21b).
  The value field accepts digits and one decimal separator (comma or dot), no minus
  sign. No increment buttons ("+" is on the Day screen row).

The widths of these forms and of the Day rows are checked on a prototype (section 8).

*Stop from the notification with a non-positive length (BHV-TIMER-R2).* The text of
the timer notification (UI-NOTIF-TIMER) changes to "Not stopped: the length is not
above 0. The timer is running." (the length can also be negative, after the clock was
moved back, BHV-TIMER-3). The text stays until the next Stop; the elapsed time is
still shown. When the app is open, a snackbar shows the same message
(UI-MSG-TIMER-NOT-STOPPED, section 7). That
a receiver can update the text is to be verified (section 8).

## 5. Habits screen (Decision 28)

- A list of habits (UI-HABITS-LIST; not archived), alphabetical as on the Day screen
  (BHV-HABIT-R4), a "+" button to create one (UI-HABITS-ADD), a collapsible section
  "Archived" (UI-HABITS-ARCHIVED; also alphabetical; a restore action on each row,
  UI-HABITS-RESTORE; their statistics stay available).
  Tapping a row opens the edit form. Archiving and deleting (with confirmation) are in
  the edit form. Export / import is an item of the menu (UI-HABITS-MENU; file format:
  Decision 30); the choice of the import mode is BHV-IMPORT-R2.
- Tapping an archived row opens the same form read-only (UI-HABITFORM-ARCHIVED-VIEW,
  BHV-HABIT-R3, BHV-HABIT-18; Decision 89): all fields and goals are shown, nothing can
  be changed, there is no Save; the top bar has "Restore" and the "⋮" menu has
  "Delete". The delete dialog of an archived habit has no "Archive" button (section 7).
  Its "Archived goals" section is expanded, since it is the only goal content. After
  "Restore" (and the restore dialog, if any) the view becomes the ordinary edit form
  of the restored habit.
- One form for creating and editing (UI-HABITFORM-SCREEN). The form has: name, direction (Build / Quit),
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
- Fields that cannot change are shown read-only with a short explanation
  (UI-HABITFORM-LOCKED): direction and measurement type after creation (BHV-HABIT-R2;
  BHV-HABIT-4, 9; text in section 7). Changing the unit of a numeric habit shows a warning on Save when something is
  relabeled: entries, a goal (active or archived) or a "+" step set by the user
  (BHV-HABIT-R2, BHV-HABIT-5, 19, 20, BHV-GOAL-11b; UI-DIALOG-UNIT, section 7). A new
  habit, or one with none of them, changes its unit without a warning. Editing a goal changes it
  on the same habit; no new habit is created. Saving the form (habit and goal) is one
  all-or-nothing operation (`docs/DESIGN.md` section 3).

*Layout of the form (Decision 83).* One scrolling screen in three parts, "Save" at the
bottom; archiving and deleting are in the "⋮" menu of the top bar (UI-HABITFORM-MENU),
not among the fields.
1. Basics: name, direction, type (read-only after creation, with a short
   explanation), for Numeric the unit and the "+" step.
2. Goal (UI-HABITFORM-GOAL): a switch "Goal". When on, in this order:
   - Target, with the unit beside it (e.g. "l"); for Duration three fields h / min / s
     (BHV-GOALVAL-R1); for Binary a number of days.
   - Period: day / week / month / quarter / year.
   - Start (UI-HABITFORM-START): a date picker, default today for a new goal; under it
     the calendar period that the date selects, e.g. "Week 9-15 Nov 2026"
     (BHV-GOAL-R1).
   - Goal length: repeats / N periods. With N periods a line "Last day: 5 Nov 2026"
     (UI-HABITFORM-LASTDAY, BHV-GOAL-R2, R4). ("Goal length", not "Duration", which is
     a measurement type.)
3. Archived goals (UI-HABITFORM-ARCHIVED-GOALS): a collapsible, read-only section, one line per goal, e.g. "target 3 /
   week, 26 Oct - 11 Nov". Not editable (BHV-GOAL-R7). Absent when the habit has none.

*Editing the active goal (Decision 89).* The form opens with the values of the active
goal: Start holds its first day, with its calendar period and "Last day". The Start
field always shows the start the new goal would get on Save, so nothing is decided
silently:
- When the user changes the target or the period, Start switches to the default of
  the new goal and the calendar period and "Last day" are recomputed at once: today
  (the current period of the new length) when the change archives the goal
  (BHV-GOAL-R4, BHV-GOAL-1, 17); the old first day when the goal is replaced and the
  period length stays (BHV-GOAL-R4a, BHV-GOAL-19); today when it is replaced with a new
  period length.
- Once the user picks a start date, it stays; later changes of the target or period do
  not reset it. Changing only the start is itself a change of the goal (BHV-GOAL-R4,
  BHV-GOAL-18).
- If every goal field is set back to the active goal's values, Start shows its first
  day again and Save changes no goal.
- N is pre-filled with the old N (BHV-GOAL-R4); "Last day" follows Start and N.
- A pending goal (not started yet, BHV-GOAL-R6) is edited in place: its Start is
  treated as a date the user picked and is not reset by a change of the target or
  period; the goal starts in the period of the new length that contains that date
  (BHV-GOAL-R1).

*Validations of the forms (section 1, "Rejected"; Decision 87).* The texts are given in
English; the Polish ones are written with the translations.

| Form, field | Rule | Message at the field |
|---|---|---|
| Habit: name | BHV-HABIT-R1, BHV-HABIT-1 | "Enter a name" |
| Habit: custom unit | BHV-HABIT-R1, BHV-HABIT-11 | "Enter a unit" |
| Habit: "+" step | BHV-HABIT-R1, BHV-HABIT-12 | "Step must be above 0" |
| Goal: target of a build goal (two fields on a new habit, where the direction can still change) | BHV-GOALVAL-R1, BHV-GOALVAL-4 | "Target must be above 0" |
| Goal: binary target against the period (two fields) | BHV-GOALVAL-R1, BHV-GOALVAL-1a | "At most 7 days in a week" (the number and the period follow the chosen period) |
| Goal: duration target against a daily period (two fields) | BHV-GOALVAL-R1, BHV-GOALVAL-3a | "At most 24 h in a day" |
| Goal: N periods | BHV-GOALVAL-R1, BHV-GOALVAL-6 | "At least 1" |
| Goal: "Last day" line (start, N and period against today; three fields) | BHV-GOAL-R2, R4a, BHV-GOAL-22 | "The goal would end before today" |
| Numeric entry: value | BHV-ENTRY-R5, BHV-ENTRY-9a | "Value must be above 0" |
| Numeric entry: value | BHV-ENTRY-9c | "The smallest value is 0.001" |
| Duration entry: length | BHV-ENTRY-R5, BHV-ENTRY-6 | "Length must be above 0" |
| Entry: time field (date and time against now; only when the date or time was changed) | BHV-ENTRY-R5, BHV-ENTRY-7, 8, 17, 21a, 21b, 21d | "Time cannot be after now" |

Input filters, so these cases need no message: numeric values, the target and the
step accept digits and one decimal separator (comma or dot), no minus sign (a
negative value, also a negative quit limit, cannot be typed; BHV-ENTRY-9b,
BHV-GOALVAL-R1); a binary target and N accept whole numbers only; the h / min / s
fields of a Duration target accept 0-59 for min and s (h has no filter; for a daily
goal it is checked by the 24 h row); a field that is empty counts as invalid (Save
disabled; the message of its row appears after a change). Only the date pickers of the
entry forms grey out the days after today (section 4); the Start picker of a goal, the
Day screen picker and the range picker of Statistics (UI-STATS-RANGE) allow any date
(BHV-GOAL-R1, BHV-ENTRY-2).

*Dialogs of the Habits screen* (texts and buttons in section 7): restoring an archived
habit (UI-DIALOG-RESTORE, BHV-HABIT-R3), only when the habit is restored on another
day than it was archived and a goal was archived with it; the goal is described so
the user knows what comes back. Change of the last day of a goal (UI-DIALOG-GOAL-END,
BHV-GOAL-R6).

*Language (BHV-LANG-R2, Decision 84).* The menu of the Habits screen has an item
"Language" next to export and import. It opens a dialog (UI-DIALOG-LANGUAGE, section
7) with two choices, Polski and
English; the choice calls the per-app locale API of Android, so it stays after the app is
closed and is the same as in the system settings (Settings > Apps > the app > Language,
available from Android 13 through `generateLocaleConfig`). Going back to "follow the
device language" is possible only in the system settings. There is no settings screen.

*Import (UI-IMPORT-SUMMARY, BHV-IMPORT-R2).* After a valid file is picked, a summary screen: export date,
number of habits and entries and, when some names equal those in the app, a line "N habits
have the same name as existing ones". Three equal buttons, none highlighted: "Replace
all" (then the warning and confirmation of BHV-IMPORT-R2, section 7), "Add to existing", "Cancel".

## 6. Statistics screen (Decision 29)

One screen. At the top a date range selector (UI-STATS-RANGE, BHV-STATS-R1: presets and a custom
range chosen in a calendar; the default is this week). Below, one card per habit
(UI-STATS-CARD), with archived habits in a separate section (UI-STATS-ARCHIVED); the cards are in alphabetical order, as on
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

*Card with several goals (Decision 83).* The card has a fixed header (the total or the
number, and the average per day for Duration and Numeric) and below it one block per
goal (UI-STATS-GOAL): e.g. "Goal 3 / week - achieved 2 of 3 - average 87%".
The blocks of archived goals are in a collapsible sub-block "Archived goals"
(UI-STATS-ARCHIVED-GOALS) on the same card (the same name as in the habit form),
collapsed by default. On the card of an archived habit the goal archived with the
habit is shown as an ordinary block, so the card is not empty by default; only the
goals before it are in the sub-block. When the sub-block would hold every block of the
card (e.g. a 30-day goal that has ended, BHV-PERIOD-4), it is expanded. The current,
unfinished period is marked "in progress, 4 days left" (UI-STATS-PROVISIONAL).

## 7. Messages and dialogs (Decision 88)

Every dialog and message of the app, with its place and text. The texts are given in
English; the Polish ones are written with the translations. Validation messages at
form fields are in section 5 (one of them, "Time cannot be after now", is also shown in
the time picker, UI-ENTRYFORM-TIME-OK). A snackbar stays for a few seconds; a dialog
stays until a button is tapped. "Cancel" in a dialog closes it and returns to the
screen it was opened from, with nothing changed.

| Situation | Rule | Place (ID) | Text and buttons |
|---|---|---|---|
| Export | REQ-S1, BHV-EXPORT-R1 | the system save dialog | Default file name `habit-tracker-<today's date>.json`, e.g. `habit-tracker-2026-11-11.json` |
| Export done | BHV-EXPORT-R1 | snackbar (UI-MSG-EXPORT-DONE) | "Exported to <file name>" |
| Export failed | BHV-EXPORT-R1, BHV-EXPORT-3 | snackbar (UI-MSG-EXPORT-FAILED) | "Export failed. Your data are unchanged." |
| Import file rejected | BHV-IMPORT-R1 | dialog (UI-DIALOG-IMPORT-REJECTED) | "This file cannot be imported: <reason>." Reasons: "it is not a backup of this app", "it is damaged", "it is from a newer version of the app". Button: OK |
| "Replace all" | BHV-IMPORT-R2 | dialog (UI-DIALOG-IMPORT-REPLACE) | "All habits, goals and entries in the app will be deleted and replaced by the file." Buttons: "Export and replace", "Replace", "Cancel" (back to the import summary) |
| "Export and replace" did not export | BHV-IMPORT-R2, BHV-IMPORT-5c, 5d | snackbar (UI-MSG-IMPORT-NOT-REPLACED) | "Not exported, so nothing was replaced." |
| Import done | BHV-IMPORT-R2 | snackbar (UI-MSG-IMPORT-DONE) | "Imported N habits and M entries" |
| Import failed while writing | BHV-IMPORT-R3, BHV-IMPORT-5e, 20 | snackbar (UI-MSG-IMPORT-FAILED) | "Import failed. Your data are unchanged." After "Export and replace" the export file stays: "Import failed. Your data are unchanged; the backup was saved." |
| Deleting a habit | BHV-HABIT-R3 | dialog (UI-DIALOG-DELETE-HABIT) | "Delete <name>? Its entries, goals and statistics are deleted for good. Archiving keeps them." Buttons: "Archive", "Delete", "Cancel". For an archived habit: "Delete <name>? Its entries, goals and statistics are deleted for good." Buttons: "Delete", "Cancel" |
| Changing the unit of a numeric habit with entries, a goal or a user-set step | BHV-HABIT-R2, BHV-HABIT-5, 20 | dialog on Save (UI-DIALOG-UNIT) | "Values are relabeled, not converted: 2 l becomes 2 ml." (the example uses the old and the new unit) Buttons: "Change", "Cancel" |
| Restoring a habit whose goal was archived with it, on another day | BHV-HABIT-R3 | dialog (UI-DIALOG-RESTORE) | "Restore the goal too (3 / week, repeats)? It starts in the current period." Buttons: "With goal", "Without goal", "Cancel" |
| Change of the last day of a goal | BHV-GOAL-R6 | dialog (UI-DIALOG-GOAL-END) | "The last day of the goal will be X; later periods will not count." Buttons: "Change", "Cancel" |
| Language | BHV-LANG-R2 | dialog (UI-DIALOG-LANGUAGE, section 5) | Choices "Polski", "English" |
| Stop with a non-positive length, in the app | BHV-TIMER-R2 | snackbar (UI-MSG-TIMER-NOT-STOPPED) | "Not stopped: the length is not above 0. The timer is running." |
| The same, from the notification | BHV-TIMER-R2 | text of the notification (UI-NOTIF-TIMER, section 4) | the same text |
| Archiving a habit whose timer has a non-positive length | BHV-TIMER-R3 | snackbar (UI-MSG-TIMER-ARCHIVED) | "Archived. The timer was stopped without an entry (its length was not above 0)." |
| Notification permission refused, or timers imported without a permission request | BHV-TIMER-R5, BHV-NOTIF-3, 4 | one-time dialog (UI-DIALOG-NOTIFICATIONS) | "Without notifications, Stop is only available in the app." Button: OK |
| Deleting an entry | BHV-ENTRY-R6a | snackbar (UI-MSG-UNDO-ENTRY, section 4) | "Entry deleted", action "Undo" |
| Discarding a timer | BHV-TIMER-R1 | snackbar (UI-MSG-UNDO-TIMER, section 4) | "Timer discarded", action "Undo" |
| Provisional period with 0 days left | BHV-STATS-R6 | text of the mark (UI-STATS-PROVISIONAL) | "in progress, ends today" instead of "0 days left" |
| No habits, Day screen | Decision 23 | text and button (UI-DAY-EMPTY-NONE) | "No habits yet." Button: "Create a habit" (opens the form on the Habits tab) |
| All habits archived, Day screen | Decision 23 | text and button (UI-DAY-EMPTY-ARCHIVED) | "All habits are archived." Button: "Go to Habits" |
| No day of the habit's life in the range, Statistics card | BHV-STATS-R2, BHV-STATS-11a, 11b | text on the card (UI-STATS-NO-DAYS); also for an archived habit (BHV-STATS-11b) | "No day of this habit in this range." |
| No goal period counts in the range, Statistics card of a habit with goals (not shown with UI-STATS-NO-DAYS) | BHV-STATS-R4, BHV-STATS-6 | text in place of the goal blocks (UI-STATS-NO-PERIODS) | "No full goal period in this range." |
| Read-only direction and type in the habit form | BHV-HABIT-R2, BHV-HABIT-4, 9 | text at the field (UI-HABITFORM-LOCKED) | "Cannot be changed after the habit is created." |
| No habits, Habits screen | | text and the "+" button (UI-HABITS-EMPTY) | "No habits yet." |
| No habits, Statistics screen | | text and button (UI-STATS-EMPTY) | "No habits yet." Button: "Create a habit" (opens the form on the Habits tab) |

"Export and replace" opens the system save dialog; the replacement runs only after the
export has been written. If the user cancels the save dialog or the export fails,
nothing is replaced (BHV-IMPORT-R2) and only UI-MSG-IMPORT-NOT-REPLACED is shown (not
UI-MSG-EXPORT-FAILED as well); the user stays on the import summary. When both steps
succeed, only UI-MSG-IMPORT-DONE is shown and the user is on the Habits screen; the
same after a successful "Replace" or "Add to existing". When the import fails while
writing (UI-MSG-IMPORT-FAILED), the user stays on the import summary and can try
again or cancel. "Archive"
in the delete dialog does the same as archiving from the form menu (BHV-HABIT-R3).

System dialogs that the app opens but does not design: the notification permission
request (BHV-TIMER-R5), the file picker of the import and the save dialog of the export.

## 8. Verify before the plan

No UX point is open. To check in the official docs or on a prototype before the plan:
1. The texts of the timer notification follow the app language set with the per-app
   locale API (BHV-LANG-R2; the official page does not say).
2. A receiver of the notification's Stop can update the text of the notification in a
   simple way (section 4).
3. Prototype, widths on a phone: the eight quick buttons of the Duration form in two
   rows of four (section 4); a Day row of a Duration habit with a running timer (name,
   Stop, session time, manual entry, value, ring); a Quit Duration ring with
   "1h20m/3h", the widest Duration text with seconds ("23h59m58s/23h59m59s", a daily
   limit is at most 24 h) and long numbers in a Quit ring such as "1250/2000"
   (section 3).

Verified on 2026-11-23: the Compose Material3 time picker cannot disable hours, the
date picker can disable days (section 4).
