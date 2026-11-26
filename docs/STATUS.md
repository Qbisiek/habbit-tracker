# Project status

Overwritten at the end of every session and when a session is interrupted (see
CLAUDE.md). It holds only the current state. History is in git. Read it at the start
of a session.

Last updated: 2026-11-26.

## Where we are

- Phase: implementation, milestone M1 (branch `milestone/m1-skeleton`).
- Gates: REQUIREMENTS, UX, DESIGN approved 2026-11-24; PLAN approved 2026-11-25;
  `android/CODING-GUIDELINES.md` approved 2026-11-26.
- Done: T1.1 (version catalog `android/gradle/libs.versions.toml`, DESIGN section 2
  updated). Next ready task: T1.2.
- The milestone branch is not pushed yet: `git push` from the AI's shell failed with
  an SSL certificate error (sandbox); the user pushes or decides how to fix it.
- Uncommitted (tooling, committed by the user): `.claude/skills/implement-task/SKILL.md`
  (regenerate the dashboard after every status change) and this file.

## Implementation tooling (decided by the user, 2026-11-26)

- Coding rules, branch model (section 9) and Definition of Done (section 10):
  `android/CODING-GUIDELINES.md`. Commit format `<type>: <task ID> - <summary>`.
- `main` is protected by the PreToolUse hook `.claude/scripts/guard_main.py`; the merge
  of a milestone into `main` and its tag are done by the user.
- The AI may push `task/*` and `milestone/*` to `origin`, never force push.
- `/implement-task <ID>` skill: interactive, gate 1 (plan of the task) and gate 2
  (report) with the user; gate 1 is mandatory for T1.1-T1.3, then the user decides.
- Later: an implementation loop over a whole milestone (agents `coder` and
  `code-reviewer`), possibly several loops in parallel for independent tasks in git
  worktrees. Not designed yet.
- Development environment: Android Studio Rabbit 2 (2026.2.2), installed 2026-11-26
  with winget. The user runs its setup wizard (SDK, emulator).

## Open for T1.2

- Check in the build: whether `room-ktx` is still a separate artifact in Room 2.8;
  that `kotlinx-coroutines-test` 1.11.0 matches core; AGP 9.5.1 as a stable patch.
- Android SDK location for the build (`local.properties`) after the wizard.

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

1. The user commits the uncommitted tooling files and pushes the milestone branch.
2. The user finishes the Android Studio setup wizard (SDK, emulator).
3. `/implement-task T1.2` (Gradle project).
