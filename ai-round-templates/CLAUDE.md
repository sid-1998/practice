# Working agreement

## Role split
- I own the architecture, the interfaces, and the scope. You implement.
- Never introduce a new abstraction, layer, or design pattern that isn't in DESIGN.md.
  If you think one is needed, say so and wait — don't add it.
- Never add a dependency without asking first, with a one-line justification.

## Task discipline
- Work on ONE task from TASKS.md at a time. Stop when it's done. Do not start the next one.
- Keep the diff small enough to review in under two minutes. If a task can't be done
  that small, tell me and propose a split instead of writing it.
- Don't touch files outside the task's scope. No opportunistic refactors, no reformatting,
  no renaming things you happen to walk past.
- After each task: list what changed and what I should look at most carefully.

## Every task ships with tests
No task is complete without, in the same diff:
1. the happy path
2. one boundary/edge case (empty, zero, max, missing field, duplicate)
3. one failure path (dependency down, timeout, invalid input, conflict)

Tests must be runnable with the project's test command and must actually fail if the
behavior breaks. No tests that assert mocks were called and nothing else.

## Code style
- Write the interface/type/contract first, implementation second.
- No speculative generality: no config flags, hooks, or extension points for
  requirements that don't exist yet.
- Errors: fail loudly with context. No swallowed exceptions, no bare catch-all that
  returns a default. Validate input at the boundary, trust it inside.
- Comments only where the *why* isn't obvious from the code. No narration comments.
- Match the surrounding code's naming and idiom.

## Observability (apply as you go, not as a cleanup pass)
- Structured logs, one line per meaningful event, with a request/correlation ID
  threaded through the call path.
- Never log secrets, tokens, PII, or full request bodies.
- Every outbound call gets: an explicit timeout, and a clear decision on retry
  (with backoff + jitter if yes). State the choice in a comment if it's non-obvious.

## How to answer me
- Be direct. No preamble, no restating my question, no summary of what you're about to do.
- If my instruction is ambiguous or looks wrong, say so in one sentence before coding.
- If you're unsure whether something is in scope, ask — don't guess wide.
- Flag anything you implemented that you're not confident about.

## Definition of done for the whole exercise
- One request path works end to end and can be demoed live.
- Tests pass via a single command.
- README says what it does, how to run it, what's stubbed, and known limitations.
