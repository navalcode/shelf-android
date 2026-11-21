# Verification

Local checks, 21 November 2026 (macOS, Corretto JDK 17.0.14):

- 25 JVM tests passed: Domain 4, Data cache 9, Data HTTP/pacing 6, presentation 6; no failures or skipped cases.
- Debug APK and optimized release AAB built successfully.
- Android Lint: zero errors after fixing a test ViewModel lifecycle mistake. Version/target and backup notices remain recorded; no baseline or suppression hides them.
- Live Open Library HTTPS smoke check returned The Hobbit, J.R.R. Tolkien, 1937 and an edition count. Deterministic suites do not depend on that service or its changing metadata.
- Local native emulator could not start: available disk below the image's required 7.2 GiB allocation gate. No native success is claimed at this stage. CI native results will be recorded after execution.
- Advisory artifact audit: release DEX 2.27 MB, R8 metadata present and four native ABIs aligned to 16 KB. This is a tool measurement, not Google Play policy verification or store approval. Debug contains 25.59 MB DEX without R8 metadata, expected for a debug artifact; its audit warning is retained.

Initial failures are kept in the local task evidence: native emulator disk gate and Lint detecting direct ViewModel construction in a composable test. Tests now create a lifecycle-owned ViewModel. No app feature or assertion was disabled to obtain a pass.

Pending: completed native runtime/visual review, hosted CI results and public-tree verification. Minimum API 26, physical devices, TalkBack, process termination/restoration and release runtime are separate checks, not implied by JVM tests/builds.
