# TASKS — Recommendation Carousel Service

Scope is `DESIGN.md` plus four added requirements: a pluggable model seam under the adaptor,
per-ID logging when content is missing, one exception type per layer, and graceful degradation at
the top boundary.

Rules: one task at a time. Task 5 was pulled ahead of Task 4 on request. Each task ships its implementation and its three tests in the same
diff. Test command: `JAVA_HOME=/Users/sipahuja/Library/Java/JavaVirtualMachines/ms-21.0.8/Contents/Home mvn -q test`

---

## - [x] Task 1 — Build skeleton + domain models

`pom.xml` (Java 21 via `maven.compiler.release`, surefire, JUnit 5, Mockito), move `src/Main.java`
into the Maven layout, add `target/` to `.gitignore`. Models: `Show`, `RequestContext` (validating
static factory; generates `requestId` when absent), `RecommendationsResponse`.

Tests — `RequestContextTest`:
1. happy — valid context is built and carries a non-blank `requestId`
2. boundary — blank locale is rejected
3. failure — null `userId` throws `IllegalArgumentException` naming the field

## - [x] Task 2 — Contracts, exception hierarchy, token resolution

Interfaces with javadoc failure contracts: `IRecommendationProvider`, `IContentService`,
`IRecommendationService`, `RecommendationModel`. Exceptions in `error/`: shared base carrying
`requestId`, plus `InvalidTokenException`, `RecommendationProviderException`,
`ContentServiceException`, `RecommendationServiceException`. `TokenResolver` +
`StubTokenResolver` (in-memory token→userId map).

The resolver only decodes token → userId; nothing calls it yet. Putting that userId into
`RequestContext` is the controller's job in Task 6.

Tests — `StubTokenResolverTest`:
1. happy — known token resolves to its userId
2. boundary — null/blank token throws without consulting the map
3. failure — unknown token throws `InvalidTokenException`; message contains the `requestId` and
   does **not** contain the token value

## - [x] Task 3 — Pluggable model + `RecommendationAdaptor`

`StubRecommendationModel` over an in-memory `userId → ordered List<String>` map (unknown user →
empty list; an empty carousel is not an error). `RecommendationAdaptor implements
IRecommendationProvider` — guards input, delegates to the injected `RecommendationModel`, logs
`requestId`/`userId`/`model`/`count`, wraps any model throwable in
`RecommendationProviderException` after an ERROR log.

Tests:
1. happy — adaptor returns the model's IDs in exact order
2. boundary — model returns empty → adaptor returns empty, never null
3. failure — model throws → `RecommendationProviderException` with the original cause attached
4. pluggability — a second stub model injected into the same adaptor changes the result with no
   other code touched

## - [x] Task 4 — `StubContentService` with explicit missing-content logging

In-memory `id → Show` catalog. For **each** requested ID with no content: one WARN line
(`requestId`, `showId`, `reason=content_not_found`), then one summary line with
requested/found/missing counts. Missing IDs are omitted rather than failing the batch. Internal
lookup failures become `ContentServiceException`.

Tests:
1. happy — all IDs hydrate to their shows
2. boundary — empty input returns empty without touching the catalog
3. failure/degradation — mixed known + unknown IDs returns only the known ones, and a captured log
   handler asserts one WARN per missing ID with that ID present in the message

## - [x] Task 5 — `RecommendationService` (core orchestration)

`IRecommendationService` impl; constructor-injected `IRecommendationProvider` + `IContentService`.
Short-circuits to an empty response when the provider returns no IDs (no pointless content call).
Re-sorts hydrated shows back into provider order. One log line per stage. Catches
`RecommendationProviderException` and `ContentServiceException` separately, logs each with its
layer tag, rethrows both as `RecommendationServiceException` with cause preserved.

Tests (Mockito mocks of both ports):
1. happy — provider order preserved even when content returns shuffled
2. boundary — provider returns empty → empty response, content service never called
3. failure — provider throws → `RecommendationServiceException` wrapping the provider cause
4. failure — content throws → same wrapper, distinguishable by cause type

## - [x] Task 6 — `RecommendationController` + demo `main()` + README

`getRecommendations(String token, String locale)`: decode the token via `TokenResolver` to get
the userId, store it in a validated `RequestContext` together with the locale and a fresh
`requestId`, delegate, return `RecommendationsResponse`. This is the only place a userId enters
the system. Only the service call is wrapped:
`RecommendationServiceException` → ERROR log with full stack + requestId → empty response
returned. Client errors (`InvalidTokenException`, `IllegalArgumentException`) propagate. `Main`
wires the stubs and prints one carousel. README covers what it does, how to run it, what's
stubbed, and known limitations.

Tests:
1. happy — valid token + locale returns the populated response
2. boundary — service throws → controller returns an empty response and does **not** throw; ERROR
   log asserted (this is the "endpoint never fails" requirement)
3. failure — invalid token throws `InvalidTokenException`; the recommendation service is never
   invoked

---

## Decisions and assumptions on record

- `DESIGN.md` §5 named `IRecommendationService` twice. Split into `IRecommendationProvider` (the
  adaptor port, returns show IDs) and `IRecommendationService` (the core service, returns the
  hydrated response).
- Graceful degradation covers **dependency** failures only. Invalid token / blank locale still
  fail loudly — a silent empty carousel would hide a client bug.
- Provider ID order is the carousel order; the content service is order-agnostic by contract.
- The controller's catch is the single deliberate exception to CLAUDE.md's "no swallowed
  exceptions" rule, because the carousel must never break the page. Documented in the README.

## Out of scope (not built unless asked)

No caching, no pagination, no fallback carousel content, no HTTP server or JSON layer, no config
flags. Timeouts/retries do not apply while both dependencies are in-process; the README names
where they would attach.
