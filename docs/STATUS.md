# Project status

Overwritten at the end of every session and when a session is interrupted (see
CLAUDE.md). It holds only the current state. History is in git. Read it at the start
of a session.

Last updated: 2026-11-24.

## Where we are

- Phase: design done. Gates:
  - `docs/REQUIREMENTS.md`: approved by the user 2026-11-24.
  - `docs/UX.md`: approved by the user 2026-11-24.
  - `docs/DESIGN.md`, `docs/DESIGN-DECISIONS.md`, `docs/diagrams/components.drawio`:
    approved by the user 2026-11-24 (after a review of Decisions 90-98; the user
    checked the diagram in draw.io; S3 done: all "awaits the user" marks replaced).
- Review reports of the design loops deleted from `docs/reviews/` (2026-11-24);
  their deferred findings are listed below.
- Next phase: plan (`docs/PLAN.md`). Not started; it needs the user's explicit start.
- Uncommitted: `docs/DESIGN.md`, `docs/DESIGN-DECISIONS.md` (S3 and the user's reasons
  added to a few rows), this file.

## Agreed at the approval (2026-11-24)

- Decisions 90-98 approved without changes. The user's questions answered: 91
  (*Replace all* keeps the UUIDs of the file), 93 (the undo offer is plain state in a
  pure Kotlin Domain object, a `StateFlow`; the wait goes through the Clock port).
- Decisions 73, 76, 79, 81: the user added reasons to some rows and adds no more;
  the remaining placeholders stay.

## Deferred (consciously, by the user)

From the review of the second design loop (report deleted; the items are kept here):
- M4 placeholders in Decisions 73, 76, 79, 81 (see above).
- M5 diagram arrow App -> GUI / Platform services "creates" (Android creates the
  Activity and the receiver).
- M7 UX 8 item 4 fallback vs "No screen subscribes to these streams".
- M8 Map by topic in DESIGN has no Language entry.
- S1 split the Domain tests sentence of DESIGN 8 by topic with BHV IDs.
- S2 shorten "Resolved" 22-26 in DESIGN 10.
- Assistant's note: Decision 92 bundles several decisions in one row; splitting it
  would help readability (cosmetic).

## Carried to the plan (first tasks, spikes)

Listed in `docs/DESIGN.md` section 10: language of notification texts (UX 8 item 1),
the API listing shown notifications (question 7), widths on a phone prototype (UX 8
item 3), a snackbar above the modal bottom sheet (UX 8 item 4). Also questions 12 (app
name) and 21 (iOS transfer setting of Auto Backup).

## Next steps (the user gates every step)

1. The user commits the changes.
2. Loop tooling, optional: a convergence rule for `/design-loop` (e.g. rounds only on
   Blocking + new Important). Not decided.
3. Links for IDs (user wants them, later): `BHV-*`, `Decision N`, `UI-*` as Markdown
   links to `<a id>` anchors, with a script (could extend `check-refs.sh`). A separate
   change.
4. The user starts the plan phase (agreed for after the commit) (`.claude/agents/tmp-planner.md` is a temporary
   initial description of a planner agent).

## To resume

1. Ask whether the uncommitted changes were committed.
2. Ask about steps 2-3, then whether to start the plan phase. Do not start it without
   the user's explicit approval.
