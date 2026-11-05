# Design: Habit Tracker (working title)

> **STATUS: INTERRUPTED. THIS IS A WORK IN PROGRESS, NOT AN APPROVED DESIGN.**
>
> The `/design-interview` session was stopped three times: in the middle of
> **Level 1**, at the end of Level 1, and now **early in Level 2** (after the
> first Level 2 decision). The user has NOT given the final "ok".
>
> **How to resume:** run `/design-interview`. When it finds this file, choose
> "update" (not "start over"), keep the decisions below as given (do not
> re-ask them), and continue from "Next steps" at the end of this block.
> Skill: `.claude/skills/design-interview/SKILL.md`.
>
> **Agreed so far**
> - The list of design topics (confirmed by the user, see section 1).
> - Decisions 1-11 in the decisions log (section 9), including the GUI
>   toolkit (Jetpack Compose), the presentation pattern (MVVM with
>   unidirectional state, pure Kotlin), the `sealed` Measurement and the
>   week start (Monday, as a parameter).
> - **Level 1 is confirmed by the user**: components, dependency direction
>   and key entities (sections 3 and 4).
> - Patterns Ports and Adapters and Repository were announced to the user
>   and recorded in the log; the user did not object.
> - Diagram `docs/diagrams/components.drawio` is **confirmed by the user**
>   (App on the left with straight arrows to all components, including GUI
>   and Presentation; the user adjusted the layout in draw.io). Open
>   question 9 is resolved.
>
> **Not decided yet**
> - Level 2, except the week start: remaining period boundary rules (day
>   boundary, time zone), goal evaluation per measurement type and
>   direction, completion percentage, statistics, timer behavior, UI
>   structure, cross-cutting concerns, entity attributes and invariants.
> - Level 3 (remaining technology choices), testing strategy, and the
>   remaining open questions in section 10.
>
> **Next steps**
> 1. Continue Level 2 with the day boundary and time zone (open question 2),
>    then goal evaluation and the completion percentage (section 5), then
>    the other open questions (1, 3-8) at the point where they affect the
>    design, UI structure, cross-cutting concerns.
> 2. Level 3, testing strategy, then the full summary in Polish with the
>    coverage check, and the final "ok". Then remove this status block.

## 1. Overview

Source: `docs/REQUIREMENTS.md`. Design topics confirmed by the user (derived
from Must/Should features and constraints):

1. Habit model: measurement type and direction.
2. Entries: any day, editing past days.
3. Timer: survives closing the app; ongoing notification with Stop (Should).
4. Goals and periods: fixed day/week/month, evaluation per type and
   direction, goal completion percentage.
5. Statistics over a user-chosen date range.
6. Habit management: create, edit, archive, delete.
7. Units: built-in and custom label (Should).
8. Screens and navigation: today screen, day switching, calendar picker,
   habit management, statistics.
9. Export / import to a file (Should).
10. Local, offline-only storage.
11. Localization: Polish and English, texts in resources.
12. Platform: Android only, minimum version.
13. No analytics or crash reporting (constrains library choices).

Always part of the design: architecture with a component diagram, and the
testing strategy (including verification of the success criteria).

## 2. Tech stack

Decided:
- Android, Kotlin (CLAUDE.md and requirements).
- GUI toolkit: Jetpack Compose (Decision 8).
- A thin `ViewModel` from androidx (lifecycle library) in the GUI component,
  only to keep a Presentation object alive across configuration changes
  such as rotation (Decision 9). The exact artifact must be checked in the
  official docs before it is named in the plan.

TBD:
- Minimum Android version (open question inherited from requirements).
- Remaining library choices (Level 3).

## 3. Architecture

Level 1, confirmed by the user. Diagram:
`docs/diagrams/components.drawio` (confirmed by the user). It shows App on
the left with dashed arrows (creates / wires) to every component, and solid
arrows for code dependencies.

**Module structure (Decision 1):** one Gradle module for Domain (pure
Kotlin/JVM, no Android) and one `app` module for everything else, separated
by packages.

**Components:**

| Component | Responsibility |
|---|---|
| Domain | Entities and business rules: periods, goal evaluation, statistics, timer logic (pure state based on a start instant), export/import rules. Defines the interfaces (ports) for storage, clock, etc. No framework dependencies. |
| Persistence | Implements the storage ports defined by Domain. Has its own stored representation and maps it to domain entities. |
| Platform services | Adapters for system features: background timer with notification, system clock, file access for export/import, string resources. |
| Presentation | Screen state (what to show, already in displayable form) and handling of user actions on a screen (calls Domain, updates state, decides navigation). Pure Kotlin, no Android and no widgets. No business rules. Depends on Domain. |
| GUI | Compose widgets and navigation; renders state from Presentation and forwards user actions to it, without making decisions. Contains the thin androidx `ViewModel` holders. Replaceable without changes to Domain and Presentation. |
| App (composition root) | Wires everything: creates the port implementations and connects them to Domain and Presentation. |

