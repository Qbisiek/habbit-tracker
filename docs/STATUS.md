# Project status

Overwritten at the end of every session and when a session is interrupted (see
CLAUDE.md). It holds only the current state. History is in git. Read it at the start
of a session.

Last updated: 2026-11-24.

## Where we are

- Phase: design. Gates:
  - `docs/REQUIREMENTS.md`: re-approved by the user 2026-11-24.
  - `docs/UX.md`: approved by the user 2026-11-24.
  - `docs/DESIGN.md`, `docs/DESIGN-DECISIONS.md`, `docs/diagrams/components.drawio`:
    revised by the first run of `/design-loop` (2026-11-24, 3 rounds); not approved.
- Uncommitted: the three design files above (the loop's changes; `git diff`), this
  file. Review reports of the loop in `docs/reviews/` (not in git):
  `2026-11-24-design-loop-1.md`, `-2.md`, `-3.md`.

## Result of the design loop (2026-11-24)

- 3 rounds, stopped at the round limit. Reference check passed after every round.
- The designer fixed every finding of rounds 1 and 2 and rejected none; declined two
  structure proposals (a Status column in DESIGN-DECISIONS; a table of Domain
  operations in the last round).
- No TO DESIGN item left. New Decisions 90-98 (proposed by the designer); Decisions
  32, 57, 59, 70, 71 revised by the designer. All await the user.
- Open questions awaiting the user, `docs/DESIGN.md` section 10 (each with the
  designer's assessment, options, chosen assumption and proposed wording):
  22 (undo: one offer at a time; "that day" of a Duration entry), 23 ("next start of
  the app" = every move to the foreground), 24 ("archived later" = added later; goal
  blocks newest first), 26 (imported archived N-period goal with a late last day:
  accept, earlier day counts) - all four blocking with assumptions; 25 (five proposed
  scenario rows for REQUIREMENTS) not blocking.
- Open findings of the last review (`docs/reviews/2026-11-24-design-loop-3.md`), not
  sent to the designer (limit reached): I1 messages shown after navigation have no
  owner; I2 no trigger for UI-DIALOG-NOTIFICATIONS after a refusal; I3 habit order
  after a language change vs Decision 9 (ViewModel survives) and the effective
  language; I4 snackbar above the modal bottom sheet unverified; M1-M7. Its questions
  for the user: Q1 channel for messages across screens, Q2 screen after archiving from
  the form (UX change), Q3 the stale list of design questions in REQUIREMENTS 5.
- Also from the loop: the designer cannot run `check-refs.sh` (no Bash; the main
  session runs it, as intended). In rounds 1 and 2 `docs-reviewer` reported that
  CLAUDE.md lacks the scripts and the loop exception, which was wrong (it probably
  read a stale copy); in round 3 it was told to read CLAUDE.md from disk and did not
  repeat it. UX 8 item 2 is verified by the designer (open question 7, resolved).
- Assistant's view of the first run (not discussed yet): each round found new
  Important gaps at a finer level (mechanisms, then app-wide events), so 3 rounds did
  not converge; the findings are real but smaller each round.

## Answers of the user (2026-11-24, after the loop)

- Q22: (a1) one undo offer, a new one replaces it; (b1) "that day" = every day the
  deleted entry covers. Written into UX 4 and BHV-ENTRY-R6a (approved by the user
  with the wording). DESIGN 10 still lists 22 as awaiting: the next loop or the main
  session moves it to resolved and drops the "Assumption" marks.
- Q23: (a) "next start" = every time the app is opened or returns to the foreground,
  for the notification and the one-time message. Written into BHV-TIMER-R5,
  BHV-NOTIF-1, BHV-NOTIF-4. DESIGN 10: as for 22.
- Q24: (a) "archived later" = added later on the same archiving day; goal blocks on a
  Statistics card newest first. Written into BHV-DAY-R2 and UX 6. DESIGN 10: as for 22.
- Q26: (b) accept the file; a last day after the N-th period counts as the last day
  of the N-th period. Written into BHV-IMPORT-R4. DESIGN 10: as for 22.
- Q25: (a) all five rows added: BHV-NOTIF-5, BHV-IMPORT-9a, 9b, BHV-ENTRY-25, 26
  (source "assistant (new)"). DESIGN 10: as for 22; the design should cite the IDs.
- Review Q1 (findings I1, I2 of round 3): (a) the App shell accepts "show message or
  dialog" from screen Presentation objects (one way); the same channel shows
  UI-MSG-IMPORT-DONE after navigation and UI-DIALOG-NOTIFICATIONS after a refusal.
  To be designed in the next loop.
- Review Q2: (b) after "Archive" the form becomes the read-only view of the archived
  habit, UI-MSG-TIMER-ARCHIVED shown there. Written into UX 5 (wording approved).
- Review Q3: (b) REQUIREMENTS 5 points to DESIGN 10 without numbers. Done.
- Open findings: I1, I2, I3, M1-M7 go to a second loop of ONE round (user: move on to
  the plan as soon as there is no blocker, to get a first testable app quickly); I4
  carried as `docs/UX.md` section 8 item 4; UX 8 item 2 marked verified.
  `docs-reviewer` definition: read CLAUDE.md from disk.
- Second loop (one round) done 2026-11-24; review
  `docs/reviews/2026-11-24-design-loop-2-1.md`: B1 blocking (typed time in the
  repeated hour vs BHV-ENTRY-26), I1, M1-M9. User accepted: designer fixes B1, M2, M3,
  M6, M9 and the I1 fallback sentence + DESIGN 10 "Carried to the plan" list, without
  a new review (user reads the diff); Q1 (a) "verify" items become the first spike
  tasks of the plan (done in REQUIREMENTS 5, 7.8 and UX 8); Q2 (a) offset in
  "changed" (done in BHV-ENTRY-R6b, UX 1); M4, M5, M7, M8, S1, S2 deferred; S3 (remove
  all "awaits the user" marks) at the approval.
- **Session cut by the usage limit** while the designer was applying the targeted fix.
  On resume: check `git diff docs/DESIGN.md docs/DESIGN-DECISIONS.md` that B1, M2,
  M3, M6, M9 and the I1 items are in (redo the missing ones), run
  `bash .claude/scripts/check-refs.sh`, then the user reads the diff and decides on
  the approval of the design (then S3).

## Next steps (the user gates every step)

1. **The user answers the questions** one at a time: design questions 22, 23, 24, 26,
   25, then the reviewer's Q1-Q3; decides on the open findings I1-I4 and M1-M7
   (fix in another loop, fix now, or defer to the plan); opens the diagram in draw.io.
   Answers that change REQUIREMENTS or UX need the user's explicit approval of the
   wording.
2. Then either another `/design-loop` (focus: the answers and the open findings) or
   the approval of the design. Remove the loop reports from `docs/reviews/` when their
   findings are closed or deferred.
3. **Loop tooling** (after the first run): consider telling `docs-reviewer` in its
   definition to read CLAUDE.md from disk; consider a convergence rule (e.g. rounds
   only on Blocking + new Important). Not decided.
4. **Links for IDs** (user wants them, later): references like `BHV-HABIT-R4`,
   `Decision N`, `UI-*` as Markdown links to their definition (`<a id>` anchors), with
   a script (could extend `check-refs.sh`); check the anchors in IntelliJ. A separate
   change.
5. The user approves the design. The plan phase must not start before that.

## Open

- Reasons and options for Decisions 73, 76, 79, 81 (user to supply).
- To verify before the plan: `docs/UX.md` section 8 (item 1 notification language;
  item 3 prototype widths incl. "23h59m58s/23h59m59s"; item 2 verified by the designer,
  to mark in UX with the user's approval).
- `docs/DESIGN.md` section 10: 12 (app name), 21 (iOS transfer setting) carried to the
  plan.

## To resume

1. Ask whether the uncommitted changes were committed.
2. Go through Next steps 1 (questions one at a time, recommendation first).
3. Do not start the plan phase.
