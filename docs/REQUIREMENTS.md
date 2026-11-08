# Requirements: Habit Tracker (working title)

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
- Habits with measurement types: binary, numeric, duration.
- Both directions for every measurement type: build and quit.
- Goals per habit over a fixed period: day, week, month.
  Fixed periods repeat automatically.
  For binary habits the goal counts days with an occurrence
  (e.g. "at most 3 days per week").
- Duration habits: manual time entry and a start/stop timer.
  A running timer survives closing the app (it stores its start time).
- Adding and editing entries for past days. Reached by switching to the previous / next
  day and by picking a date from a calendar.
- Habit management (create, edit, archive, delete) and a "today" screen for quick logging.
- Built-in units for numeric habits.
- Statistics over a user-chosen date range (e.g. last 6 months, last year):
  - total time spent on a habit and the average per day
    (e.g. "6 months: X h Y min, on average Z per day"),
  - number of occurrences (e.g. how many times went to bed early in a year),
  - how many times the period goal was achieved and the average goal completion percentage.

### Should
- Export / import of data to / from a file.
- Custom unit for numeric habits, defined as a text label (no conversion between units).
- Ongoing notification while a timer is running (with a Stop button).

### Won't (v1)
- Reminders / notifications.
- Goal interval: custom length (e.g. every 10 days).
- Goal interval: arbitrary set of dates.
- User choice whether intervals repeat or not.
- Scale / rating habit type (e.g. mood 1-5).
- Checklist habit type (habit made of sub-tasks).
- iOS.
- Publication in Google Play (v1 is for personal use and friends testing).
- Dark theme.
- Home screen widget.
- Categories, grouping, tagging.

## 3. Constraints

- Fully offline. No account, no server.
- Data stored locally on the device only. Backup and restore via file export / import.
- Platform: Android phones only. Minimum Android version: see open questions
  (no support for very old versions).
- UI in two languages from the start (Polish and English). All texts in resources,
  with the assumption that more languages will be added later.
- No ads, no analytics, no crash reporting. No data leaves the device except a
  user-initiated export.

## 4. Success criteria

v1 is successful if, for one week, the author can record and monitor three habits:
- a duration habit (build), e.g. working on a book, using the timer and one entry for a
  past day,
- a numeric habit (build) with a daily goal, e.g. litres of fluids drunk,
- a binary habit (quit) with a weekly goal, e.g. going to bed late, at most 3 days
  per week.

Checks:
- All entries are saved without errors or data loss.
- Statistics for the week match a manual calculation.
- Daily and weekly goals are correctly evaluated as achieved or not.

Acceptance tests of individual features belong to the plan and implementation phase,
not to this document.

## 5. Open questions

- App name (working title: "Habit Tracker").
- Minimum Android version.
- List of built-in units for numeric habits.

## Ideas for later

- iOS version.
- Publication in Google Play.
- Monetization (e.g. limited number of habits in the free version, paid Pro version).
- Reminders / notifications.
- Goal interval: custom length (e.g. every 10 days).
- Goal interval: arbitrary set of dates.
- User choice whether goal intervals repeat or not.
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
- Import that merges the data from a file with the data already in the app
  (v1: import replaces all data, after a warning and confirmation).
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
