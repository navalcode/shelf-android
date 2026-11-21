# Architecture and review guide

[Acceptance contract](SPEC.md) maps each behavior to its test or explicit validation limit.

```text
app (Compose → SearchViewModel → SearchBooks)
 ↓                                      ↓
data (CachedBookRepository) → domain (BookRepository)
 ↓
OpenLibrarySource → Retrofit suspend → OkHttp → HTTPS
```

Domain depends only on Kotlin/coroutines; Data depends on Domain. The Android application is the composition root. Manual DI exposes a single application-scoped repository/cache/source and a lifecycle-owned ViewModel; tests replace the same public ports.

## Read in this order

1. `domain/.../Books.kt`: models, canonical query, use case and repository contract.
2. `data/.../CachedBookRepository.kt`: short mutex-protected cache operations; no cache lock over network.
3. `data/.../OpenLibrary.kt`: HTTP DTO mapping, validation and cancellable request pacing.
4. `app/.../presentation/SearchViewModel.kt`: cancel-before-debounce, generation and state.
5. `app/.../presentation/ShelfApp.kt`: stateless screens and navigation by selected snapshot.

## Concurrency and cache contract

Editing increments a generation, cancels the old job, replaces visible content with loading and starts a new job. Blank input cancels and returns idle without HTTP. Debounce runs inside the new job, so the old request is already cancelled. Retrofit's coroutine adapter connects cancellation to its Call. CancellationException is rethrown before all error handling. A stale generation cannot write success/error even if a substitute repository ignores cancellation.

Cache TTL is 180,000 ms from receipt using elapsedRealtime on Android. The query is trimmed, whitespace collapsed and lowercased using Locale.ROOT before both network and cache. Empty results are cached. Exact expiry is a miss. Failed refresh does not expose an expired entry. LRU bounds memory to 20 queries of at most 20 books each under the normal API contract. Cache and detail snapshot have different lifetimes: selected detail stays readable after expiry; a new search still refreshes its query.

One UI consumer is expected. Cache operations are thread safe; identical concurrent misses are **not coalesced**. Source serializes request starts; cancellation releases that gate and spacing survives cancellation. No persistent cache, offline-first promise, pagination or retry loop. A future multi-consumer app should add keyed single-flight behavior deliberately.

## Validation and limits

Malformed required identifiers/title/list reject the entire response; optional fields remain absent. HTTP errors preserve their status, including a separate 429 message. The UI never renders raw exceptions, HTML or arbitrary external links. Query snapshots are bounded to 121 characters (120 allowed plus validation feedback). Searches use generic Open Library `q` semantics, including its supported query syntax.

SavedStateHandle stores query and selected fields only; Navigation handles the back stack. System task restoration is conditional on Android retaining saved state. Tests of SavedStateHandle reconstruction do not prove an OS process-death journey. No production signing, store publishing, physical-device behavior, TalkBack or formal accessibility conformance is claimed.

## Design questions / Preguntas de diseño

- Why cancel before debounce? / ¿Por qué cancelar antes del debounce?
- Why elapsedRealtime instead of wall time? / ¿Por qué reloj monotónico?
- What happens at exact expiry or failed refresh? / ¿Qué ocurre en la expiración exacta o un refresco fallido?
- Why retain a detail snapshot instead of a global selected ID only? / ¿Por qué un snapshot mínimo del detalle?
- What changes for multiple concurrent consumers? / ¿Qué cambia con varios consumidores?

These questions concern the implementation in this repository. Source code and tests provide the basis for discussing each tradeoff.
