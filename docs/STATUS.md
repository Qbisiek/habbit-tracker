# Project status

Overwritten at the end of every session and when a session is interrupted (see
CLAUDE.md). It holds only the current state. History is in git. Read it at the start of a
session.

Last updated: 2026-11-24.

## Where we are

- Phase: design. Gates:
  - `docs/REQUIREMENTS.md`: **re-approved by the user 2026-11-24** (after the
    amendments listed below).
  - `docs/UX.md`: **approved by the user 2026-11-24**.
  - `docs/DESIGN.md`: has **TO DESIGN** parts (banner updated 2026-11-23), not
    reviewed, not approved. Next work.
- Banners of REQUIREMENTS and UX and the status line of README updated 2026-11-24.
- Nothing committed since 2026-11-23 (check with `git status`). Uncommitted:
  `docs/UX.md`, `docs/REQUIREMENTS.md`, `docs/DESIGN-DECISIONS.md`, `docs/DESIGN.md`,
  `README.md`, this file. All review reports were deleted on 2026-11-24 at the user's
  request (one of them was tracked, so its deletion is in the diff).

## Changes to REQUIREMENTS since 2026-11-14 (re-approved 2026-11-24)

- 2026-11-16: BHV-DAY-R2, BHV-ENTRY-R5, ENTRY-8, ENTRY-19, BHV-IMPORT-R4, the language
  paragraph before BHV-LANG-R1, one "Ideas for later" item.
