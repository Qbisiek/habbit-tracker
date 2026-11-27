---
name: implement-milestone
description: Run the implementation loop over one milestone of docs/PLAN.md in the main session - the orchestrator takes the ready tasks, runs coder + code-reviewer rounds (at most 3) for each in its own git worktree, up to 2 tasks in parallel, rebases and squash-merges approved tasks into the milestone branch, until no task is left that can be taken. Never merges into main and never starts the next milestone. Started only by the user with a milestone ID (e.g. /implement-milestone M2).
disable-model-invocation: true
---

# Implementation loop (one milestone)

The main session is the orchestrator. It chooses tasks, manages branches, worktrees,
statuses and the emulator, spawns the agents, passes reports and decides when a task
is merged or blocked. It does not write code and does not review. Talk to the user in
Polish; files, commits and reports are in English.

This skill is the CLAUDE.md exception for the implementation loop: it chains `coder`
and `code-reviewer` only inside one milestone. It never merges into `main`, never tags,
never starts the next milestone. The procedure of one task is sections A to E of
`.claude/skills/implement-task/SKILL.md` (followed by `coder`); this skill replaces its
sections S and Z and its gates.

## Settings (user decisions 2026-11-27)
- **Rounds:** at most 3 reviews per task. Blocking findings after round 3: the task is
  `blocked`. Non-blocking findings never stop a task.
- **Parallel tasks:** at most 2 coders at once, only ready tasks (status `todo`, all
  dependencies `done`; `python .claude/scripts/plan_ready.py <milestone>`).
- **Device:** at most 1 running task with "device: yes" at a time.
- **Merge:** after the review has no Blocking finding, the task branch is rebased onto
  the milestone branch and squash-merged by the orchestrator, not by the coder.

Names used below: `<ms>` the milestone branch (`android/CODING-GUIDELINES.md`
section 9), `<root>` the main repository directory, `<wt>` =
`<root>/../habbit-tracker-worktrees/<task id>` the task's worktree, `<reports>` =
`<root>/docs/reviews/code/<milestone>/` (not in git).

## Start
1. Read `docs/STATUS.md` and run `plan_ready.py <milestone>`.
2. `<root>` must have no uncommitted changes except `docs/STATUS.md`. The milestone
   branch exists, or create it from `main`; check it is not behind `main` (the user
   merges `main` into it otherwise). Check out `<ms>` in `<root>`; it stays checked
   out for the whole loop.
3. Show the user in a few lines: ready / waiting / blocked / in progress tasks, which
   of them need the device, the settings above. Blocked tasks: ask the user which of
   them are resolved and how (their answer goes to the coder). This is the only
   question before the end of the loop; wait for the confirmation.
4. `git worktree list`: existing worktrees of in-progress tasks are resumed, not
   recreated. Create `<reports>`; start `<reports>/loop.md` (a short log: one line per
   event, so the loop can be resumed and reported after a context compaction).

## Device
A task needs the device when it touches Persistence (instrumented tests) or has UI
(screenshots, checks on the screen): its title names Persistence, App, a screen, a
sheet or a form, or its plan notes say so. When the first such task starts:
1. `adb devices`: if a device is listed (e.g. the user's emulator from Android
   Studio), use it and do not stop it later.
2. Otherwise start in the background
   `"$LOCALAPPDATA/Android/Sdk/emulator/emulator.exe" -avd Medium_Phone_API_37.0
   -no-window -no-snapshot-save -no-audio`, wait until
   `adb shell getprop sys.boot_completed` is `1` (about 30 s), then
   `adb shell wm dismiss-keyguard`.
3. At the end of the loop: `adb emu kill`, only for an emulator the loop started.
Screenshots: `adb exec-out screencap -p > <reports>/<task id>/<name>.png`.

## Taking a task (repeat while a slot is free and a task is ready)
Take the ready tasks in plan order; skip a device task while the device is taken.
1. On `<ms>` in `<root>`: status `in progress` in `docs/PLAN.md`, commit
   `chore: <id> - start` (only that file), regenerate the dashboard
   (`python .claude/scripts/plan_dashboard.py`).
