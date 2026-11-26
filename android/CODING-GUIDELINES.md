# Coding guidelines

The single source of coding rules for every implementer: a skill, an agent or a human.
They say *how* to write code. *What* to build and *where* it goes is in
`docs/DESIGN.md` (components, ports, patterns: section 3 and section 9), the behavior
in `docs/REQUIREMENTS.md` section 7 (`BHV-*`), the tasks in `docs/PLAN.md`. When a
rule here seems to conflict with `docs/DESIGN.md`, the design wins and the conflict is
reported to the user; it is never resolved silently in code.

## 1. Principles

In order of priority when they pull in different directions:

1. **Readability over optimization.** Write the code that is easiest to read, change
   and extend. Optimize only for a measured problem (a slow screen, a slow query), and
   say in the task notes what was measured. No micro-optimizations, no clever tricks.
2. **SOLID.** In practice:
   - one reason to change per class and per function; a class that needs "and" in its
     description is split;
   - extension through new types and new implementations of an interface, not by
     editing `when` chains spread across files (one `when` over a sealed type in one
     place is fine, see section 4);
   - every implementation keeps the contract of its interface (fakes included,
     `docs/DESIGN.md` section 8);
   - small, focused interfaces; the ports are shaped by `docs/DESIGN.md` section 3;
   - dependencies arrive through constructors as interfaces; only App creates concrete
     classes (`docs/DESIGN.md` section 9, dependency injection).
3. **Composition over inheritance.** Share behavior by passing collaborators to the
   constructor, by small functions and by delegation (`by`). No abstract base classes
   for code reuse (`BaseViewModel`, `BaseRepository`). Allowed: implementing an
   interface, sealed hierarchies as sum types (`docs/DESIGN.md` section 9), and
   subclassing that a framework requires (`Activity`, `Application`,
   `BroadcastReceiver`, `ViewModel`, `RoomDatabase`).
4. **Orchestration over choreography.** A use case reads as a sequence of named steps
   in one function, executed one after another:

   ```kotlin
   suspend fun stopTimer(habitId: HabitId): StopResult = lock.withLock {
       val timer = runningTimers.find(habitId) ?: return StopResult.NotRunning
       val entry = entryFromTimer(timer, clock.now())
       dataOperations.stopTimer(habitId, entry)
       timerNotification.remove(habitId)
       StopResult.Stopped(entry)
   }
   ```

   - The coordinating function calls the steps; the steps do not call each other in a
     chain and do not trigger further work through events or callbacks.
   - Events and streams (`Flow`) only where `docs/DESIGN.md` defines them (storage
     reads, the Clock port, the two Domain streams of the Observer pattern). Not as a
     way to connect steps of one action.
   - Flat over nested: early returns and guard clauses instead of nested `if`; at most
     two levels of nesting inside a function; no nested lambdas inside lambdas when a
     named function can take one level.
5. **Names and structure instead of comments.** See section 5.

## 2. Formatting and static checks

- **Formatter and linter:** ktlint through its Gradle plugin (the official Kotlin
  style, the Android Studio code style), plus the default Android Lint of AGP.
  `./gradlew ktlintCheck` must pass. Its version is checked in the official docs
  (T1.1) and it is recorded in `docs/DESIGN.md` section 2 (user decision 2026-11-26).
- Line length 120 (ktlint default for Android Studio style).
- No wildcard imports. No unused code, no commented-out code.

## 3. Structure

- **Modules and components** are fixed by `docs/DESIGN.md` section 3 (Decision 1):
  module `domain` (pure Kotlin/JVM) and module `app`, where the components are
  packages.
- **Packages inside a component:** component first, then feature, e.g.
  `kubisp.habittracker.presentation.day`, `kubisp.habittracker.gui.day`,
  `kubisp.habittracker.persistence.entry`; in `domain` by topic, e.g.
  `...domain.goal`, `...domain.timer`, with the ports in `...domain.port`. Feature
  first (`day.presentation`) is not used: it hides the component boundaries.
- **Visibility.** In `domain`: `internal` by default; `public` only for what other
  components use (Domain operations, pure functions for Presentation, ports, entities).
  In `app`: `private` for everything that does not leave its file.
- **Preferred sizes, not limits.** Around 25 lines for a function body, 300 lines for
  a file and 6 constructor parameters, code is not wrong but is worth a second look:
  does this block do too much at once, is there a step or a collaborator to extract?
  Keep it as it is when splitting would not make it clearer. Never split code only to
  get under a number, and a reviewer does not reject code for its size alone.
