# Habit Tracker

Android app (Kotlin) for habit tracking. Time tracking is one way of tracking
a habit (e.g. a timer), not a separate parallel feature. Also a learning
project: the goal is to practice a structured AI-assisted workflow.

## Workflow
- Phases: requirements -> design -> plan -> implementation.
  Artifacts: docs/REQUIREMENTS.md, docs/DESIGN.md, docs/PLAN.md, android/.
- The user is the gate between phases. Never start the next phase without
  explicit approval. Do not chain skills or agents on your own.
- Never run git commit or git push. The user reviews diffs and commits.
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
  - docs/STATUS.md - current state of the project and the next steps (see
    Workflow, "Session state")
  - docs/DESIGN-DECISIONS.md - decisions log of the design (cited as "Decision N")
  - docs/diagrams/ - architecture diagrams (components.drawio)
  - docs/reviews/ - reports from docs-reviewer. Open findings of the latest
    report must be resolved or consciously deferred by the user before the
    plan phase starts.
- .claude/skills/, .claude/agents/ - workflow tooling
- android/ - the Android project (does not exist yet)

## Scope
- v1 scope is defined in docs/REQUIREMENTS.md. New ideas go to its
  "Ideas for later" section, not into the code.
