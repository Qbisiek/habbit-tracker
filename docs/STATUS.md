# Project status

Overwritten at the end of every session and when a session is interrupted (see
CLAUDE.md). It holds only the current state. History is in git. Read it at the start
of a session.

Last updated: 2026-11-26.

## Where we are

- Phase: implementation. Milestone M1 (branch `milestone/m1-skeleton`, pushed): all
  tasks T1.1-T1.8 are done (T1.8 squash `505dc28`) and the manual test is accepted.
  Waiting for the user: the merge of `milestone/m1-skeleton` into `main` with
  `--no-ff` and the tag `m1` (done by the user).
- Gates: REQUIREMENTS, UX, DESIGN approved 2026-11-24; PLAN approved 2026-11-25;
  `android/CODING-GUIDELINES.md` approved 2026-11-26.
- M1 APK (debug, no prototypes, built 2026-11-26):
  `android/app/build/outputs/apk/debug/app-debug.apk`.
- Manual test of M1 (user decision 2026-11-26): the user's acceptances of T1.4-T1.7 at
  gate 2 count as the manual test (the prototypes were removed in T1.8). On the final
  APK the AI checked on the emulator that the app starts, shows "Habit Tracker", has no
  "Spikes" icon and declares neither `POST_NOTIFICATIONS` nor `INTERNET`.
- Spike results are recorded in the documents (T1.8): Decisions 99-103, `docs/UX.md`
  sections 3, 4, 8, `docs/DESIGN.md` sections 3, 5.5, 5.6, 6, 10, `BHV-GOALVAL-R1`,
  new scenarios BHV-GOALVAL-3c/3d, BHV-DAY-12c/13a-c/14, BHV-LANG-8/9, plan rows T2.12,
  T5.9, T7.4, T7.7, T8.3, T8.4. A `docs-reviewer` review of the T1.8 changes found one
  blocker and six major findings; all were fixed with the user's approval except m8
  (history of the done task T1.6 in PLAN, left as is). No review report is kept.
- Uncommitted: this file and `.claude/scripts/plan_dashboard.html` (the "Waits on ·
  blocks" column counts open tasks of the whole chain instead of effort points; the
  task details list the whole chain, indirect tasks marked "via chain"; user request
  2026-11-26; not tested in a browser, there is no Node on the machine).

## Implementation tooling (decided by the user, 2026-11-26)

- Coding rules, branch model (section 9) and Definition of Done (section 10):
  `android/CODING-GUIDELINES.md`. Commit format `<type>: <task ID> - <summary>`.
- `main` is protected by the PreToolUse hook `.claude/scripts/guard_main.py`; the merge
  of a milestone into `main` and its tag are done by the user.
- The AI may push `task/*` and `milestone/*` to `origin`, never force push.
- `/implement-task <ID>` with gate 1 (plan of the task) and gate 2 (report). Rule C4
  (user request 2026-11-26): when a UI layout does not work, step back and question the
  approach, prototype two or three alternatives and propose them.
- Statuses in `docs/PLAN.md` change only on the milestone branch; the dashboard is
  regenerated after every change (it has a roadmap at the top, user request).
- From M2 on: an implementation loop over a whole milestone (agents `coder` and
  `code-reviewer`), possibly several loops in parallel for independent tasks in git
  worktrees. Not designed yet.

## Environment (2026-11-26)

- Android Studio Rabbit 2 (2026.2.2); the user opens `android/` (not the repo root).
- SDK in `%LOCALAPPDATA%\Android\Sdk` (platforms 37.0 and 37.2, build-tools 36.0.0,
  emulator API 37, AVD "Medium_Phone_API_37.0"). JDK: the JetBrains Runtime 25 of
  Android Studio; the daemon JVM is fixed by `android/gradle/gradle-daemon-jvm.properties`
  (in git; keeping it is the AI's recommendation, not confirmed by the user).
- Avast HTTPS scanning was switched off by the user: with it on, Java and git (OpenSSL)
  reject the certificates and Gradle cannot download anything.
- The AI adds files to commits by name, not with `git add -A`.
- Emulator checks by the AI: adb with `uiautomator dump` and screenshots work; in Git
  Bash set `MSYS_NO_PATHCONV=1` for device paths; `am kill` did not kill the process,
  `adb shell run-as kubisp.habittracker kill -9 <pid>` did; `wm density` and
  `settings put system font_scale` simulate other phones (reset them afterwards).

## Open

- Check in the first task with Room (M2): whether `room-ktx` is still a separate
  artifact in Room 2.8 (it is in the version catalog, not used yet).
- Smoke tests `BuildSmokeTest` in `domain` and `app`: removed by the first task that
  adds a real test in that module.

## Deferred (consciously, by the user)

From the review of the second design loop (report deleted):
- M4 placeholders in Decisions 73, 76, 79, 81.
- M5 diagram arrow App -> GUI / Platform services "creates".
- M8 Map by topic in DESIGN has no Language entry.
- S1 split the Domain tests sentence of DESIGN 8 by topic with BHV IDs.
- S2 shorten "Resolved" 22-26 in DESIGN 10.
- Decision 92 bundles several decisions in one row (cosmetic).
(M7, UX 8 item 4, was resolved by T1.8.)
Tooling, optional, later: a convergence rule for `/design-loop`; links for IDs
(`BHV-*`, `Decision N`, `UI-*`) as Markdown anchors with a script; an optional
`docs-reviewer` review of `docs/PLAN.md`.

## Next steps (the user gates every step)

1. The user: merge `milestone/m1-skeleton` into `main` with `--no-ff` and tag `m1`.
2. Before M2: design of the implementation loop (agents `coder`, `code-reviewer`, loop
   skill), then M2 starting with T2.1.
