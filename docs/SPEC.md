# Search slice · acceptance contract

Original implementation plan. Keep the scope to two screens and a public bibliographic service. No accounts, analytics, disk persistence or backend credentials.

| Behavior | Acceptance evidence |
| --- | --- |
| A nonblank query searches Open Library and renders up to 20 matching works. | HTTP contract test verifies query/fields/limit; Compose journey renders list. Live API smoke is recorded separately. |
| An edit cancels the old request immediately, before debounce. | ViewModel virtual-time test observes cancellation before the next request starts. Retrofit test inspects cancellation on the actual OkHttp Call. |
| Old results never replace the current query. | Late-response ViewModel test uses a deliberately non-cooperative repository; clearing input also cancels and removes items. |
| A valid canonical-query cache hit avoids network. | Cache test counts source invocations at 179,999 ms; independent query and empty-result cases. |
| At 180,000 ms, refresh from network and restart TTL at receipt. | Exact-boundary and slow-network cache tests. Failure never serves the expired entry. |
| Memory is bounded and cancellation/failure cannot seed the cache. | LRU eviction, failed refresh, cancelled and non-cooperative source tests. |
| A selected work opens a detail with title, author, first publication, editions and ID. | Compose detail/back journey; missing values labelled explicitly. |
| Error and empty states recover. | Typed domain errors; real HTTP 429/503, malformed/missing/204 bodies and uncached recovery tests; presentation mapping/retry; native error/retry/clear/empty journeys. |
| The UI remains lifecycle-owned and restorable within Android task state. | ViewModel factory, Navigation back stack and SavedStateHandle reconstruction and real MainActivity recreation on detail, including restored Navigation/snapshot. OS process termination is not covered by activity recreation. |
| Reviewers can build and test without a private account. | JDK/SDK instructions, checked wrapper SHA, independent export build, CI workflow. |

Domain tests use in-code repository doubles. Data HTTP tests use MockWebServer; no external API is involved in deterministic CI. Presentation uses virtual time. Production-entry instrumentation substitutes only HTTP responses, preserving the real launcher, application graph, Retrofit, cache and ViewModel factory; it asserts query/headers, distinct error messages, activity recreation, detail/back and a cache hit with no extra request. Native screenshots use an original fictional book fixture, not a fabricated live service result.

Known limits: single UI consumer (no shared in-flight request coalescing), no pagination, memory cache lost on process death, no physical-device/TalkBack/store or performance claims. See VERIFICATION for actually executed checks.

## Visual and motion acceptance

Material affordances, local licensed typography, explicit state recovery and ES/EN resources stay functional at enlarged text. A selected identity is rendered once during navigation, without incompatible line-break crossfades; error/empty states do not retain prior results. Android animation preferences remove spatial transition and metadata delay even under the test clock. Four isolated native journeys and two production-entry integrations cover the user paths; optional deterministic frame capture makes the motion evidence repeatable. DESIGN.md records actual tokens after implementation, not a speculative mockup.
