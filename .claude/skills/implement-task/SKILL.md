---
name: implement-task
description: Implement one task of docs/PLAN.md interactively in the main session - start or resume it on its task branch, read only the context it needs, propose a task plan, write tests first and code, build, check the Definition of Done and show the result to the user for review; after approval squash-merge it into the milestone branch. Started only by the user with a task ID (e.g. /implement-task T1.2).
disable-model-invocation: true
---

# Implement a task

Talk to the user in Polish; code and files are in English. The coding rules are
`android/CODING-GUIDELINES.md` (including the branch model, section 9, and the
Definition of Done, section 10); this skill does not repeat them.

The skill has two parts:
- **Task procedure** (sections A to E): how one task is done. It is the only
  description of that work; a future `coder` agent follows the same sections.
- **Orchestration** (sections S and Z): choosing the task, branches, statuses in
  `docs/PLAN.md`, merge. Here it is done by the main session with the user. In the
  future implementation loop it is done by the loop skill (not written yet), never by
  the coder.

## S. Start (orchestration)
1. Read `docs/STATUS.md`. Find the task's row and the notes under its milestone table
   in `docs/PLAN.md` (search for the ID; do not read the whole plan).
2. The task can be taken when its status is `todo` or `in progress` and every task in
   `Depends on` is `done`. Otherwise stop and tell the user why.
3. Branches (`android/CODING-GUIDELINES.md` section 9):
   - the milestone branch exists, or create it from `main`; it must not be behind
     `main`;
   - `in progress` with an existing task branch: **resume**. Check it out, read
     `git log` and `git diff <milestone branch>...HEAD`, and in A to C continue from
     what is there instead of starting over;
   - otherwise create `task/<id>-<short-slug>` from the milestone branch.
4. On the milestone branch set the status to `in progress` in `docs/PLAN.md` and
   commit it (`chore: <id> - start`), before the task branch is created. Statuses are
   changed only here and in Z, never on a task branch, so parallel task branches never
   conflict on them.
5. After every status change, regenerate the dashboard:
   `python .claude/scripts/plan_dashboard.py` (it renders the checked-out
   `docs/PLAN.md`; the output is not in git).

## A. Context (read progressively)
1. `android/CODING-GUIDELINES.md` in full.
2. The `Covers` IDs: `python .claude/scripts/req_index.py` for their titles, then only
   the sections of `docs/REQUIREMENTS.md` with those IDs (search by ID).
3. `docs/DESIGN.md`: the "Summary and map" (map by topic), then only the sections the
   task needs; Decisions only when a section cites them for the task.
4. `docs/UX.md` only for a task with UI: its map, then the screens of the task.
5. The existing code the task touches (search first, then read the files).
Never load all documents at once. If something needed is missing or unclear, it goes
to the plan of the task as a question, not into an assumption in code.

## B. Plan of the task (gate 1)
Show the user, short:
- what will be built and in which components, packages and files;
- the tests: which `BHV-*` scenarios at which level (`docs/DESIGN.md` section 8);
- the steps in order;
- questions and risks, each with a recommendation.
Wait for the user's approval. Gate 1 is mandatory for every task of M1 (user decision
2026-11-26); from M2 on the tasks run in the implementation loop.

## C. Implementation
1. Stop and ask the user (do not decide in code) when the task needs:
   - a new library, also a test or build tool;
   - a change of `docs/REQUIREMENTS.md`, `docs/UX.md`, `docs/DESIGN.md`,
     `docs/DESIGN-DECISIONS.md` or `docs/PLAN.md` beyond the status;
   - a choice the documents do not make, or a conflict between them and the code;
   - more scope than the task (it goes to the user as a proposed new task).
2. For each scenario: minimal API so the test compiles, the failing test, the code,
   the test passes (`android/CODING-GUIDELINES.md` section 7). Then the next one.
3. Commit on the task branch in small steps (`android/CODING-GUIDELINES.md`
   section 9, commit messages).
4. When a UI layout of the documents does not work on the device (it does not fit,
   it is not clean or not readable), step back before patching it: question the
   approach itself, not only its parameters. One or two tweaks of the same idea
   (sizes, paddings, font) are fine; if they do not solve it, look for a different
   approach: another element (a bar instead of a ring), another arrangement (a second
   line, wrapping, moving an element to its own line), icons instead of text, less
   precision in a text (fewer units, rounding). Prototype and measure two or three
   such alternatives, then propose them to the user with a recommendation (rule 1:
   the change of `docs/UX.md` is the user's decision). Example: T1.6, where a text
   inside a progress ring never fitted and the answer was a rectangular bar in a
   two-line row, not a bigger ring.

Tasks without code (research such as T1.1, document changes such as T1.8): the result
is a document change. Each change is shown to the user and written only after the user
approves it. Facts about tools and libraries come from their official docs, with the
link; if a fact cannot be confirmed, say so.

## D. Verification
1. In `android/`: `./gradlew build` (on Windows `gradlew.bat build`); also the
   instrumented tests when the task touches Persistence and a device or emulator is
   available, otherwise tell the user they were not run.
2. Go through the Definition of Done (`android/CODING-GUIDELINES.md` section 10) item
   by item. Fix what fails; what cannot be fixed goes to the report.

## E. Report (gate 2)
Show the user:
- what was done, by component, and the commits of the task branch;
- the result of the build and the tests (numbers, failures if any);
- the Definition of Done, each item met or not and why;
- deviations from the plan of the task and open questions;
- the manual checks for the user, if the task has any;
- how to see the diff: `git diff <milestone branch>...<task branch>`.
Fixes asked by the user are made on the same task branch, then D and E again.

## Z. Close (orchestration, after the user approves the result)
1. Squash-merge the task branch into the milestone branch with the message
   `<type>: <id> - <task title>` (`android/CODING-GUIDELINES.md` section 9).
2. On the milestone branch set the status to `done` in `docs/PLAN.md` in the same
   commit as the squash. Regenerate the dashboard (S5).
3. Delete the task branch; push the milestone branch to `origin`.
4. If it was the last task of the milestone: build the APK, give the user its path and
   the checklist from "Manual test" of the milestone. The merge into `main` is done by
   the user.
5. Update `docs/STATUS.md` (CLAUDE.md, "Session state") and tell the user in Polish
   what was saved.

If the user must stop in the middle: commit the work done so far on the task branch
(the status stays `in progress`, so the task can be resumed), update
`docs/STATUS.md` with the task, its branch and the next step.