**Dependency direction:** `GUI -> Presentation -> Domain <- Persistence`, and
`Domain <- Platform services`. Domain depends on nothing. App knows all
components; no component knows App.

**How the design principles are met:**
1. Business logic free of frameworks: Domain is a separate pure Kotlin/JVM
   module; storage and platform features are reached through ports defined
   by Domain and implemented in Persistence and Platform services.
2. Business logic separated from the GUI: GUI depends on Presentation only
   (state in, actions out); Presentation and Domain know nothing about
   Compose. Replacing the GUI does not touch them.
3. Patterns: see section 9 (MVVM with unidirectional state, Ports and
   Adapters, Repository, sealed hierarchy).

**Note on iOS:** pure Kotlin Presentation and Domain make a later move
easier, but sharing them with iOS would need Kotlin Multiplatform and a
change of the Domain module type. Not designed for in v1 (out of scope);
the choices only do not rule it out.

## 4. Data model

Level 1 (confirmed): names and relations only; attributes and invariants are Level 2.

- **Habit**: a pure definition of the activity. Identity, name, direction
  (Build / Quit), status (active / archived). Direction is the habit's
  intent and exists even without a goal. It is not part of the goal.
- **Measurement**: how the habit is measured. A closed (`sealed`) set of
  variants: Binary, Numeric (with a unit), Duration. Exactly one per habit.
  The unit belongs to the Numeric variant, not to the Habit. A unit is
  data, not a type: either a built-in one (identifier, name from string
  resources) or a custom one (text label). Details are Level 2.
- **Goal**: optional (a habit may exist without any goal). Target value and
  period (day / week / month). Separate from Habit, so changing a goal does
  not change the habit definition. The comparison ("at least" / "at most")
  is derived from the habit's direction. The period is a separate attribute
  so a goal without a period can be added later (see Decision 7).
- **Entry**: a recorded value for a habit at a moment in time (date and
  time, not only a day). Its meaning comes from the habit's Measurement.
- **RunningTimer**: a habit plus the instant the timer was started.

Relations: Habit 1 - 1 Measurement; Habit 1 - 0..n Goal (whether a habit can
have a history of goals is a Level 2 question); Habit 1 - 0..n Entry;
Habit 1 - 0..1 RunningTimer (whether several timers may run at once is open).

Proposed, not yet confirmed: the Measurement of a habit cannot change once
the habit has entries.

## 5. Behavior and rules

Decided so far (Level 2):
- **Week start:** Monday. The first day of the week is a parameter of the
  period calculation, not a constant hard-coded in the rules, so a user
  setting can be added later without changing the rules (Decision 11).
  Day and month boundaries need no decision.

TBD (Level 2). Topics: day boundary and time zone, goal evaluation per
measurement type and direction (including binary "days with occurrence"),
goal completion percentage, statistics, timer behavior across app restarts.

## 6. User interface structure

TBD (Level 2). Topics: today screen, day switching, calendar picker, habit
management, statistics, export/import.

## 7. Cross-cutting concerns

TBD (Level 2): localization (Polish and English), local storage and
backup, timer in the background.

## 8. Testing strategy

TBD. Domain logic is expected to be testable on the JVM thanks to the
separate Domain module; Presentation is pure Kotlin and should be testable
on the JVM as well. Verification of the success criteria in
`docs/REQUIREMENTS.md` section 4 is to be described here.

## 9. Decisions log

