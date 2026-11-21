# Verification

21 November 2026. Results apply to the source, environment and route described below.

| Check | Result |
| --- | --- |
| JVM tests | 32 passing: Domain 5, cache 9, HTTP 9 and presentation 9; no failures or skipped cases. |
| Evidence-parser tests | Six passing cases cover deadline boundaries, unavailable metrics, idle intervals and matching phase batches. |
| Android lint | Zero errors. Existing dependency/target and backup notices remain visible. |
| Native integration | Nine cases pass on API 26 and API 35. Each launches MainActivity and the production graph, replacing only HTTP responses. |
| Optimized startup | External cold-launch inspection and a separate same-certificate platform probe pass on API 26/API 35. The probe retains the release Application, Retrofit, cache and ViewModel wiring. |
| Viewports | API 37 emulator:360 × 640 dp and640 × 360 dp phone windows,800 × 1280 dp and1280 × 800 dp expanded windows. English/light/font 1.0 and Spanish/dark/font 1.8 exercised. |
| Accessibility | Names, roles, action labels, targets, pane title and state announcements checked. API 34+ adds Accessibility Test Framework scans. Manuel confirmed the Spanish TalkBack search/detail/Back journey in the emulator, with the query retained. |
| Motion | Entry/return sampled with normal and disabled system animation settings. Samples verify identity/geometry and the reduced-motion path; they are not FPS measurements. |
| Process loss | QA-signed optimized APK: Home, background process kill, old PID absent, retained task resumed through the launcher, new PID and restored detail/query. Force-stop, task removal and reboot persistence are outside this guarantee. |
| Live API | The optimized app searches Dune and opens actual bibliographic detail. Automated deterministic suites and CI use controlled responses; the optional live probe is separate. |
| Artifact inspection | Advisory AAB report: 2.35 MB DEX, R8 metadata present and native libraries aligned to 16 KB. APKs retain the missing bundle-R8-metadata warning. This is not store approval. |

## Continuous integration

[Android checks](https://github.com/navalcode/shelf-android/actions/workflows/checks.yml) runs formatting, unit tests, lint, debug/release builds and native checks on API 26/API 35. A fresh run will validate the current main-branch publication; the executed source checks above precede the documentation/history cleanup.

## Rendering measurements

A separate API 37 emulator run uses the optimized APK and real navigation: three warm-up round trips, then 20 measured round trips. All 20 complete, producing 880 FrameMetrics samples and no dropped callbacks. TOTAL_DURATION p95 is 23.696 ms; 67 frames exceed their reported deadline. [Runtime validation](RUNTIME_VALIDATION.md) includes native samples, phases and reproduction.

These observations include the emulator graphics pipeline and instrumentation overhead. They do not establish physical-device FPS or isolate the cause of deadline overruns.

## Limits

- The owned emulator remains installed and available. Its TalkBack setting was restored after the confirmed human route.
- Earlier Android 14 physical tests passed nine cases with foreground-launch assistance required by the OEM. The external release inspector returned a null accessibility root on that phone; that path is not counted as passed. The phone was left unchanged when follow-up validation moved to the emulator.
- A human screen-reader route and automated scans do not establish formal accessibility conformance.
- Production signing, store distribution and physical frame-time benchmarks are outside executed validation.

[Runtime reproduction](../verification/README.md) covers the QA certificate, installed-APK identity, startup probe and retained-task procedure. [Image provenance](images/PROVENANCE.json) distinguishes live catalogue captures from original test fixtures and sampled navigation frames.
