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
| Error and empty states recover. | Domain error test, HTTP 429/503 tests, presentation retry and Compose retry/clear/empty journeys. |
| The UI remains lifecycle-owned and restorable within Android task state. | ViewModel factory, Navigation back stack and SavedStateHandle reconstruction test. OS process termination is not covered by that unit test. |
| Reviewers can build and test without a private account. | JDK/SDK instructions, checked wrapper SHA, independent export build, CI workflow. |

Domain tests use in-code repository doubles. Data HTTP tests use MockWebServer; no external API is involved in deterministic CI. Presentation uses virtual time. Native screenshots use an original fictional book fixture, not a fabricated live service result.

Known limits: single UI consumer (no shared in-flight request coalescing), no pagination, memory cache lost on process death, no physical-device/TalkBack/store or performance claims. See VERIFICATION for actually executed checks.
