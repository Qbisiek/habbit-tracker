---
name: plan-interview
description: Walk the user (in Polish) through the implementation plan of the app and write docs/PLAN.md (in English). Use at the start of the plan phase, after docs/DESIGN.md is approved.
disable-model-invocation: true
---

# Plan interview

Goal: produce docs/PLAN.md through a structured conversation about the ORDER in
which the approved design is built: milestones, tasks, dependencies, effort and the
coverage of the requirements. Talk to the user in Polish. Write the file in English.
This phase does not change requirements or design and does not write code.

## Start: read progressively
The documents are large (about 70k tokens together). Do not read them whole up
front. Start with a broad picture and read details only when a step needs them.
- At the start read only:
  - CLAUDE.md and docs/STATUS.md;
  - docs/DESIGN.md: the status banner, "Summary and map", section 3 (Architecture),
    section 8 (Testing strategy), section 10 (Open questions, incl. "Carried to the
    plan phase");
  - docs/UX.md: the status banner and section 1 (UI principles, the list of
    screens);
  - docs/REQUIREMENTS.md: the status banner and "Map", sections 2 (Features) and 4
    (Success criteria), and the index of IDs from
    `python .claude/scripts/req_index.py` (ID, priority, title; pass an ID prefix,
    e.g. `BHV-EVAL`, to narrow it, or `--coverage` for the IDs that need a task).
- On demand: a single rule or scenario (`BHV-*`), a section of docs/DESIGN.md or
  docs/UX.md, when a milestone or task is being planned and needs it. Read the
  section, not the file.
- docs/DESIGN-DECISIONS.md holds reasons, not what was decided (that is in
  docs/DESIGN.md). Read a row only if a question about the reason comes up.
- Check that docs/DESIGN.md is approved (its banner and docs/STATUS.md). If it is
  not, tell the user and stop.
- If docs/PLAN.md already exists, tell the user and ask whether to update it or
  start over. Never overwrite silently.

## Planning principles (fixed, from the user)
Do not re-ask whether to follow them; ask only how to apply them.
1. A running app as early as possible. Every milestone ends with something the
   user can install and test by hand on a phone, not only automated tests. Critical
   problems must show up early (fail fast). So plan iteratively: first a small
   subset of the features (e.g. one kind of habit in one direction, even without
   goals), then extend.
2. Breadth through the app before more variants. With the first kind of habit,
   prefer the other screens and actions for it (statistics, edit, archive, ...)
   over adding more kinds of habits, so that as many screens and functions as
   possible can be used and tested early.
3. Export and especially import early, even limited, because they make loading
   test data easy.
4. Function before look. The GUI will be tuned for a long time; a plain UI that
   works is enough until late milestones.
5. The checks "carried to the plan phase" (docs/DESIGN.md section 10) come first,
   as spikes, because their result can change the UX or the design.

## Rules
- Ask short questions and wait for the answers. Decisions: ONE at a time, with
  2-3 options, short trade-offs and one recommendation with the reason. Simple
  factual questions: group 2-3.
- The user decides. Raise a concern ONCE; if the user insists, record it and move
  on. Do not turn trivial choices into discussions; propose a default.
- Never invent requirements and never change the design. If planning shows a gap,
  a conflict or a costly requirement, tell the user; a change needs the user's
  explicit approval and an edit of the owning document (REQUIREMENTS, UX, DESIGN),
  never done silently.
- Every task traces to requirement IDs it implements, to a design part it builds,
  or to a planning principle (setup, spike, test data). Do not add tasks without
  such a reason.
- Stay at plan level: what is built in which order and how it is tested. No code,
  no class or method design beyond docs/DESIGN.md.
- If unsure about a tool, library or API (Android, Gradle, libraries), check the
  official docs or say you don't know. Do not guess.

## Flow (top-down)
1. Level 1 - milestones: propose the whole list first (M1, M2, ...), each with its
   goal and "what the user can test by hand afterwards". Discuss the significant
   choices one at a time.
2. Level 2 - tasks of each milestone, milestone by milestone, reading the needed
   details on demand.
3. Level 3 - dependencies, effort and the "Covers" column of every task.
- After each level: a short Polish summary and the user's confirmation. Going back
  to a higher level is allowed at any time; say so explicitly.
- At the end: a coverage check (every ID from `req_index.py --coverage`, that is
  REQ-M*, REQ-S*, REQ-C*, REQ-SC* and all BHV-* rules and scenarios, is covered by
  a task or consciously deferred by the user; list the gaps), run
  `bash .claude/scripts/check-refs.sh`, show the Polish summary and ask for the
  approval.

## Writing docs/PLAN.md
- Write the file during the conversation, not only at the end: create it after
  Level 1 is confirmed and update it after each confirmed step. Follow CLAUDE.md:
  say briefly what you write before writing.
- Until the user approves it, the file starts with a banner:
  `> **STATUS (<date>): DRAFT, NOT APPROVED.** <one line on where we are>.`
  If the session is interrupted, the banner also says in a few lines where the
  conversation stopped and points to docs/STATUS.md (details go there, per
  CLAUDE.md). Unconfirmed items are not presented as decided.
- At the approval: replace the banner with `> **STATUS (<date>): APPROVED BY THE
  USER.**`. Do not commit. Do not start the implementation; the user decides when.

## Format of docs/PLAN.md (read by a script, keep it exact)
A script (`python .claude/scripts/plan_dashboard.py`) generates the progress
dashboard from this file and reports format problems, so the milestone headings
and the task tables must follow this format. Run it after each update of the file. Free text elsewhere is allowed and
ignored by the script.

```
# Plan: <app name>

> **STATUS (...)**

## Summary
<a few lines: the order of the milestones and why>

## M1 - <short name>
**Goal:** <one or two lines>
**Manual test:** <what the user installs and checks by hand after this milestone>

| ID   | Task                    | Status | Effort | Depends on | Covers                  |
|------|-------------------------|--------|--------|------------|-------------------------|
| T1.1 | <short imperative text> | todo   | M      | -          | -                       |
| T1.2 | <...>                   | todo   | S      | T1.1       | BHV-ENTRY-1, BHV-DAY-2  |

<optional notes per task: "**T1.2:** ...">

## M2 - ...

## Deferred
<requirement IDs consciously deferred by the user, with the reason>
```

Column rules:
- ID: `T<milestone>.<n>`, stable, never reused (a removed task gets the status
  `dropped`).
- Status: `todo`, `in progress`, `done` or `dropped`. Only the implementation
  changes it, after the user confirms the task is done.
- Effort: `S`, `M`, `L` or `XL` (relative size; the dashboard counts 1, 2, 4, 8
  points). A task bigger than XL is split.
- Depends on: task IDs separated by commas, or `-`. No cycles.
- Covers: requirement IDs (`BHV-*`, `REQ-*`) separated by commas, or `-` for setup
  tasks and spikes.