- One public top-level class per file, the file named after it. Small private helpers
  and the sealed subtypes of the class may stay in the same file.
- Function granularity: a function works on one level of abstraction. A coordinating
  function calls named steps; the steps do the details. If a block inside a function
  needs a comment to say what it does, it becomes a function with that name.

## 4. Kotlin patterns

- **Immutability:** `val` over `var`; data classes with read-only properties;
  read-only collection types in signatures (`List`, not `MutableList`). Mutation
  stays local and private.
- **Types for meaning:** value classes for identities and units of meaning
  (`HabitId`, `EntryId`) instead of a bare `UUID` or `String`; `java.time` types for
  time; exact decimals by `docs/DESIGN.md` section 5.2, never `Double` or `Float` for
  values, targets and sums.
- **Sealed types** for states and results: UI state, results of Domain operations,
  validation errors. A `when` over a sealed type is exhaustive and has no `else`.
- **Errors:**
  - expected business outcomes (invalid draft, timer not running, damaged import
    file) are values in a sealed result type, not exceptions;
  - exceptions only for programmer errors (`require`, `check`, `error`) and for
    failures of the platform (I/O), which an adapter turns into a result at the port
    boundary where `docs/DESIGN.md` defines one;
  - no empty `catch`; never catch `CancellationException` without rethrowing it.
- **Null:** no `!!`. Nullable types only where absence is a real case of the model.
- **Time:** never `Instant.now()`, `LocalDate.now()`, `ZoneId.systemDefault()` or
  `System.currentTimeMillis()` outside the Clock adapter; everything else gets time
  from the Clock port (`docs/DESIGN.md` section 5.4).
- **Coroutines and Flow:**
  - storage reads are `Flow`, writes `suspend` (`docs/DESIGN.md` section 3);
  - no `GlobalScope`, no `runBlocking` in production code; scopes come from the owner
    (`viewModelScope`, the receiver's `goAsync`, an injected application scope);
  - dispatchers are injected where code switches threads; Domain does not choose
    dispatchers;
  - Domain write operations run under the one Domain lock (`docs/DESIGN.md` section 3).
- **No singletons:** no `object` holding state, no static mutable state. A stateless
  `object` with pure functions or constants is fine.
- **Extension functions** for mapping between layers (`StoredEntry.toDomain()`), kept
  in the package of the layer that owns the source type's mapping (Persistence maps
  its stored classes).
- **Scope functions** (`let`, `apply`, `also`, `run`) only when they make the code
  shorter and still clear; never nested.

## 5. Naming and comments

- Names say intent and domain meaning, in the words of `docs/REQUIREMENTS.md` and
  `docs/DESIGN.md` (habit, entry, goal, period, running timer, Domain operation). One
  term per concept in the whole codebase.
- Functions are verbs (`archiveHabit`, `entriesOverlapping`); booleans read as a
  question (`isArchived`, `hasRunningTimer`); no abbreviations except common ones
  (`id`, `ui`).
- Presentation and GUI per screen: the pure Kotlin Presentation object
  `DayPresenter` with `DayState` and `DayAction`; the thin androidx holder
  `DayViewModel` (`docs/DESIGN.md` section 2); the composable `DayScreen`. The
  `...Presenter` suffix keeps the Presentation object apart from the holder.
- Composables: nouns in `PascalCase`; a function that returns a value is
  `camelCase`.
- String resource keys: from the UI IDs of `docs/UX.md` in `snake_case`, e.g.
  `UI-MSG-IMPORT-DONE` -> `ui_msg_import_done`.
