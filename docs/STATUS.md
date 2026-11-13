# Project status

Overwritten at the end of every session and when a session is interrupted (see
CLAUDE.md). It holds only the current state. History is in git and in
`docs/reviews/`. Read it at the start of a session.

Last updated: 2026-11-13.

## Where we are

- Phase: requirements (revision after the design phase started). Gate: the user
  has **not** approved `docs/REQUIREMENTS.md`.
- The design phase is paused. `docs/DESIGN.md` and `docs/DESIGN-DECISIONS.md` are
  not approved and partly obsolete (goal model, numeric entries, import).
- Session of 2026-11-13 ended by the user (not interrupted mid-task): the
  requirements were revised and reviewed three times, the fixes of the third review
  were applied, no fourth review was run.
- Nothing is committed from the 2026-11-13 work (new: STATUS.md, three review
  reports; changed: REQUIREMENTS.md, CLAUDE.md, README.md); the user reviews the
  diffs and commits.
- Caveat: the last fixes (same-day restore in BHV-HABIT-R3, N-period goals in
  BHV-GOAL-R2, import rules in BHV-IMPORT-R4, new rows GOAL-21/22, HABIT-7f,
  IMPORT-17..19) were not checked by the reviewer. Each round of fixes has added
  rules with new edge cases, so a fourth review has diminishing value; it is the
  user's choice.

## Why the requirements were reopened

Approved earlier, then changed. On 2026-11-13 the user decided that the
user-visible behavior (scenarios of "what happens when...") is a requirement and
lives in `docs/REQUIREMENTS.md` section 7, not in the design, and that the design
must read "Ideas for later" (section 8 of the requirements) and the extensibility
constraints (section 6) so that later versions need no rebuild.

## Plan (the user gates every step)

1. Requirements brought up to date. **Done.**
2. Review of the requirements by `docs-reviewer`. **Done** on 2026-11-13, report
   `docs/reviews/2026-11-13-requirements-review.md`; findings decided with the
   user and applied.
3. The user approves the requirements. **Next.** Three reviews are done
   (`docs/reviews/2026-11-13-requirements-review*.md`) and their findings applied;
   the third found no blocker. The document awaits the user's approval (or a
   further review, the user's choice).
4. Triage of the design (not started):
   - `docs/DESIGN.md` section 5.5 and the goal rules are replaced by references to
     section 7 of the requirements;
   - go through `docs/DESIGN-DECISIONS.md` and mark each decision "still valid" or
     "to revise"; log the decisions listed below;
   - reassess the open findings of `docs/reviews/2026-11-11-docs-review-3.md`
     (many may be obsolete after the goal model changed);
   - fix references (the `Was` column of the requirements, "Decisions 1-67" in
     DESIGN section 9); then remove the `Was` column and cite a decision number in
     `Source` only where the decision is still valid;
   - rework the design for numeric entries with a date and time (replaces the
     fixed time 12:00).
5. Review-design loop (not started): a designer agent and `docs-reviewer` work in a
   loop until the reviewer has no blocker / major finding, or for at most 3
   rounds. The loop stops earlier and asks the user when a finding is a gap in the
   requirements, a product trade-off, or the two agents go in circles. The designer
   reads "Ideas for later" (section 8) and section 6, leaves room for them without building
   them, adds nothing outside v1, and logs every change in
   `docs/DESIGN-DECISIONS.md`. Minor findings go to a list for the user.
   - CLAUDE.md must be changed first ("Do not chain skills or agents on your own"):
     the loop runs only inside one phase and only when the user starts it; the gate
     between phases stays.
   - Check in the official docs whether a subagent can start another subagent; if
     not, the main session runs the loop (probably through a skill).
6. Run the loop on the design; the user approves the design. The plan phase must
   not start before that.

## Decisions to log in `docs/DESIGN-DECISIONS.md` (step 4)

From 2026-11-13:
- A goal is only a definition; progress is computed. Changing or switching off a
  goal archives it (last day = today) with no question; the three options
  (Abandon / Delete the unfinished period / Wait) are gone, and so are the
  "abandoned" period and its mark.
- At most one active goal per habit; archived goals may overlap each other and the
  active goal. A retroactive start of a new goal is allowed with no restriction.
  Lowering N so that the last day is in the past asks for one confirmation.
- A goal has a first day and a last day (both included); there is no separate "end
  day". The default start of a new goal after a change that archives is the current
  period (a daily goal works from today); a replaced goal (BHV-GOAL-R4a) keeps its
  first day when the period length does not change. N is kept from the changed or
  restored goal. The form rejects a goal whose last day is before today (creation
  and replacement); the confirmation of BHV-GOAL-R6 stays for a goal archived by
  the change.
- A habit restored on the day it was archived gets its goal back unchanged, without
  a question (BHV-HABIT-R3). The creation day of a habit and of a goal is stored and
  exported; an imported goal keeps it. A replaced goal counts as created that day.
- A period is completed when its last day is before today. A goal without a
  completed period, or created today, is replaced (edited in place), not archived;
  switching such a goal off deletes it (BHV-GOAL-R4a). Not at habit archiving.
  Changing only the first day of a started goal is a change as in BHV-GOAL-R4.
- The last day of an active N-period goal is the last day of its N-th period; the
  last day of an archived goal is any day not before its first day.
- "+" and a typed value of a numeric habit belong to the selected date. The rule "no
  entries in the future" applies when adding an entry or changing its date or
  time, not to an edit of the value only and not to an import (this covers every
  "not in the future" rule of BHV-ENTRY-R5).
- Statistics are per goal, never added together (replaces the line per period
  length, Decision 62); they apply BHV-GOAL-R5 only, with no extra cutting at
  archiving.
- Archiving a habit archives its goal; restoring the habit asks whether to restore
  the goal, which creates a new goal as a copy.
- Direction and measurement type cannot change after the habit is created.
- Import has two modes, *Replace all* and *Add to existing* (imported habits are
  added as new ones, nothing is matched), with a note about equal names; replaces
  Decision 31.
- The Day screen can navigate to any date; entries cannot be added on future dates
  in v1; no progress indicator for a period that has not started.
- Numeric entries have a date and a time, kept as wall-clock time (no recompute
  after a time zone change); no entries in the future. Replaces the fixed time
  12:00 of the design (and Decision 6 for numeric entries).
- The language switch in the app (REQ-M9); the language follows the device until
  the user chooses one.

## Open

- Where the language switch lives; whether the Android per-app language setting
  can be used (verify in the official docs before the plan).
- Open questions in `docs/DESIGN.md` section 10 (7, 12, 16, 18, 21).
- App name.

## To resume

1. Ask whether the user has committed the diffs and has read the revised
   `docs/REQUIREMENTS.md`.
2. Ask whether to run a fourth review or to approve the requirements (step 3).
3. After approval: step 4 (triage of the design), then CLAUDE.md for the loop
   (step 5). Do not touch the design before the approval.
