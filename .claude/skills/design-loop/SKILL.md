---
name: design-loop
description: Run the designer + reviewer loop on docs/DESIGN.md in the main session - the designer agent writes, the reference check and a fresh docs-reviewer check, the designer revises - until no Blocking or Important finding is left or 3 rounds are done. Then report to the user (in Polish) with all collected questions. Started only by the user.
disable-model-invocation: true
---

# Design loop

The main session runs the loop. It spawns the agents, runs the reference check,
passes findings and decides when to stop. It does not design and does not review
itself. Talk to the user in Polish; files are in English.

This skill is the only exception to the CLAUDE.md rule "do not chain skills or
agents on your own", and only inside the design phase. It never approves the
design and never starts the plan phase.

## Start
1. Read docs/STATUS.md and the banner of docs/DESIGN.md.
2. The scope is always the whole design: the designer checks every existing part
   against the approved REQUIREMENTS and UX and updates it (see its definition).
   Propose to the user the focus inside it (by default the TO DESIGN items of the
   banner and the open next steps of STATUS that belong to the design) and the
   round limit (default 3). Wait for the user's confirmation. This is the only
   question before the end of the loop.
3. Note the date and the state: `git status` (to show the user at the end what the
   loop changed).

## Round N (N = 1, 2, 3)
1. **Designer.**
   - Round 1: spawn the `designer` agent with the confirmed focus; remind it that
     its scope is the whole design (check of the existing parts first).
   - Round 2 and later: continue the same designer with `SendMessage` (it keeps its
     context) and give it the findings to work on: the Blocking, Important and Minor
     findings of the last review that are new or still open, and the failures of the
     reference check. If the designer can no longer be continued, spawn a new one
     and give it the same plus a short list of what earlier rounds changed.
   - Wait for its final message (changes, findings, new questions, scope left).
2. **Reference check.** Run `bash .claude/scripts/check-refs.sh`. If it fails, send
   the failures to the designer at once (`SendMessage`) and run the check again;
   at most twice, then carry the failures into the review prompt as known problems.
3. **Review.** Spawn a NEW `docs-reviewer` every round (a fresh look; it must not
   read docs/reviews/ or earlier reports). Prompt it with:
   - the scope: the whole of docs/DESIGN.md, docs/DESIGN-DECISIONS.md and
     docs/diagrams/, not only what changed, with consistency against REQUIREMENTS
     and UX;
   - "changed in this round": the designer's list of changes;
   - that the questions under "Awaiting the user (design loop)" in docs/DESIGN.md
     section 10 and the places marked "Assumption (open question N, awaits the
     user)" are not findings by themselves; report them only if the chosen
     assumption causes a concrete problem (a contradiction, a failing scenario, a
     violated principle);
   - that REQUIREMENTS and UX are approved: changes to them go to "Questions for
     the user".
4. **Save the report** as `docs/reviews/<date>-design-loop-<N>.md` (English,
   condensed: verdict, findings with IDs, questions; the folder is not in git).
5. **Match the findings** (main session, not the reviewer): mark each finding as
   new, or as the same issue as a finding the designer already rejected in an
   earlier round (then it is not sent again; it goes to the user).
6. **Stop check.** Stop when the report has no Blocking and no Important finding
   other than ones the designer already rejected, or when N reached the limit.
   Otherwise start round N+1.

Between rounds do not ask the user anything. If the user writes during the loop,
answer and ask whether to go on. If the user says they must stop, follow the
CLAUDE.md rules for an interrupted session (STATUS and banner) and tell them which
round was reached.

## End
Report to the user in Polish:
1. **Result:** number of rounds, why the loop stopped, the reference check result.
2. **What changed:** by file and section, new and revised Decisions (and point to
   `git diff`).
3. **Questions for the user:** all questions of section 10 "Awaiting the user
   (design loop)" with, for the blocking ones, the designer's assessment, options
   and chosen approach; then the "Questions for the user" of the last review. Ask
   them one by one only after the report, as in the interviews (one decision at a
   time, recommendation first).
4. **Open findings:** rejected by the designer (with its reason) and not fixed
   (including Minor), each with a proposal: accept, fix, or defer.
5. **Next step:** the user answers the questions and reads the diff; then either
   another loop or the approval of the design. Do not approve and do not start the
   plan phase.

Then update docs/STATUS.md (CLAUDE.md, "Session state"): the loop result, the open
questions by number, the open findings, and the report files in docs/reviews/.
When all findings of the last report are closed or consciously deferred by the
user, delete the reports of the loop (CLAUDE.md, Layout). Do not commit.