- **Comments are rare.** Prefer a better name, a smaller function, a clearer type or
  signature. A comment is written only for what the code cannot say:
  - *why* a non-obvious rule exists, citing its source (`BHV-TIMER-R2`, `Decision 21`);
  - a workaround of a platform or library behavior, with a link if there is one;
  - the contract of a port method that its signature cannot express (e.g. "returns
    entries whose span overlaps the range").
  No comment that repeats the code, no KDoc on every function, no section banners, no
  change history (git has it). `TODO` only with a task ID from `docs/PLAN.md`.

## 6. Compose (GUI)

- Screen composables are split: `DayScreen(holder)` collects the state and passes
  lambdas; `DayContent(state, onAction)` is stateless and has a `@Preview`.
- State hoisting: composables receive state and emit actions; no business decision
  and no formatting rule beyond resolving text identifiers and locale formatting
  (`docs/DESIGN.md` section 6, section 7).
- `remember` only for UI-local state (scroll, focus, an open menu); everything the user
  can lose on rotation belongs to Presentation.
- Texts from string resources only; no hard-coded user-visible text.

## 7. Tests

- Test-first for every `BHV-*` scenario a task covers: write the failing test from the
  scenario, then the code. To make the test compile, first create the minimal API it
  needs: the classes, interfaces and signatures with bodies of `TODO()`. The test must
  then fail for the right reason (an assertion or the `TODO()`), not by not compiling.
- **Test names:** a backtick sentence starting with the ID it covers, if any:
  ``fun `BHV-TIMER-R2 stop with zero length writes no entry`()``.
- Given / when / then as three blocks separated by a blank line, no comments needed.
- Fakes, not mocks: hand-written in-memory implementations of the ports, shared per
  module in a test fixtures source set; a fake keeps the contract of its port
  (`docs/DESIGN.md` section 8). No mocking library.
- Test data through small builder functions with defaults
  (`aHabit(name = "Run")`), so a test shows only what matters to it.
- Time in tests comes only from the fake clock.
- **Flow tests:** Turbine plus `kotlinx-coroutines-test` (user decision 2026-11-26;
  versions checked in T1.1, recorded in `docs/DESIGN.md` section 2).
- Tests are independent, deterministic and fast; no `Thread.sleep`, virtual time
  instead.

## 8. Dependencies

A new library (also a test or build tool) is a design change: it needs the user's
approval and an entry in `docs/DESIGN.md` section 2. Versions only from the version
catalog, checked in the official docs.

## 9. Git and commits

**Branch model** (user decision 2026-11-26):
- `main` holds only finished milestones, tested by hand by the user.
- A branch per milestone from `main`: `milestone/m1-skeleton`. A branch per task from
  its milestone branch: `task/t1.2-gradle-project`.
- The AI commits freely on task branches.
- A task branch is squash-merged into its milestone branch after the review is
  approved and `./gradlew build` passes, then deleted.
- End of a milestone: the AI builds the APK and a checklist from "Manual test" of
  `docs/PLAN.md`; fixes are further tasks. After the user's acceptance the user
  merges the milestone branch into `main` with `--no-ff` and tags it (`m1`).
- The AI may push `task/*` and `milestone/*` branches to `origin`; never `main`,
  never a force push.
- `.claude/scripts/guard_main.py` (a PreToolUse hook in `.claude/settings.json`)
  blocks the AI's commits, merges, tags and pushes on `main` and every force push.
  It does not affect the user's own git commands.
- Work outside implementation tasks (documents, `.claude/`, CLAUDE.md) is committed
  by the user.

- **Commit messages:** a Conventional Commits type, the task ID and a short
  imperative summary: `<type>: <task ID> - <summary>`, e.g.
  `feat: T1.2 - create the Gradle project`. Types: `feat`, `fix`, `test`, `refactor`,
  `build`, `docs`, `chore`. A commit outside a task has no ID: `docs: <summary>`. The
  squash commit on the milestone branch uses the task title as the summary.
- One task per branch; no unrelated changes ("while I was here") in a task.

## 10. Definition of Done (per task)

A task of `docs/PLAN.md` is done when:
1. The code does what the task and its `Covers` IDs require; every covered `BHV-*`
   scenario that is a Domain or Presentation rule has a test (`docs/DESIGN.md`
   section 8 says at which level).
2. `./gradlew build` passes: compilation, unit tests of both modules, ktlint and
   Android Lint without new warnings. Instrumented tests pass when the task touches
   Persistence.
3. The code follows these guidelines and `docs/DESIGN.md`; deviations are reported to
   the user, not hidden.
4. No new library without approval; no `TODO` without a task ID; no debug code or
   logs left.
5. Manual checks that the task names are done, or listed for the user when they need a
   device the AI cannot use.
6. The task status in `docs/PLAN.md` is updated; documents are changed only with the
   user's approval.
7. The reviewer has approved the diff: the user for the first, interactive tasks
   (T1.1 to T1.3, in the main session), later the `code-reviewer` agent. Once the loop
   runs, the user reviews the whole milestone branch before its merge into `main`, not
   every task.