| # | Decision | Options considered | Reason |
|---|---|---|---|
| 1 | Separate Gradle module for Domain (pure Kotlin/JVM) + one `app` module; the rest separated by packages. | (a) separate Domain module + `app`; (b) single module, packages only; (c) several modules (domain / data / presentation / ui). | The compiler enforces that Domain cannot use Android (design principle 1), tests run fast on the JVM. (b) enforces nothing. (c) is more configuration than this app justifies. |
| 2 | A goal is optional. | (a) optional; (b) mandatory. | Tracking without goals must be possible; requirements do not say goals are mandatory. |
| 3 | Goal is separate from Habit. | Goal inside Habit. | Habit is a pure definition; changing a goal must not change the habit. Allows a goal history. (User decision.) |
| 4 | Measurement (type and unit) is separate from Habit. | Type and unit inside Habit. | Habit is a pure definition; the unit describes how a habit is measured. (User decision.) |
| 5 | Direction (Build / Quit) lives in Habit. | (a) in Goal; (b) in Habit; (c) in Measurement. | Direction is the intent of the habit and exists without a goal (e.g. tracking time one wants to reduce). Matches the wording of the requirements. The goal comparison is derived from it. (User decision; the initial recommendation was (a).) |
| 6 | Entry stores a moment in time, not only a day. | Day only. | Timer sessions and manual entries have a time; the day is derived from it. (User decision.) |
| 7 | A goal without a period (cumulative, e.g. "1000 books") is out of v1. Added to "Ideas for later" in `docs/REQUIREMENTS.md`. The period stays a separate attribute of the goal. | (a) out of v1; (b) in v1 (needs new rules and a change of the requirements); (c) leave open. | Not needed by the v1 success criteria; (b) adds rules with no backing in the requirements. (a) keeps the variant possible later. (User decision.) |
| 8 | GUI toolkit: Jetpack Compose. | (a) Jetpack Compose; (b) Android Views (XML) with ViewBinding; (c) Kotlin Multiplatform with Compose Multiplatform. | Mainstream, current standard for new Android apps, the "state down, events up" model fits the Presentation/GUI split. (b) means more boilerplate and imperative view updates. (c) designs for iOS, which is out of v1, and adds complexity. (User decision.) |
| 9 | Presentation pattern: **MVVM with unidirectional state flow**. Presentation classes are pure Kotlin (expose a `StateFlow` of screen state and action methods); a thin androidx `ViewModel` in the GUI component only keeps them alive across rotation. | (a) MVVM with unidirectional state; (b) MVI (single state, intents, reducer); (c) MVP (presenter drives the view through an interface). Variant: use androidx `ViewModel` directly as the Presentation class. | (a) gives the state/action split that principles 1 and 2 need with little ceremony and fits Compose. (b) adds ceremony not justified by simple screens. (c) is imperative and fits Views, not Compose. The direct `ViewModel` variant was rejected: pure Kotlin keeps Presentation independent of Android and testable on the JVM, at the cost of one small holder class per screen. (User decision.) |
| 10 | Measurement is a **closed `sealed` hierarchy** (Binary, Numeric with unit, Duration). A unit is data inside Numeric (built-in or custom label), not a new variant. | (a) sealed hierarchy (sum type); (b) enum plus optional fields; (c) open class hierarchy with polymorphism (Strategy pattern). | The set of types is known and closed in v1 (scale and checklist are Won't), so compiler-checked exhaustive handling is worth more than openness. (b) allows invalid states (Binary with a unit, Numeric without one). (c) would mix rules with presentation and adds flexibility v1 does not need. (User decision.) |
| 11 | The week starts on Monday. The first day of the week is a parameter of period calculation. | (a) always Monday; (b) from the device settings (needs another port, result depends on the phone); (c) user setting in the app (needs a settings screen, not in the v1 requirements). | v1 is for the author and friends in Poland; (b) and (c) add a port or a screen with no backing in the requirements. Making it a parameter keeps (b) and (c) possible later. (User decision.) |

**Patterns used** (design principle 3):

| Pattern | Where | Reason | Why not the alternatives |
|---|---|---|---|
| MVVM with unidirectional state flow | Presentation / GUI | See Decision 9. | MVI, MVP: see Decision 9. |
| Sealed hierarchy (sum type) | `Measurement` | See Decision 10. | Enum plus optional fields, Strategy: see Decision 10. |
| Ports and Adapters (dependency inversion) | Domain defines ports; Persistence and Platform services implement them | Required by principle 1: dependencies must point to Domain. | Domain calling Room or Android directly breaks principle 1. (Announced to the user, no objection.) |
| Repository | Shape of the storage ports | Domain sees a collection of entities, not a database. | Ports shaped like SQL queries leak persistence details into Domain. (Announced to the user, no objection.) |

## 10. Open questions

1. One entry or many per day (timer sessions suggest many).
2. Which day an entry belongs to near midnight; time zone handling; which
   time a manual entry for a past day gets.
3. Goal history: are past periods evaluated with the goal valid at that
   time? What happens to existing results when a goal changes?
4. What deleting or archiving a habit means for its entries and statistics.
5. Is direction immutable? Changing it flips the meaning of an existing goal.
6. Can several timers run at the same time?
7. Timer logic must stay independent of Android (design principle 1): how
   the background mechanism and the Domain logic are separated.
8. Localization path: Presentation must not depend on Android string
   resources. How do translated texts reach the GUI (e.g. Presentation
   exposes structured values or resource-independent identifiers and the GUI
   resolves them)?
9. (Resolved: the diagram shows App wiring to GUI and Presentation.)
10. Minimum Android version (inherited from requirements).
11. List of built-in units (inherited from requirements).
12. App name (inherited from requirements).
