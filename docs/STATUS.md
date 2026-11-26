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
  question 21 closed). Ready: T1.4-T1.7 (spikes), T2.1.
- Uncommitted: this file.

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

1. The rest of M1 with `/implement-task`: spikes T1.4-T1.7, then T1.8.
2. Before M2: design of the implementation loop (agents `coder`, `code-reviewer`,
   loop skill).
