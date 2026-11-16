# Project status

Overwritten at the end of every session and when a session is interrupted (see
CLAUDE.md). It holds only the current state. History is in git and in
`docs/reviews/`. Read it at the start of a session.

Last updated: 2026-11-15.

## Where we are

- Phase: design (after the revision of the requirements). Gate:
  `docs/REQUIREMENTS.md` is **approved** by the user (2026-11-14). The design is
  **not** approved: `docs/DESIGN.md` and `docs/UX.md` were cleaned up but not
  reviewed by `docs-reviewer` and not approved.
- Session ended by the user at a clean point: the cleanup of the design (steps 1-3
  below) and the triage of the old review are done; no new design content has been
  written yet.
- Nothing from this session is committed. Changed: `docs/REQUIREMENTS.md`,
  `docs/DESIGN.md` (rewritten), `docs/DESIGN-DECISIONS.md`, `CLAUDE.md` (Layout),
  `README.md`, `docs/reviews/2026-11-11-docs-review-3.md` (status line). New:
  `docs/UX.md`. The user reviews the diffs and commits. Some older review reports
  are deliberately left uncommitted by the user.

## Done in this session

1. `docs/DESIGN-DECISIONS.md`: removed the superseded Decisions 15, 31, 42, 52, 62,
   67, 68, 69; revised 6, 13, 17, 20, 28, 29, 38, 40, 41, 49, 50, 53, 55, 59, 63, 70
   and the rule pointers of the rest; added Decisions 73-81 (goal model, replacement
   of a goal, periods per goal and statistics per goal, archiving and restoring a
   habit, immutable direction and type, import modes, dates on the Day screen,
   wall-clock numeric entries, language). The reasons of 73, 76, 79 and 81 and the
   options of 76, 79 and 81 are not recorded: marked "to be added by the user".
2. Old IDs (`R-*`, `INV-*`, scenarios A-J) mapped to `BHV-*` and the references
   rewritten. The only old scenario without a counterpart is G8 (Entries port reads
   a range with an overlapping entry); it stays in `docs/DESIGN.md` section 8.
3. `docs/DESIGN.md` cleaned: behavior rules and the scenario table removed, the
   mechanisms kept (section 5), numbering of sections 1-10 kept, section 6 moved to
   the new `docs/UX.md`. Parts marked **TO DESIGN** wait for the next step.
4. `docs/REQUIREMENTS.md`: banner set to approved, the `Was` column removed, `Source`
   citations of removed decisions replaced. Added with the user's consent:
   BHV-TIMER-R2 (stop length rounded to the nearest second), BHV-TIMER-R5 (import with
   running timers when the notification permission was never requested),
   BHV-GOALVAL-R1 (a 24 h goal on a 23 h / 25 h day), and rows BHV-TIMER-9a/9b,
   BHV-NOTIF-4, BHV-GOALVAL-7. These additions were not reviewed by the reviewer.
5. `docs/reviews/2026-11-11-docs-review-3.md` triaged (see its status line). Open
   from it: I4, I5 (P4), M7, M10, structure proposal 2 (all in the list below).
6. `CLAUDE.md` Layout lists `docs/UX.md`. The rule for the review loop is not changed
   yet.

## Next steps (the user gates every step)

A. **New design content** (recommended order: UX first, because the state of the
   screens in Presentation follows from it):
   - `docs/UX.md` section 7: the open UX points (defaults of manual entries, language
     switch, look of the indicator and the quit action control, order of the Habits
     list and Statistics cards, UI element IDs, message of a failed Stop from the
     notification, the new layouts required by the requirements).
   - `docs/DESIGN.md` **TO DESIGN** items: the Goal entity and its invariants
     (BHV-GOAL-R1 to R7); Numeric entry with date and time; creation day and time of
     habits and goals; import in two modes (new identities for imported habits,
     REQ-X9); a section on how a later version adds REQ-X1 to X6; export range (read
     up to the latest entry, not only to today; recommended option (a) of the old
     question P4); the notification receiver calls the Domain operation "stop timer"
     (old I4); review of the component diagram (arrow labels, Decision 71, restore as
     a Data operation).
B. **CLAUDE.md:** the rule for the designer + reviewer loop (only inside one phase and
   only when the user starts it; the gate between phases stays). First check in the
   official docs whether a subagent can start another subagent (not known); if not,
   the main session runs the loop.
C. **Reviewer improvement** (discussed, nothing changed; the user has not decided):
   recommended order: (1) a script for mechanical checks (references to `BHV-*`,
   `Decision N` and sections exist, table columns, no old IDs) run by the main
   session before a review; (2) review in several passes with different lenses
   (traceability, user scenarios, consistency, structure) by naming the scope in the
   prompt; (3) working files of the reviewer in `docs/reviews/work/` (not committed)
   only if reports still miss things; this needs the Write tool and weakens the
   read-only guarantee of `.claude/agents/docs-reviewer.md`.
D. Review loop on the design, then the user approves the design. The plan phase must
   not start before that. Open findings of the latest review report must be resolved
   or consciously deferred before the plan; a new review of the cleaned design is
   still to be run.

## Open

- Reasons and options for Decisions 73, 76, 79, 81 (user to supply).
- Where the language switch lives; whether the Android per-app language setting can
  be used (verify in the official docs before the plan).
- Open questions in `docs/DESIGN.md` section 10 (7, 12, 21); app name.

## To resume

1. Ask whether the user committed the diffs listed above.
2. Ask which of A, B, C to start with (recommended: A, UX first).
3. Do not start the plan phase.
