# Requirements review 3, 2026-11-13

Status: findings decided with the user on 2026-11-13 and applied to
`docs/REQUIREMENTS.md`. Summarized from the `docs-reviewer` report (Polish,
translated and shortened when saved). Scope: only `docs/REQUIREMENTS.md`.
Verdict before the fixes: no blocking findings; approvable after cheap text fixes.
All recomputed dates, counts and percentages agreed. The fixes of review 2 were
applied, two of them only partly (I5 -> M5 here, M7 -> I6 here).

## Important

- I1. BHV-ENTRY-R3 (no entry can be edited after today) contradicted
  BHV-ENTRY-21 (value edit of an entry dated after today, possible after a time
  zone change).
- I2. The creation day of a goal decides "replaced or archived" (R4a) but Fixture S
  and GOAL-3, 16, 17 did not state it.
- I3. BHV-GOAL-R2 defined the last day of an archived goal as the day of archiving,
  which is wrong for an N-period goal archived automatically the day after its last
  day.
- I4. The import exemption from the "not in the future" rule named only numeric
  entries, not the start of a duration entry (round trip after the clock was moved
  back).
- I5. The creation day of an imported goal was not defined; it must be stored and
  exported.
- I6. Archiving and restoring a habit on the same day left an archive entry and
  restarted an N-period goal.
- I7. A goal for N periods whose last day is before today at creation was
  archived at once and could not be corrected.

## Minor

M1 header and STATUS.md stale; M2 the decision list in STATUS.md contradicted R4a;
M3 R1 pointed to R4 only; M4 wording of the "gap" in HABIT-7e; M5 restore: start
always in the current period, no form; M6 a replaced goal counts as created that
day, row for two changes on one day; M7 ENTRY-4 described a change over time; M8
"e.g." in the list of DAY-R2.

## Structure

- STATS-R5 only links to HABIT-R3 (one owner of the archiving rule). Applied.
- A decision table for changing a goal at the top of 7.2: rejected (duplicates
  R4, R4a, R6, risk of drift).

## Decisions of the user (2026-11-13)

- P1 (A): a habit restored on the day it was archived gets its goal back unchanged
  (the archive entry is removed; an N-period goal keeps its last day); nothing is
  asked.
- P2 (A): the creation day of an imported goal is taken from the file; it is part
  of the export.
- P3 (A): the form rejects a goal whose last day would be before today, at creation
  and when editing a replaced goal (R4a); for a change that archives the goal,
  BHV-GOAL-R6 keeps its confirmation.
- Minor findings and I1-I4: as recommended by the reviewer.
