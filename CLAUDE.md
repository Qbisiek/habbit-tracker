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
- Interrupted sessions: when the user says they must stop or end the current
  session (interview or any other multi-step work), by default save the
  state to the phase's artifact file without being asked. Mark at the top of
  the file, clearly, that the session was interrupted and the document is
  not approved. List what was agreed (decisions, confirmed items), what is
  still undecided, and the next steps to resume. Do not present unconfirmed
  items as decided. Tell the user in Polish what was saved.

## Language
- Conversation with the user: Polish.
- Everything in the repo (code, comments, docs, skills, agents): English.

## Working style
- Short answers. Be direct and critique constructively. No compliments.
- If unsure about a tool's current format or API, check the docs or say
  you don't know. Do not guess.

## Layout
- docs/ - requirements, design, plan
- .claude/skills/, .claude/agents/ - workflow tooling
- android/ - the Android project (does not exist yet)

## Scope
- v1 scope is defined in docs/REQUIREMENTS.md. New ideas go to its
  "Ideas for later" section, not into the code.
