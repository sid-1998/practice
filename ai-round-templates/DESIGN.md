# DESIGN — <problem name>

> Fill this in yourself in ~8 minutes. Terse bullets, not prose. This is a thinking
> tool and a prompt, not a spec. Then feed it to plan mode.

## 1. Problem, restated
<Two lines, in my own words. Who has what pain, and what does "solved" look like.>

## 2. Assumptions
> Say these out loud to the interviewer and ask them to correct any.
- Scale: ~<N> RPS, ~<N> records, <N> concurrent users
- Consistency: <strong / eventual OK for X because Y>
- Latency target: <p99 < Nms> on the critical path
- Single region / single instance for now
- Auth/identity is assumed handled upstream; requests arrive with a trusted user ID
- <domain-specific assumption>

## 3. Scope
**In (must work end to end, demoable):**
- <capability 1>
- <capability 2>

**Deliberately out (would sequence next, in this order):**
1. <thing> — why deferred:
2. <thing> — why deferred:
3. <thing> — why deferred:

## 4. Approach options
| Option | How it works | Pros | Cons | Verdict |
|---|---|---|---|---|
| A | | | | **Chosen** |
| B | | | | Rejected because |
| C | | | | Rejected because |

**Why A for a 90-minute slice:** <reason>
**The seam:** if we later need B, the change is isolated to <component/interface>, because <reason>.

## 5. Interfaces (the contract — write this before any code)

### API
```
POST /<resource>
  req:  { ... }
  res:  201 { id, ... }
  errs: 400 invalid input | 409 duplicate | 503 dependency unavailable
```

### Core types
```
<Type> { field: type, ... }
```

### Key internal boundary
```
interface <Port> {
  <method>(input) -> output   // throws <error> when ...
}
```

## 6. Data model / storage
- Store: <Postgres / in-memory / Redis> — why:
- Shape: <tables/keys>
- Indexed on: <field> because <query pattern>
- Write path: <sync / async via queue>

## 7. Failure handling
| What fails | Detection | Behavior | Blast radius |
|---|---|---|---|
| Downstream timeout | <Nms timeout> | <retry N w/ backoff+jitter, then fail closed/open> | |
| Duplicate request | idempotency key on <field> | return original result, no double-write | |
| Store unavailable | | | |
| Bad input | validate at boundary | 400 with field-level detail | caller only |

## 8. Validation plan
- Happy path test: <what>
- Boundary tests: <what>
- Failure tests: <what>
- Manual demo: `curl ...` → expect <result>  ← this is the thing I show at the end

## 9. Observability
- Logs: structured, correlation ID threaded from ingress
- Metrics:
  - `<name>` — request latency p50/p99 by endpoint
  - `<name>` — error rate by error class
  - `<name>` — <domain metric, e.g. queue depth / retry count>
- **The one alert I'd page on:** <condition> — because it means <user-visible impact>
- Trace: <span boundaries I'd add>

## 10. Scale story (for the discussion, not for building today)
- First bottleneck at 10×: <what> — fix: <shard / cache / queue / read replica>
- Second: <what> — fix:
- Known single point of failure I'm accepting today: <what> — acceptable because <reason>

## 11. Known debt taken on knowingly
- <shortcut> — cost: <what it makes harder later>
- <shortcut> — cost:
