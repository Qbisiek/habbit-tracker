# Project status

Overwritten at the end of every session and when a session is interrupted (see
CLAUDE.md). It holds only the current state. History is in git. Read it at the start
of a session.

Last updated: 2026-11-26.

## Where we are

- Phase: implementation, milestone M1 (branch `milestone/m1-skeleton`, pushed).
- Gates: REQUIREMENTS, UX, DESIGN approved 2026-11-24; PLAN approved 2026-11-25;
  `android/CODING-GUIDELINES.md` approved 2026-11-26.
- Done: T1.1 (version catalog), T1.2 (Gradle project: `domain` and `app`, wrapper
  9.9.2, ktlint, compileSdk 37.2), T1.3 (Auto Backup off, INTERNET removed,
  question 21 closed), T1.4 (notification language spike, accepted by the user
  2026-11-26), T1.5 (spike: list of shown notifications, accepted by the user
  2026-11-26, squash `376999d`), T1.6 (spike: widths, accepted by the user 2026-11-26,
  squash `aa5f7c4`), T1.7 (spike: Undo snackbar with the modal sheet, accepted by the
  user 2026-11-26, squash `74815f1`, pushed). Ready: T1.8 (last task of M1), T2.1.
- Spike prototypes: package `spike` in `app`, launcher icon "Spikes"
  (`SpikeActivity`); T1.5-T1.7 add their sections there; T1.8 removes the package,
  its strings `spike_*`, its drawables `spike_ic_*`, its manifest entries and
  `POST_NOTIFICATIONS`.
- Uncommitted: this file and `.claude/skills/implement-task/SKILL.md` (new rule C4: step back
  and question the approach when a UI layout does not work; user request 2026-11-26).

## Implementation tooling (decided by the user, 2026-11-26)

- Coding rules, branch model (section 9) and Definition of Done (section 10):
  `android/CODING-GUIDELINES.md`. Commit format `<type>: <task ID> - <summary>`.
- `main` is protected by the PreToolUse hook `.claude/scripts/guard_main.py`; the merge
  of a milestone into `main` and its tag are done by the user.
- The AI may push `task/*` and `milestone/*` to `origin`, never force push.
- `/implement-task <ID>` with gate 1 (plan of the task) and gate 2 (report) for every
  task of M1 (user decision 2026-11-26). From M2 on: the implementation loop.
- Statuses in `docs/PLAN.md` change only on the milestone branch; the dashboard is
  regenerated after every change (it has a roadmap at the top, user request).
- Later: an implementation loop over a whole milestone (agents `coder` and
  `code-reviewer`), possibly several loops in parallel for independent tasks in git
  worktrees. Not designed yet.

## Environment (2026-11-26)

- Android Studio Rabbit 2 (2026.2.2); the user opens `android/` (not the repo root).
- SDK in `%LOCALAPPDATA%\Android\Sdk` (platforms 37.0 and 37.2, build-tools 36.0.0,
  emulator API 37). JDK: the JetBrains Runtime 25 of Android Studio; the daemon JVM is
  fixed by `android/gradle/gradle-daemon-jvm.properties` (in git; Gradle recommends
  committing it; keeping it is the AI's recommendation, not confirmed by the user).
- Avast HTTPS scanning was switched off by the user: with it on, Java and git (OpenSSL)
  reject the certificates and Gradle cannot download anything.
- The AI adds files to commits by name, not with `git add -A` (an unreviewed generated
  file slipped into the T1.2 commit).

## Open

- T1.4 results, to record in T1.8 (emulator API 37, framework `LocaleManager`): the
  notification texts follow the per-app language, also in a fresh process started by
  the Stop receiver and after a device language change (BHV-LANG-R1); the fallback of
  `docs/DESIGN.md` 5.5 is not needed. Parts drawn by the system (app name, time,
  shade texts) use the system language. Conflict with BHV-LANG-R2: an already posted
  notification and the channel name keep the old language until posted again. AI
  recommendation, not decided: re-post the notifications of running timers and
  recreate the channel when the app returns to the foreground / the language changes
  (the notification sync, `docs/DESIGN.md` 5.6); alternative: accept the delay and
  write it into the requirement. Mark `docs/DESIGN.md` 5.5, section 10 and
  `docs/UX.md` 8 item 1 as verified.

- T1.5 results, to record in T1.8 (emulator API 37, `NotificationManagerCompat`
  `getActiveNotifications()` with `notify(tag, id, ...)`, tag `habit:<id>`, a
  PendingIntent request code per habit): the list returns tag and id; it survives a
  killed process (the new process sees the notifications); a notification dismissed by
  the user disappears from it; Stop of one habit removes only its notification; a
  notification is in the list right after `notify()` (one trial, not a guarantee);
  revoking the permission and force-stop remove all the app's notifications (empty
  list). Not expected: with 2+ notifications without a group the system adds an
  autogroup summary (`AUTOGROUP_SUMMARY`, id 1, tag
  `0|kubisp.habittracker|g:Aggregate_SilentSection`, posted asynchronously) that the
  list also returns; swiping the collapsed group dismisses all timer notifications at
  once. AI recommendation, not decided: in `docs/DESIGN.md` 5.6 the sync considers only
  notifications with its own tag prefix and re-posts missing ones when the app returns
  to the foreground; keep the system autogrouping (an own group with an own summary
  was not checked). Testing note: `am kill` did not kill the process on the emulator;
  `adb shell run-as kubisp.habittracker kill -9 <pid>` did.

