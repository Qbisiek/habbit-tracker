# Project status

Overwritten at the end of every session and when a session is interrupted (see
CLAUDE.md). It holds only the current state. History is in git. Read it at the start
of a session.

Last updated: 2026-11-27.

## Where we are

- Phase: implementation. Milestone M1 is finished: all tasks T1.1-T1.8 done, the
  manual test accepted (the user's acceptances of the spikes T1.4-T1.7 at gate 2 count
  as the manual test, user decision 2026-11-26), merged by the user into `main` with
  `--no-ff` (`573fdbc`) and tagged `m1`. M2 has not started; its milestone branch does
  not exist yet.
- Gates: REQUIREMENTS, UX, DESIGN approved 2026-11-24; PLAN approved 2026-11-25;
  `android/CODING-GUIDELINES.md` approved 2026-11-26.
- Spike results are recorded in the documents (T1.8): Decisions 99-103, `docs/UX.md`
  sections 3, 4, 8, `docs/DESIGN.md` sections 3, 5.5, 5.6, 6, 10, `BHV-GOALVAL-R1`,
  new scenarios BHV-GOALVAL-3c/3d, BHV-DAY-12c/13a-c/14, BHV-LANG-8/9, plan rows T2.12,
  T5.9, T7.4, T7.7, T8.3, T8.4. The `docs-reviewer` findings on T1.8 are all closed
  (m8 left as history by the user); no review report is kept.
- Uncommitted: this file only (the AI does not commit on `main`).

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
  regenerated after every change (roadmap at the top; the "Waits on · blocks" column
  counts open tasks of the whole chain, user request 2026-11-26).
- From M2 on: the implementation loop `/implement-milestone <M>` (written 2026-11-27,
  not committed and not tried yet; the user reviews the diff and commits). Files:
  `.claude/skills/implement-milestone/SKILL.md`, `.claude/agents/coder.md`,
  `.claude/agents/code-reviewer.md`, `.claude/scripts/plan_ready.py`; new status
  `blocked` in `docs/PLAN.md` (dashboard, `plan-interview`, `/implement-task`). User
  decisions 2026-11-27: the loop runs through the whole milestone without gates; at
  most 3 review rounds, Blocking findings after round 3 make the task `blocked` for
  the user, Non-blocking findings are handled by the coder's judgment; the
  orchestrator (main session) rebases the task branch onto the milestone branch and
  squash-merges it (task branches stay local); at most 2 tasks in parallel, each in
  its own worktree, only ready tasks; at most 1 running task uses the device.
- Emulator without Android Studio (tested 2026-11-27): `emulator -avd
  Medium_Phone_API_37.0 -no-window -no-snapshot-save -no-audio` in the background
  boots in about 30 s; `wm dismiss-keyguard`, `screencap` and `uiautomator dump`
  work headless; `adb emu kill` stops it. A new worktree needs
  `android/local.properties` copied (not in git).

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
  `settings put system font_scale` simulate other phones (reset them afterwards). There
  is no Node on the machine (the dashboard JS cannot be syntax-checked).

## Open

- Tooling: `.claude/scripts/guard_main.py` blocks every Bash command that contains the
  word `main` (also read-only ones such as `git tag --list` while on `main`, or a Python
  script that edits a file and mentions `main`); `git log --decorate` works. Possibly
  narrow the hook to writing git commands (user to decide).
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

1. The user reviews and commits the loop tooling on `main` (see "Implementation
   tooling").
2. M2: `/implement-milestone M2` creates `milestone/m2-...` from `main` (name by
   `android/CODING-GUIDELINES.md` section 9) and starts with T2.1, the only ready task.
   First run of the loop: watch it and adjust the skill after the milestone.
