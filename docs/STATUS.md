# Project status

Overwritten at the end of every session and when a session is interrupted (see
CLAUDE.md). It holds only the current state. History is in git. Read it at the start
of a session.

Last updated: 2026-11-25.

## Where we are

- Phase: plan finished. `docs/PLAN.md` approved by the user on 2026-11-25.
- Gates: `docs/REQUIREMENTS.md`, `docs/UX.md`, `docs/DESIGN.md` (with
  `docs/DESIGN-DECISIONS.md` and the diagram) approved on 2026-11-24; `docs/PLAN.md`
  approved on 2026-11-25.
- Implementation not started; the user decides when it starts.
- Uncommitted: `docs/PLAN.md` (M4-M9 tasks, level 3, approval banner) and this file.

## Plan: what was decided (user, 2026-11-24 and 2026-11-25)

- 9 milestones, 81 tasks, 185 effort points; every one of the 295 IDs of
  `req_index.py --coverage` is covered by a task; nothing deferred.
- Decisions of 2026-11-25 (recorded as task notes in `docs/PLAN.md`):
  - M4: archive / restore / delete only for a habit without a goal and without a
    timer; the goal part in M6 (T6.4), the timer part in M8 (T8.2). Statistics
    figures only for Numeric in M4; Binary and Duration in M7.
  - M5: an existing goal is read-only in the form (temporary note) until M6; goal
    validation of Binary and Duration in M7.
  - The evaluation of a Binary period is in T5.3 (M5), not M7, because the
    goal-change scenarios of M6 (Fixture S) use a binary habit.
  - M9: a signed release APK with a fixed key kept outside the repo (T9.8); the Auto
    Backup settings are in T1.3, not in M9.
- Earlier decisions (2026-11-24): see the Conventions section and the task notes of
  M1-M3 in `docs/PLAN.md`.

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
(`BHV-*`, `Decision N`, `UI-*`) as Markdown anchors with a script.

## Next steps (the user gates every step)

1. The user reviews and commits `docs/PLAN.md` and this file.
2. Optional: a `docs-reviewer` review of `docs/PLAN.md`, if the user wants it.
3. The user decides when the implementation starts; the first task is T1.1 (check
   the current versions of the toolchain and libraries).