2. `git worktree add <wt> -b task/<id>-<short-slug> <ms>` (or the existing branch on
   resume), then copy `<root>/android/local.properties` to `<wt>/android/` (not in
   git, Gradle needs the SDK path).
3. Spawn `coder` in the background with: task ID and title, `<ms>`, the task branch,
   `<wt>`, `<reports>`, device yes/no, the user's answer for a resolved block, and on
   resume that the branch has commits.
4. Log it in `loop.md`. Do not wait: take the next task if a slot is free.

## When a coder finishes a round
- **Blocked:** see "Blocking a task".
- **Done:** spawn a NEW `code-reviewer` (round N) with: task ID, `<ms>`, the task
  branch, `<wt>`, `<reports>`, the round, and from round 2 on the previous findings
  with the coder's answers. Save its report as `<reports>/<id>-review-r<N>.md`.
  - No Blocking finding: if there are Non-blocking ones, send them to the same coder
    (`SendMessage`) for one last pass by its own judgment, without a new review; then
    "Merge".
  - Blocking findings and N < 3: send them to the same coder (`SendMessage`), round
    N+1. If the coder cannot be continued, spawn a new one with the findings and the
    path of its previous report.
  - Blocking findings and N = 3: "Blocking a task".

## Merge (one task at a time)
1. In `<wt>`: `git rebase <ms>`. On a conflict, send it to the coder to resolve. If the
   resolution changed code beyond the obvious, one extra `code-reviewer` round on the
   resolution only (it does not count toward the limit of 3).
2. In `<wt>`: `gradlew.bat build` (and the instrumented tests for a Persistence task,
   if it holds the device). A failure goes back to the coder as a Blocking finding.
3. In `<root>` on `<ms>`: `git merge --squash task/<...>`, status `done` in
   `docs/PLAN.md`, one commit `<type>: <id> - <task title>` with a body: summary,
   assumptions, Non-blocking findings left open, manual checks. Regenerate the
   dashboard.
4. `git worktree remove <wt>`, `git branch -D task/<...>` (squashed, so `-d` refuses),
   `git push origin <ms>`. Task branches are never pushed (a rebase would need a force
   push).
5. Log it; run `plan_ready.py` and take the newly ready tasks.

## Blocking a task
1. The coder commits what is done on the task branch; the worktree and the branch are
   kept for the resume.
2. On `<ms>`: status `blocked`, commit `chore: <id> - blocked`, regenerate the
   dashboard. Release the slot and the device.
3. Log the reason. Tasks that depend on it stay waiting; the others go on. Do not ask
   the user now.

## End
The loop ends when no task is running and none is ready.
1. Stop the emulator if the loop started it.
2. If every task of the milestone is `done`: `gradlew.bat assembleDebug` on `<ms>`,
   give the user the APK path and the checklist from "Manual test" of the milestone.
3. Report to the user in Polish:
   - **Result:** tasks done (with their squash commits), blocked, not started (why).
   - **Blocked tasks:** per task the question, the options and the coder's
     recommendation, and the open Blocking findings; then ask them one by one
     (recommendation first).
   - **Assumptions and open Non-blocking findings** worth the user's attention.
   - **Manual checks** of the tasks, and how to see the work:
     `git log <ms>` and `git diff main...<ms>`.
   - **Next step:** the user reviews the milestone branch, does the manual test and
     merges into `main`, or resolves the blocks and starts the loop again.
4. Update `docs/STATUS.md` (CLAUDE.md, "Session state"). The reports in `<reports>`
   are deleted when the user has seen them and nothing in them is open.

## During the loop
- Between tasks do not ask the user anything. If the user writes, answer and ask
  whether to go on.
- If the user must stop: tell the running coders (`SendMessage`) to commit their work
  and stop; statuses stay `in progress`, worktrees are kept; stop the emulator; update
  `docs/STATUS.md` with the tasks, branches, rounds reached and the next step.
- Never: merge into `main`, push a task branch, force push, change a document in
  `docs/` other than the statuses in `docs/PLAN.md`, start a task of another
  milestone.
