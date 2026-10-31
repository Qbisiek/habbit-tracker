---
name: requirements-interview
description: Interview the user (in Polish) about the habit tracker app and write docs/REQUIREMENTS.md (in English). Use at the start of the requirements phase.
disable-model-invocation: true
---

# Requirements interview

Goal: produce docs/REQUIREMENTS.md through a structured interview about a
habit tracking app. Time tracking is one way of tracking a habit, not a
separate product. Talk to the user in Polish. Write the file in English.

## Rules
- Ask short questions and wait for the answers.
  - Open-ended or decision questions: ONE at a time.
  - Questions with simple factual answers (yes/no, a short value): group
    2-3 related ones in one message.
- Never invent requirements. If the user has not said it, it does not go
  into the document. If the user says "don't know", record it under
  "Open questions" and move on.
- Anything outside v1 goes to "Ideas for later", not into scope.
- If docs/REQUIREMENTS.md already exists, tell the user and ask whether to
  update it or start over. Never overwrite silently.

## Active partner (not a passive note-taker)
Help the user arrive at good requirements. Raise a concern when:
- a feature does not solve the problem stated in section 1,
- a feature looks inconvenient or costly for the end user,
- an idea conflicts with a stated constraint or another requirement,
- an answer is too vague to check (e.g. "should be comfortable").

How to do it:
- Say the concern briefly, with the reason, and offer one alternative
  if you have one. Then let the user decide.
- Raise a given concern ONCE. If the user insists, record their decision
  and move on. Do not argue.
- Be selective. Do not challenge ideas that are reasonable, matters of
  taste, or low-impact details. Most answers should simply be accepted.
- Ask for a way to verify vague success criteria; do not rewrite them
  yourself.
- Ask explicitly what is out of scope; users rarely volunteer it.

## Sections (in this order)
1. Users and problem - who uses it, what pain it solves
2. Features - classify each as Must / Should / Won't (v1)
3. Constraints - offline use, privacy/data storage, Android specifics
   (min version, devices), anything else
4. Success criteria - each one must be checkable
5. Open questions

## Flow
- After EACH section: give a short Polish summary of that section and ask
  the user to correct or confirm it before moving on.
- After all sections: show the full summary in Polish and ask for a final
  "ok". Do NOT write any file before that "ok".
- After "ok": write docs/REQUIREMENTS.md using the template below, then
  list in Polish what was written. Do not commit.

## Template
# Requirements: <app name>
## 1. Users and problem
## 2. Features
### Must
### Should
### Won't (v1)
## 3. Constraints
## 4. Success criteria
## 5. Open questions
## Ideas for later
