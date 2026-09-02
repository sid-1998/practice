# Recommendation carousel

A backend for a personalised "recommended shows" carousel. One entry point takes a caller's token
and locale, asks a recommendation model for a ranking of show ids, hydrates those ids into
renderable tiles, and returns the carousel in the model's order.

Designed in [DESIGN.md](DESIGN.md); built task by task per [TASKS.md](TASKS.md).

## How to run

Java 21 is required. The CLI default `java` on this machine is 11, so point `JAVA_HOME` at 21:

```sh
export JAVA_HOME=/Users/sipahuja/Library/Java/JavaVirtualMachines/ms-21.0.8/Contents/Home
```

Tests — the single command that proves the behaviour:

```sh
mvn test
```

The demo — wires the stubs and walks five scenarios end to end:

```sh
mvn -q compile && java -cp target/classes com.practice.recommendation.Main
```

It prints a carousel per scenario plus the structured logs behind it: a normal ranking with one
unlisted show, a user with no recommendations, an unknown user, a forged token, and a model that
is down. Every log line for one request shares its `requestId`.

## How a request flows

```
RecommendationController      validates locale, decodes token -> userId, builds RequestContext
        |                     the only place a userId enters the system
        v
RecommendationService         short-circuits an empty ranking, hydrates, imposes model order
        |         \
        |          \
        v           v
RecommendationAdaptor        IContentService
        |                    (StubContentService)
        v
RecommendationModel
(StubRecommendationModel)
```

`RecommendationAdaptor` is the seam that makes models pluggable. It owns everything
model-independent — input guarding, logging which model served the request, translating model
failures — so a new model is one class implementing `RecommendationModel` plus one wiring line in
`Main`. Neither the core service nor the controller changes.

The model's order **is** the personalisation and is never recomputed or re-sorted. `IContentService`
returns a `Collection`, not a `List`, so nothing can accidentally depend on the content layer's
order instead.

## Error handling

One exception type per layer, each carrying the `requestId`, all rooted at `CarouselException`:

| Layer | Type | Behaviour |
|---|---|---|
| api, token | `InvalidTokenException` | client error — propagates |
| api, input | `IllegalArgumentException` | client error — propagates |
| provider | `RecommendationProviderException` | wraps the model failure |
| content | `ContentServiceException` | lookup failed |
| core | `RecommendationServiceException` | wraps the two above; `getCause()` says which |

Each layer logs one ERROR line tagged with its own `layer=` or `reason=` and rethrows as its own
type with the cause attached. Exactly one place converts a failure into a result:
`RecommendationController` catches `RecommendationServiceException`, logs the full chain, and
returns an empty carousel. An empty shelf costs one row; a thrown error breaks the page.

Client errors are the deliberate exception — a forged token or a missing locale still throws,
because answering them with an empty carousel would hide the caller's bug.

Degradation that is *not* an error: an id with no content is dropped after its own WARN naming
that id, so one gap in the catalog costs one tile. A rising `missing` count in the
`event=content_fetched` summary means the model and the catalog have drifted apart.

## What is stubbed

| Stub | Stands in for | Swap by |
|---|---|---|
| `StubTokenResolver` | real token/signature verification | implementing `TokenResolver` |
| `StubRecommendationModel` | the recommendation service or ML model | implementing `RecommendationModel` |
| `StubContentService` | the content service | implementing `IContentService` |

All three are in-process maps. No network calls are made anywhere.

## Known limitations

- **No HTTP layer and no JSON.** `RecommendationController` is a plain class called from tests and
  `Main`. `DESIGN.md` specifies `GET /recommendations?locale=<locale>` and snake_case JSON; mapping
  `RecommendationsResponse` onto that wire format is not built.
- **No timeouts or retries.** Both dependencies are in-process, so there is nothing to time out.
  When they become network hops, the timeout and retry decision attaches to the `model.recommend`
  call in `RecommendationAdaptor` and to `fetchContent` in the content implementation.
- **`StubContentService` never throws `ContentServiceException`.** A map lookup cannot fail, so the
  catch would be unreachable. The type is part of the `IContentService` contract for a real
  implementation, and `RecommendationService` already handles it.
- **`RecommendationService` folds a null ranking in with an empty one.** Unreachable through the
  real wiring, since `RecommendationAdaptor` enforces non-null; it buys a degraded carousel instead
  of an NPE if a future provider breaks the contract.
- **Duplicate ids are handled silently.** Repeats in a ranking are deduped when the carousel is
  assembled, and nothing logs it.
- **No caching, pagination, or fallback carousel content.** A user with no recommendations gets an
  empty carousel, not a popular-shows fallback.
- **Log values must be single tokens.** `LogLine` emits `key=value` pairs separated by spaces, so a
  value containing a space would break field parsing. Only ids, counts and the `LogEvents`
  constants are passed today.
