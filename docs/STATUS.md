# Project status

Overwritten at the end of every session and when a session is interrupted (see
CLAUDE.md). It holds only the current state. History is in git. Read it at the start
of a session.

Last updated: 2026-11-24.

## Where we are

- Phase: plan (`docs/PLAN.md`), started by the user with `/plan-interview` on
  2026-11-24. Session interrupted at the user's request after the tasks of M3.
- Gates: `docs/REQUIREMENTS.md`, `docs/UX.md`, `docs/DESIGN.md` (with
  `docs/DESIGN-DECISIONS.md` and the diagram) approved by the user on 2026-11-24.
  `docs/PLAN.md` is a DRAFT, not approved.
- Uncommitted: `docs/PLAN.md` (new), this file, two fixes of the dashboard
  (`plan_dashboard.py` and its template: the template opened directly shows a hint
  instead of broken text; the whole multi-line STATUS banner is shown).
- Plan tooling committed by the user: the `plan-interview` skill,
  `.claude/scripts/req_index.py`, `.claude/scripts/plan_dashboard.py` (+ template),
  `docs/dashboard/` in `.gitignore`.

## Plan: agreed so far (user, 2026-11-24)

- Level 1 (milestones) confirmed: M1 skeleton and spikes; M2 numeric build end to end
  without goals; M3 export and import; M4 breadth for the first habit (management,
  Statistics, language switch); M5 goals and the quit direction; M6 goal changes and
  history; M7 binary and duration with manual entries; M8 timer and notification; M9
  success criteria and finishing.
- Choices behind it: first habit kind numeric build (not binary, not duration with a
  timer); the full data model and file format already in M3; goals (M5-M6) before more
  habit kinds and the timer (M7-M8).
- Level 2 (tasks) agreed for M1, M2, M3 (tables in `docs/PLAN.md`).
  - Application ID / package: `kubisp.habittracker`.
  - Spike prototypes live in a `spike` package of `app` and are removed in T1.8.
  - Conventions (section in `docs/PLAN.md`): PL/EN texts in every UI task; the
    navigation bar has only working tabs (Statistics from M4); the database is
    recreated on a schema change until the first everyday version (at the latest M9),
    then real migrations.
  - Moved out of M3: the goal validity part of BHV-IMPORT-R4 goes to M5; the
    notification sync and one-time message after an import (BHV-NOTIF-2, 4) and the
    check that "Export and replace" fits the receiver time go to M8.
- Not confirmed yet: effort, dependencies and Covers in the tables are proposals
  until level 3; Covers lists only REQ-* IDs so far (BHV-* IDs come at level 3).

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
2. Continue `/plan-interview`: level 2 for M4, then M5-M9 (remember the items moved
   to M5 and M8), then level 3 (effort, dependencies, Covers with BHV-* IDs), the
   coverage check (`python .claude/scripts/req_index.py --coverage` and the
   dashboard), `check-refs.sh`, approval.

## To resume

1. Ask whether `docs/PLAN.md` was committed.
2. The user restarts `/plan-interview`; the skill finds the existing `docs/PLAN.md`
   and must ask whether to continue it (continue: from the tasks of M4).
