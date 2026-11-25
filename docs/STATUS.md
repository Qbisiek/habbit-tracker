# Project status

Overwritten at the end of every session and when a session is interrupted (see
CLAUDE.md). It holds only the current state. History is in git. Read it at the start
of a session.

Last updated: 2026-11-25 (session interrupted by the user).

## Where we are

- Phase: plan finished. `docs/PLAN.md` approved by the user on 2026-11-25.
- Gates: `docs/REQUIREMENTS.md`, `docs/UX.md`, `docs/DESIGN.md` (with
  `docs/DESIGN-DECISIONS.md` and the diagram) approved on 2026-11-24; `docs/PLAN.md`
  approved on 2026-11-25.
- Implementation not started. Current work: preparing the implementation tooling
  (coding guidelines, git branch model, skills and agents). No file of this work
  exists yet.
- Uncommitted: `docs/PLAN.md` (M4-M9 tasks, level 3, approval banner) and this file.

## Implementation tooling: decided by the user (2026-11-25)

- Goal: the user is involved as little as possible in the implementation itself
  (requirements, design and plan are detailed enough). Target: a coder -> reviewer
  loop that may commit on branches and merge into a development branch.
- The first few tasks are done interactively in the main session, to see how much
  interaction is really needed; the loop comes after that.
- Coding guidelines: one single source of truth close to the code, the same for
  every implementer (skill, agent or human). Content: preferred design patterns,
  approach to writing code, preferred structures, naming, comments, etc. They must
  NOT copy `docs/DESIGN.md` or the diagram; they refer to them.
- Git: `main` holds only finished milestones tested by hand. Direction agreed:
  a branch per milestone, task branches merged into the milestone branch, manual
  verification at the end of each milestone, then a merge into `main`.
- Progressive reading is a requirement for every new skill and agent: read maps,
  indexes and `req_index.py` first, then only the sections the task needs; never
  load all documents at once.
- Order of work (accepted): 1. guidelines, 2. git model, 3. `implement-task` skill
  with interactive T1.1-T1.3, 4. agents and the loop.

## Proposals of Claude, NOT decided (waiting for the user)

- Guidelines file: `android/CODING-GUIDELINES.md`, referenced by one line in
  CLAUDE.md and read by every implementation skill and agent. Before drafting,
  check what `docs/DESIGN.md` already fixes (layers, DI, patterns) to avoid
  contradictions. Topics: style and formatter enforced in Gradle (ktlint or
  detekt); package structure (by feature or by layer), file size, `internal` by
  default; naming (classes, ViewModel / UiState / events, composables, string keys,
  test names); patterns (immutability, `sealed` states and results, error handling,
  coroutines and Flow rules, injected `Clock` and dispatchers, no singletons);
  Compose (stateless, state hoisting, previews); comments (why, not what; KDoc for
  the public API of `domain`; IDs `REQ-*` / `BHV-*` / `Decision N` in tests, in
  production code only where they explain a non-obvious rule); tests (fakes instead
  of mocking libraries, fixtures, test-first for `BHV-*`); a new library is a design
  change and needs the user's approval; Definition of Done and the commit message
  format. Tool versions are filled in after T1.1. Form: a draft with default
  recommendations, only the real choices (about 6-8) marked for the user.
- Branch model details: `milestone/m1-skeleton` from `main`;
  `task/t1.2-gradle-project` from the milestone branch; the AI commits freely on
  task branches; after reviewer approval and a green build and tests, a squash
  merge into the milestone branch with the message `T1.2: <title>`, then the task
  branch is deleted. End of milestone: the AI builds the APK and a checklist from
  "Manual test" of `docs/PLAN.md`; fixes as further tasks; after the user's
  acceptance a `--no-ff` merge into `main` and a tag `m1`, done by the user or by
  the AI only on the user's explicit command.
- Protection of `main`: a PreToolUse hook that blocks commit / merge / push on
  `main` (via the `update-config` skill); optionally GitHub branch protection.
- CLAUDE.md changes needed by the git model and the loop: the rules "Never run git
  commit or git push" and "Do not chain skills or agents on your own" (text to be
  proposed to the user).
- The loop: agents `coder` and `code-reviewer` (read-only, fresh context; checks the
  diff against the guidelines, `docs/DESIGN.md` and the `Covers` IDs), a loop skill
  with a round limit and an escalation to the user when a design problem appears.

## Open questions for the user

1. Start with the draft of `android/CODING-GUIDELINES.md` in the proposed form
   (location and topics)?
2. May the AI push branches other than `main` to `origin` (GitHub)?
3. Optional: a `docs-reviewer` review of `docs/PLAN.md` before the guidelines.

## Deferred (consciously, by the user)

From the review of the second design loop (report deleted):
- M4 placeholders in Decisions 73, 76, 79, 81.
- M5 diagram arrow App -> GUI / Platform services "creates".
- M7 UX 8 item 4 fallback vs "No screen subscribes to these streams".
- M8 Map by topic in DESIGN has no Language entry.
- S1 split the Domain tests sentence of DESIGN 8 by topic with BHV IDs.
- S2 shorten "Resolved" 22-26 in DESIGN 10.
- Decision 92 bundles several decisions in one row (cosmetic).
Tooling, optional, later: a convergence rule for `/design-loop`; links for IDs
(`BHV-*`, `Decision N`, `UI-*`) as Markdown anchors with a script.

## Next steps (the user gates every step)

1. The user reviews and commits `docs/PLAN.md` and this file.
2. The user answers the open questions above.
3. Draft of the coding guidelines, then the git model and the CLAUDE.md changes.
4. `implement-task` skill; T1.1 (check the current versions of the toolchain and
   libraries) as the first interactive task.