- 2026-11-23: banner; section 5; the Source convention of section 7; rules BHV-DAY-R2,
  BHV-ENTRY-R3 (a timer can be started only today), BHV-ENTRY-R6 (undo, withdrawn on
  conflict, no undo for the Day toggle, times to the minute), BHV-TIMER-R1 (listed on
  every day it covers, undo), BHV-TIMER-R2 (half up), BHV-IMPORT-R2 ("Export and
  replace"), BHV-HABIT-R3 (archived habit: only restore or delete); rows DAY-9a-d,
  DAY-10a-b, DAY-11, DAY-12a-b, ENTRY-8 (source), ENTRY-21a-c (split), ENTRY-22a-d,
  ENTRY-23a-b, ENTRY-24, TIMER-9c, TIMER-10a-c, TIMER-11a-b, IMPORT-5a-e, IMPORT-20,
  EXPORT-3, HABIT-17, HABIT-18.
- 2026-11-23, after the third review: BHV-ENTRY-R6 split into R6, R6a (undo) and R6b
  (time precision, seconds per time field, quick buttons, "+" entries, equal times);
  BHV-HABIT-R2 (unit warning only when something is relabeled); BHV-HABIT-R3 wording;
  7.4 intro; Source convention ("(new)"); rows DAY-12b ("0:40"), ENTRY-4 (source
  Dec. 80), ENTRY-23c, HABIT-19, HABIT-20.
- 2026-11-24, triage of the targeted check: Map row 7.4 (R6a, R6b); 7.4 intro
  ("default dates and times"); BHV-ENTRY-R5 (a date or time set back is not changed);
  BHV-ENTRY-R6b ("changed" = hour and minute at Save differ from the stored ones; "+"
  with seconds only on today's date); BHV-HABIT-R2 (check against the stored habit);
  rows DAY-11 ("1h20m/3h"), DAY-12a ("3h1m/3h"), DAY-12b ("40s"), new ENTRY-21d,
  ENTRY-23d.

## Done in this session (2026-11-24)

1. Triage of the targeted check of 2026-11-23, all findings closed:
   - I1 (a): two terms in UX 1, "touched" (sticky, only for showing a message) and
     "changed" (value at Save differs from the stored value; decides BHV-ENTRY-R5, R6b).
   - I2: compact units everywhere in the Duration text of the Quit ring ("1h20m/3h",
     "3h1m/3h", "40s"); replaces the m:ss choice (Q2 (b)) of the third review. Decision
     82 updated (also M1).
   - M2-M4, M6-M9, M11, M13, M14 fixed as proposed, accepted by the user as a whole.
     M10 fixed with I2. M5 does not exist; M12 checked earlier.
2. Mechanical reference check (script in the session, not saved in the repo): every
   `BHV-*`, `REQ-*`, `Decision N`, `UI-*` reference and every "`docs/X.md` section N"
   reference has a definition; no ID is defined twice; all UI screens are from the
   closed list; the unused Decision numbers are the ten listed in DESIGN-DECISIONS.
3. Choices made by the assistant within the fixes (the user may change them):
   - Duration units: zero units are left out ("3h", "1h30s"), a duration of 0 is "0",
     no spaces. The value of the day in the row (UI-DAY-VALUE) keeps "1 h 20 min"
     (user agreed).
   - BHV-HABIT-R2: a goal entered in the same form, like a step, does not trigger the
     unit warning.
   - "+" on a past date stores 12:00:00 (UX 3).

## Next steps (the user gates every step)

1. (Done 2026-11-24: REQUIREMENTS re-approved, UX approved.)
2. **New design content in `docs/DESIGN.md`** (**TO DESIGN** items in its banner): the
   Goal entity and its invariants (BHV-GOAL-R1 to R7); Numeric entry with date and time;
   creation day and time of habits and goals; import in two modes (REQ-X9) and "Export
   and replace" (Decision 88); undo withdrawn on conflict (Decision 86); notification
   port: update of the text; the Stop receiver through the Domain operation (Decision
   71, old I4); a section on how a later version adds REQ-X1 to X6; export range (read
   up to the latest entry, recommended option (a) of the old question P4); restore of a
   habit on the archiving day (reactivation, BHV-HABIT-R3) in the Data operations; review of
   the component diagram (arrow labels, Decision 71, restore as a Data operation).
3. **CLAUDE.md:** the rule for the designer + reviewer loop (only inside one phase and
   only when the user starts it; the gate between phases stays). Checked in the
   official docs 2026-11-24 (code.claude.com/docs/en/sub-agents, /skills): a subagent
   can start subagents (default up to 3 levels, `Agent` in its `tools`); a finished
   subagent can be continued with its context via `SendMessage`; a skill with
   `context: fork` runs one subagent, no documented loop feature. Decided and written
   2026-11-24: the main session runs the loop through the skill `design-loop`
   (`.claude/skills/design-loop/SKILL.md`) with the new agent `designer`
   (`.claude/agents/designer.md`) and a fresh `docs-reviewer` per round; the scope is
   always the whole design (the designer first checks the existing parts against the
   approved REQUIREMENTS and UX), the TO DESIGN items are only the focus; stop when
   no Blocking or Important finding is left, at most 3 rounds; the designer records
   questions in `docs/DESIGN.md` section 10 and goes on (blocking ones with an
   assessment, options and a chosen assumption, by the priorities in its
   definition); it may reject a finding with a reason. CLAUDE.md has the exception
   to "do not chain". Not tried yet: the first run is the test (does `SendMessage`
   continue the designer; does the reviewer leave the awaiting questions alone). Lesson: do not ask `docs-reviewer` to read earlier
   reports (its definition forbids it); give it the list of fixes in the prompt or match
   "new vs already open" in the main session.
4. **Reviewer improvement:** the reference check is saved as
   `.claude/scripts/check-refs.sh` (2026-11-24, listed in CLAUDE.md). Still only
   discussed: several review passes with different lenses.
5. **Links for IDs** (user wants them, later): references like `BHV-HABIT-R4`,
   `Decision N`, `UI-*` as Markdown links to their definition (`<a id>` anchors). About
   600 references, so only with a script (could be the same as step 4); the user reads
   in IntelliJ, so check there that the anchors work. A separate change, not mixed with
   content fixes.
6. Review loop on the design, then the user approves the design. The plan phase must not
   start before that.

## Open

- Reasons and options for Decisions 73, 76, 79, 81 (user to supply).
- To verify before the plan: `docs/UX.md` section 8 (notification texts and per-app
  language, updating the notification text, prototype widths incl. the widest Duration
  ring text "23h59m58s/23h59m59s").
- Open questions in `docs/DESIGN.md` section 10 (7, 12, 21); app name.

## To resume

1. Ask whether the uncommitted changes listed above were committed.
2. The loop is ready (Next steps 3). The user starts it with `/design-loop` on the
   TO DESIGN parts (Next steps 2); ask whether to start it.
3. Do not start the plan phase.