- User decision 2026-11-26 (from T1.6), to write in T1.8 with the user's approval of
  each change: a duration limit above 1 h cannot be set with seconds (whole minutes
  only), so the 2-unit display (see T1.6 below) never hides the state of a quit goal.
  Touches `BHV-GOALVAL-R1` (today: "a duration target has a precision of one second")
  and `docs/UX.md` 3 (rounding of the Quit ring text). It applies to every duration
  target, build and quit (user, 2026-11-26).

- T1.6 results, user decisions 2026-11-26, to write in T1.8 (`docs/UX.md` 3 and 4,
  Decision 82): the Day row has two
  lines for every habit type (line 1: name and indicator; line 2: action controls and
  the value of the day, wrapping when it does not fit); the timer controls are icons
  (stop square, start triangle) with a content description, no text; quick buttons of
  the Duration form are labelled `-5m` ... `+1h`, with a small padding (8 dp), four in a
  row, wrapping to 2x2 under their field at large font scales; duration texts of the
  indicator and the value show at most 2 units. Measured: a text inside the ring does
  not fit at any size (40-56 dp, one or two lines); a small ring (32 dp) with the text
  beside it in line 1 fits at 360/411 dp and font scale 1.0/1.3/2.0. Decided (user):
  the indicator is a rectangular progress bar with the text inside ("result/limit" or
  "75%"), not a ring; light fill tones (green, amber, red above the limit) with dark
  text; fixed width so the rows stay aligned; height at least 24 dp, growing with the
  font. Measured: the bar sized to "23h59m/23h59m" fits all samples (also
  "1234.567/2000", "167h59m/168h") at 360/411 dp and font scale 1.0/1.3/2.0; the name
  keeps 209 dp (360 dp, 1.0) to 108 dp (360 dp, 2.0). Decided (user, 2026-11-26) for T1.8:
  (1) texts in the bar are kept short by the 2-unit rule and rounding, also for
  numeric values (no full precision in the bar); if a text still does not fit, the bar
  moves to its own line under the name and the controls go to a third line; (2) build
  at 100%: the text "100%" is enough, no check; (3) Decision 82 and UX 3 replace the
  ring by the bar, the fallback for long numbers goes away; (4) hours are the largest
  unit of a duration amount (no days or weeks; a goal counted in days is a binary
  habit), as BHV-GOALVAL-R1 says; to be changed later only if a need appears. Open for
  T1.8: the concrete rounding rule for numeric texts in the bar, keeping the state
  honest like the duration rule (within the limit down, above it up). AI proposal, not
  decided: decimals by the size of the number (100 and more: none; 10-99.9: one; below
  10: up to two).

- T1.7 results, user decision 2026-11-26 (variant B), to write in T1.8 (`docs/DESIGN.md`
  3 App shell and 10 question list, `docs/UX.md` 4 and 8 item 4): verified on the
  emulator (API 37) that a `ModalBottomSheet` has its own window above the Activity
  window, so the screen snackbar is hidden under the open sheet (not visible, Undo not
  reachable); it appears when the sheet is closed. Variant B works: the entries sheet
  has a second `SnackbarHost`; the App shell stays the only owner of the snackbar state
  and shows it on the sheet host while the sheet is open, otherwise on the screen host;
  switching hosts or the end of the offer cancels the showing, so it is never shown
  twice and never outlives the offer (DESIGN 5.7). Undo works in the sheet; reopening
  the sheet moves the snackbar back into it; a new deletion replaces the offer. The
  same applies to the "timer not stopped" snackbar. The non-modal sheet fallback is not
  needed. Not tested: the dismiss (X) action; rotation (skipped by the user).

- Check in the first task with Room (M2): whether `room-ktx` is still a separate
  artifact in Room 2.8 (it is in the version catalog, not used yet).
- Smoke tests `BuildSmokeTest` in `domain` and `app`: removed by the first task that
  adds a real test in that module.

## Deferred (consciously, by the user)

From the review of the second design loop (report deleted):
- M4 placeholders in Decisions 73, 76, 79, 81.
- M5 diagram arrow App -> GUI / Platform services "creates".
- M7 UX 8 item 4 fallback vs "No screen subscribes to these streams".
- M8 Map by topic in DESIGN has no Language entry.
- S1 split the Domain tests sentence of DESIGN 8 by topic with BHV IDs.
- S2 shorten "Resolved" 22-26 in DESIGN 10.
- Decision 92 bundles several decisions in one row (cosmetic).
Tooling, optional, later: a convergence rule for `/design-loop`; links for IDs
(`BHV-*`, `Decision N`, `UI-*`) as Markdown anchors with a script; an optional
`docs-reviewer` review of `docs/PLAN.md`.

## Next steps (the user gates every step)

1. `/implement-task T1.8` (last task of M1): record the results and decisions of
   T1.4-T1.7 in the documents (each change approved by the user), remove the spike
   prototypes; then the APK and the manual test checklist of M1 for the user.
2. Before M2: design of the implementation loop (agents `coder`, `code-reviewer`,
   loop skill).
