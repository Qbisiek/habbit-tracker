---
name: design-interview
description: Walk the user (in Polish) through the technical design of the app and write docs/DESIGN.md (in English). Use at the start of the design phase, after docs/REQUIREMENTS.md is approved.
disable-model-invocation: true
---

# Design interview

Goal: produce docs/DESIGN.md through a structured conversation about HOW to
build what docs/REQUIREMENTS.md describes. Talk to the user in Polish. Write
the file in English. This phase makes design decisions; it does not write
code and does not produce the implementation plan.

## Start
- Read docs/REQUIREMENTS.md and CLAUDE.md. If REQUIREMENTS.md does not
  exist, tell the user and stop.
- If docs/DESIGN.md already exists, tell the user and ask whether to update
  it or start over. Never overwrite silently.
- Derive the list of topics that need a design decision from the
  requirements (every Must/Should feature and every constraint that forces
  a technical choice). Show the list in Polish and ask the user to correct
  or confirm it before starting. Do not assume topics that the
  requirements do not imply.

## Rules
- Ask short questions and wait for the answers.
  - Design decisions: ONE at a time.
  - Simple factual questions: group 2-3 in one message.
- Every design element must trace back to a requirement or to a design
  principle. If it does not, do not add it. Items outside the current
  scope stay out; at most note that a choice keeps a deferred idea
  possible, without designing for it.
- Never invent requirements. A missing or ambiguous requirement is not a
  license to decide silently: ask, or record it under "Open questions".
  Open questions inherited from the requirements should be resolved at the
  point where they affect the design, or carried over.
- If unsure about a library, API or version, check the official docs or say
  you don't know. Do not guess.
- Prefer boring, mainstream, well-documented choices. This is a learning
  project; avoid over-engineering (no layers, modules or abstractions
  without a concrete reason from the requirements or the design
  principles).

## Design principles (fixed, apply to every decision)
These come from the user and are not up for negotiation. Do not re-ask
whether to follow them; ask only how to apply them.
1. Business logic is separated from framework-specific code (platform
   APIs, persistence libraries, messaging/event infrastructure, etc.).
   Business logic must not depend on any framework; frameworks depend on
   it, through interfaces it defines where needed. This includes
   persistence: the logic depends only on storage interfaces it defines,
   and the stored representation is kept separate from the domain
   entities (mapping between them is part of the persistence layer).
2. Business logic is separated from the GUI. It must be possible to
   replace the GUI (toolkit or whole UI layer) without changing the
   business logic.
3. Every design pattern that is used is named explicitly, with the reason
   it was chosen and why the main alternatives were not. Record it in the
   decisions log. Do not use a pattern by implication or by habit.

How to apply them without over-engineering: the principles are about the
direction of dependencies, not about adding layers. Introduce an interface
or a layer only where a dependency would otherwise point the wrong way.
In the Architecture section, show concretely how each principle is met
(what depends on what), and keep logic that is pure enough to be tested
without the framework or the GUI.

## Top-down approach
Design from the general to the specific. Do not finish one section in
depth before the whole picture exists.
- First settle the main components, their responsibilities and the
  dependencies between them. Only then refine each component, and only as
  far as the requirements and the next phase need.
- Refining may show that a higher-level decision was wrong. Then go back,
  revise it, say so explicitly to the user, and record the revision in the
  decisions log. Do not patch around a flawed high-level design.
- Stay at design level. Do not specify implementation details: no class
  hierarchies, method signatures, full schemas (column types, indexes,
  nullability, technical fields) or file layouts. Name a concrete class or
  type only if it is a key element of a main component.
- Key domain entities ARE part of the design. For each, define its
  identity, its relations to other entities, the distinctions between its
  variants, the attributes that carry business meaning (those the rules or
  requirements depend on) and its invariants. Everything beyond that
  belongs to the plan and implementation phases.
- If a detail does not change the structure, the interfaces between
  components or a requirement's feasibility, leave it out.

## Diagrams
Diagrams are a key part of the design, not decoration.
- At least one diagram is required: the main components and the
  dependencies between them. Arrows show the direction of dependency, so
  the design principles can be checked visually. Add further diagrams
  (e.g. entity relations, a key flow) only where they clarify something
  the text does not.
