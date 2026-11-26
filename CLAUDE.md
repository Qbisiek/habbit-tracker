# Habit Tracker

Android app (Kotlin) for habit tracking. Time tracking is one way of tracking
a habit (e.g. a timer), not a separate parallel feature. Also a learning
project: the goal is to practice a structured AI-assisted workflow.

## Workflow
- Phases: requirements -> design -> plan -> implementation.
  Artifacts: docs/REQUIREMENTS.md, docs/DESIGN.md, docs/PLAN.md, android/.
- The user is the gate between phases. Never start the next phase without
  explicit approval. Do not chain skills or agents on your own. The only
  exceptions: the design-loop skill (designer + docs-reviewer rounds): only
  when the user starts it, only inside one phase, and the loop never approves
  a document or starts the next phase; the implementation loop skill (coder +
  code-reviewer rounds): only when the user starts it, only inside one
  milestone; it never merges into `main` and never starts the next milestone.
- Git: follow the branch model in android/CODING-GUIDELINES.md section 9.
  During implementation the AI commits on `task/*` branches and squash-merges
  them into their milestone branch after the review is approved. Never commit,
  merge or push on `main`: the merge into `main` is done by the user. Outside
  implementation tasks, the user reviews diffs and commits.
- Coding rules: android/CODING-GUIDELINES.md, read before writing code.
- Before writing a file, briefly explain what it contains and why.
- Session state: docs/STATUS.md holds the current state (phase and gate, what
  was agreed, what is open, next steps). Read it at the start of a session.
  Update it (overwrite, no history) at the end of every session and, without
  being asked, when the user says they must stop or end the current session
  (interview or any other multi-step work). Do not present unconfirmed items
  as decided. Tell the user in Polish what was saved.
- Interrupted sessions: besides STATUS.md, mark at the top of the phase's
  artifact file, in a few lines, that the session was interrupted and the
  document is not approved, and point to docs/STATUS.md. Details of what was
  agreed or is undecided go to STATUS.md, not into the artifact.

## Language
- Conversation with the user: Polish.
- Everything in the repo (code, comments, docs, skills, agents): English.

## Working style
- Short answers. Be direct and critique constructively. No compliments.
- If unsure about a tool's current format or API, check the docs or say
  you don't know. Do not guess.

## Layout
- docs/ - requirements, design, plan
  - docs/UX.md - screens, navigation and layout (part of the design phase)
  - docs/STATUS.md - current state of the project and the next steps (see
    Workflow, "Session state")
  - docs/DESIGN-DECISIONS.md - decisions log of the design (cited as "Decision N")
  - docs/diagrams/ - architecture diagrams (components.drawio)
  - docs/reviews/ - the current report from docs-reviewer, not in git
    (.gitignore). Open findings of the latest report must be resolved or
    consciously deferred by the user before the plan phase starts. When all
    its findings are closed, the report is deleted; what was decided lives in
    the documents and in docs/STATUS.md.
- .claude/skills/, .claude/agents/, .claude/scripts/ - workflow tooling
  - .claude/scripts/check-refs.sh - mechanical reference check of docs/ (IDs,
    Decisions, UI-*, section references); run it before a docs review
  - .claude/scripts/req_index.py - index of requirement IDs (ID, priority,
    title) from docs/REQUIREMENTS.md; read it instead of the whole file
  - .claude/scripts/plan_dashboard.py - generates the progress dashboard
    docs/dashboard/index.html from docs/PLAN.md (not in git; PLAN.md is the
    only source of truth). Template: plan_dashboard.html
  - .claude/scripts/guard_main.py - PreToolUse hook (.claude/settings.json)
    that blocks the AI's commits, merges and pushes on `main` and force pushes
- android/ - the Android project (Gradle project not created yet)
  - android/CODING-GUIDELINES.md - coding rules for every implementer

## Scope
- v1 scope is defined in docs/REQUIREMENTS.md. New ideas go to its
  "Ideas for later" section, not into the code.
