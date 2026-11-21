# Verification

21 November 2026. Results are scoped to the environment and artifact described; an emulator/build is not physical-device, TalkBack, performance or store approval evidence.

| Check | Executed result |
| --- | --- |
| JVM / Corretto 17.0.14 on macOS | 30 tests passed: Domain 4, cache 9, HTTP/pacing 8, presentation 9; no failures or skipped cases. |
| Android Lint | Zero errors; dependency/target and backup notices remain visible. No baseline hides them. |
| Artifacts | Debug APK and optimized release AAB built; an independent exported package was also built successfully. |
| Native Android 17 / API 37 emulator | Four distinct Compose journeys pass: search/detail/back, error/retry/clear, empty result, suggestion/system Back. Normal and disabled motion tested; Spanish/dark/font 1.8 also passes. |
| Production-entry integration / API 37 | Two additional cases launch the real MainActivity and application graph with controlled HTTP responses: invalid-payload recovery, query/headers, detail/back, activity recreation and cache hit; distinct 429/503 UI recovery. Retrofit, repository, cache and production ViewModel factory are retained. No public network is used. |
| Native visual review | English/light phone 360×640dp, Spanish/dark/text 1.8, and expanded 800×1280dp on the same emulator inspected. Expanded viewport is not separate tablet hardware. Error/empty images may be scrolled to their state. |
| Motion review | Six test-clock frames each for entry/return, normal/disabled. Independent review found duplicate/clipped text and ineffective reduced-motion evidence; all three fixes were scored resolved after a correction batch. No FPS measurement. |
| Live API | Production app searches Dune, opens its real bibliographic detail, returns and reuses a valid cached query. Separate HTTPS smoke check returned The Hobbit. Automated suites use controlled data instead. |
| Artifact audit | Advisory release report: DEX 2.35 MB, R8 metadata present, four native ABIs aligned to 16 KB, no tool warnings. This does not verify Google Play policy or store approval. |

The first release export was verified byte-for-byte against all 39 public blobs. Subsequent revisions are reviewed and rechecked before delivery. The final hosted result is recorded below and remains available through [CI](https://github.com/navalcode/shelf-android/actions/workflows/checks.yml).

## Attempts and limitations

- The error-contract revision added an HTTP 204 regression test that initially exposed Retrofit’s null-body exception. Returning an explicit HTTP Response lets Data classify an absent successful body as invalid data; the test now passes.
- Initial local AVD startup failed the disk allocation gate. The owned AVD was recreated with a bounded data partition; no unrelated emulator was used.
- Lint caught direct ViewModel construction in the composable test; the test now uses a lifecycle-owned factory. A later test-window configuration mistake ran off the UI thread and was corrected without weakening assertions.
- The first hosted run `44881311209` passed build but failed native preparation on dependency resolution before tests; Maven artifact URLs returned 200 from the local host afterwards. Root cause is not established. Native validation now waits for build and compiles APKs before boot. `44881689424` was superseded; bootstrap `44881941484` passed both jobs.
- Capture files originally disappeared with Gradle's target uninstall. The native helper copies original display PNGs to a shell-readable output directory before cleanup. Early route-change images and an unsuccessful media-decoding attempt are not shipping evidence.
- The first shared-bounds animation crossfaded incompatible line breaks, and Compose's test clock masked system duration settings. Final code renders a single identically typeset shared element and explicitly observes Android settings; disabled motion removes movement and delays.

Minimum API 26, actual process termination/restoration, physical devices, release runtime, TalkBack, production signing and store distribution have no new runtime validation here. SavedStateHandle reconstruction is tested but is not an OS process-death test. Screenshots and the GIF have [explicit provenance](images/PROVENANCE.json); the GIF uses a fictional fixture and sampled test-clock frames, not real-time hardware footage. 

## Hosted result

[Run 44884887236](https://github.com/navalcode/shelf-android/actions/workflows/checks.yml) completed successfully on code commit `previous source revision`: both build and device jobs passed, including all four native cases on API 35. The documentation-only commit previous source revision preserves that tested runtime, tests, dependency declarations, assets and workflow exactly. This is the previous visual delivery; the error-contract revision is recorded separately below. The earlier bootstrap and failed/superseded attempts above remain part of the record.


## Error-contract revision

The revised local suite passes 30 JVM tests, lint, debug APK, instrumentation APK and optimized release AAB. The six native cases pass on API 37 in both English/light/font 1.0 and Spanish/dark/font 1.8 with system motion disabled. Normal-motion visual evidence above belongs to the preceding visual delivery; this revision does not change motion code. Domain error categories replace transport checks in Presentation; invalid, missing, malformed and HTTP 204 payloads are distinguished from connectivity failures. Malformed/HTTP error responses cannot seed the cache. A deliberately obsolete failure is also rejected after a newer success.

The architecture guide was rendered through GitHub GFM and checked for a bounded Mermaid block, its separate following paragraph and all six following section headings. These checks verify Markdown structure; they are not a screenshot review of GitHub’s Mermaid renderer.

Hosted verification for this revision is pending publication; the run above describes the preceding code, not this revision.
