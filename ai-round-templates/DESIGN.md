# DESIGN — <problem name>


## 1. Problem, restated

## 2. Assumptions


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


## 11. Known debt taken on knowingly
- <shortcut> — cost: <what it makes harder later>
- <shortcut> — cost:
