# Shelf

Shelf searches the Open Library catalogue and displays bibliographic details in a native Android interface.

## Search and detail

Users enter a title, author or subject, then open a result to inspect its author, first publication, edition count and catalogue identifier. Missing metadata is labelled explicitly. Suggestions provide starting queries without preloading fictional catalogue results.

## Behavior

- Editing cancels the previous request before the 400 ms debounce and removes its visible results.
- Each normalized query has a three-minute memory-cache entry, bounded to 20 recent queries.
- Network requests return at most 20 works and observe a minimum interval between starts.
- Loading, empty, invalid-data, connectivity and service failures have distinct states with appropriate recovery.
- Back navigation retains the query; a small selected-work snapshot supports Android task restoration.

## Interface

Spanish resources have an English fallback. The app follows system light/dark and animation settings, supports scalable text and uses bounded single- or two-pane layouts. Title and author connect list and detail through one shared transition.

## Implementation

Kotlin, Jetpack Compose Material 3, Navigation and ViewModel provide the Android interface. Pure JVM domain and data modules contain the use case, repository contract, Retrofit integration and cache. Dependency wiring is explicit.

The scope is two screens. Accounts, disk cache, pagination and store distribution are outside this exercise.

## Verification

The [acceptance contract](docs/SPEC.md) maps behavior to tests. [Verification](docs/VERIFICATION.md) records executed checks, and [runtime validation](docs/RUNTIME_VALIDATION.md) documents the optimized build, screen-reader journey and rendering measurements.