- Format: editable vector diagrams as draw.io files (.drawio), stored in
  docs/diagrams/ and referenced from docs/DESIGN.md. Do not use text-based
  diagram languages (e.g. Mermaid) and do not draw with ASCII.
- If you are unsure about the current .drawio file format, check the
  official draw.io documentation or say you don't know. Do not guess. If
  a draw.io tool is available in the session, you may use it; the result
  must still be a .drawio file in docs/diagrams/.
- You usually cannot see the rendered result. Keep diagrams simple (boxes,
  labeled arrows, a clear layout) and ask the user to open the file in
  draw.io, fix the layout if needed, and confirm that the diagram matches
  the design. Do not treat a diagram as confirmed before that.
- Diagram and text must agree. When a decision is revised, update the
  affected diagrams together with the text.
- Follow the CLAUDE.md rule: before writing a diagram file, briefly say
  what it shows.

## Active partner (not a passive note-taker)
For each real decision:
- Present 2-3 options with short trade-offs, relevant to THIS project.
- Give one recommendation with the reason. Explain briefly why, since the
  user is also learning.
- The user decides. Raise a given concern ONCE. If the user insists, record
  the decision and move on.
- Do not turn trivial or taste-level choices into discussions; propose a
  default and move on.
- Flag when a requirement is hard or costly to implement, conflicts with
  another requirement, or is too vague to design against. Offer an
  alternative, but changing a requirement needs the user's explicit
  approval and an update to docs/REQUIREMENTS.md (never edit it on your own).

## Sections
Use this generic outline. Add the confirmed topics that do not fit any
section as subsections of "Cross-cutting concerns", and drop sections that
do not apply.
1. Overview - the requirements and constraints that shape the design
2. Tech stack
3. Architecture - includes the component diagram
4. Data model
5. Behavior and rules - non-trivial logic implied by the requirements
6. User interface structure - screens and navigation
7. Cross-cutting concerns
8. Testing strategy - what is tested and how; how the success criteria in
   the requirements will be verified. Detailed test cases belong to the
   plan.
9. Decisions log - per significant decision (including every design
   pattern used and every revision of an earlier decision): decision,
   options considered, reason. Filled throughout the conversation.
10. Open questions

## Flow
The Sections above are the layout of the document, not the order of the
conversation. Walk through the design in levels:
1. Level 1 - overview: start with a draft of the whole structure proposed
   by you (do not ask about details first), then discuss the significant
   decisions in it one at a time. It covers: main components, their
   responsibilities, the dependencies between them (how the design
   principles are met), the key
   domain entities and the relations between them (names and relations
   only), and only the technology choices that shape this structure (e.g.
   platform, language, UI toolkit). Level 1 also produces the component
   diagram (see "Diagrams"); its confirmation by the user is part of
   confirming this level.
2. Level 2 - per component: its boundaries/interfaces, non-trivial rules,
   UI structure and the confirmed cross-cutting topics; for each key
   entity its business-relevant attributes and invariants.
3. Level 3 - only where needed: remaining technology choices (libraries,
   formats) and anything else that must be decided before planning.
4. Then testing strategy and open questions. The decisions log is filled
   throughout.
- After EACH level: give a short Polish summary of that level and ask the
  user to correct or confirm it before going deeper. Going back to a
  higher level is allowed at any time (see "Top-down approach").
- After all levels: show the full summary in Polish, including a short
  check that every Must/Should requirement is covered by the design, that
  the design principles are met and that diagrams and text agree, and ask
  for a final "ok". Do NOT write docs/DESIGN.md before that "ok". The only
  files allowed earlier are the working versions of diagrams in
  docs/diagrams/, which the user needs to review during the conversation.
- After "ok": explain briefly what the file contains, write docs/DESIGN.md
  using the template below, then list in Polish what was written. Do not
  commit. Do not start the plan phase; the user decides when to move on.

## Template
# Design: <app name>
## 1. Overview
## 2. Tech stack
## 3. Architecture
## 4. Data model
## 5. Behavior and rules
## 6. User interface structure
## 7. Cross-cutting concerns
## 8. Testing strategy
## 9. Decisions log
## 10. Open questions
