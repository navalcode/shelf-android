# Verification

21 November 2026. Results describe the tested artifact and environment. Build success, automated accessibility checks and sampled animation frames do not establish store approval, human TalkBack usability or hardware performance.

| Check | Executed result |
| --- | --- |
| JVM / Corretto 17 on macOS | 32 tests: Domain 5, cache 9, HTTP 9, presentation 9; no failures or skips. Includes Turkish-locale normalization, a server exceeding the 20-item limit, cancellation, stale successes/failures, TTL and failure recovery. |
| Build and lint | Debug, instrumentation and optimized release APK/AAB built. Lint has zero errors; dependency/target and backup notices remain visible. No suppression baseline. |
| Native application integration | Nine cases launch MainActivity with the real application graph, Retrofit, cache and ViewModel factory. Only HTTP is controlled. Search/detail/Back, HTTP and invalid-data recovery, recreation/cache, IME/focus, accessible actions and absent metadata pass on API 37. |
| Accessibility | Names, roles, action labels, 48dp targets, pane title and polite error/empty announcements checked. API 34+ adds Android Accessibility Test Framework scans. Manuel confirmed the Spanish TalkBack search/detail/Back journey on the emulator; see the follow-up below. |
| Viewports | Phone 360×640dp, actual landscape 640×360dp, expanded 800×1280dp and 1280×800dp on the owned API 37 emulator. Spanish/dark/font 1.8 and English/light/font 1.0 exercised. Resizing one emulator is not tablet hardware validation. |
| Motion | Entry/return sampled at logical Compose-clock offsets 0/16/80/160/240/400ms, with system scales 1 and 0; expanded normal-motion samples too. Discrete captures are not FPS or frame-time measurements. |
| Optimized runtime | QA-signed R8 APK cold-launches on API 37 and exposes the accessible input. Live Dune search/detail works. The same optimized APK is installed on the physical test phone. |
| Actual process loss | Optimized APK: Home, background `am kill`, verified absent old PID, same launcher intent, new PID, restored detail fields and original query after Back. Retained-task restoration only; no force-stop/reboot/persistent-cache claim. |
| Physical Android 14 / API 34 | All nine native cases passed, including ATF scans, with explicit foreground-launch assistance required by OEM background-start restrictions. No security settings changed. This run precedes only the compact search-border correction; final optimized APK is subsequently installed. |
| Artifact audit | Advisory AAB report: DEX 2.35 MB, R8 shrinking 98%, optimization 97.04%, obfuscation 97.91%; four native ABIs aligned to 16 KB. APK ELF/ZIP alignment passes; APKs lack bundle R8 metadata and retain that warning. This is tool output, not verified store policy. |

## Readability and emulator-validation follow-up

The follow-up source uses hash-pinned ktfmt, a more explicit dependency graph and a dedicated presentation-error mapper. Local 32 JVM tests/lint/build checks pass. Six Python tests protect FrameMetrics evidence interpretation. The installed QA-signed R8 APK passes external cold-launch inspection and a separate same-certificate platform probe on API 37, retaining the real application graph.

The requested follow-up uses the owned emulator; the physical installation is preserved. Twenty warmed navigation round trips produce 880 native FrameMetrics samples, with67/880 exceeding their reported deadline. [Runtime validation](RUNTIME_VALIDATION.md) supplies phase timings, raw samples, method and limits. Manuel confirms the human Spanish TalkBack search/detail/Back journey, comprehensible announcements and retained query. The prior accessibility state was restored; Shelf remains installed and open.

[Run 44901113590](https://github.com/navalcode/shelf-android/actions/workflows/checks.yml) passed on code `previous source revision` at attempt 1:32 JVM tests, zero lint errors, pinned formatting, six frame-evidence tests, nine native cases on each API 26/API 35 and both optimized-startup inspection paths. Downloaded native/runtime reports were inspected. Subsequent presentation-only commits update Markdown and the export manifest; application and verification code, tests, assets, dependencies and workflow remain identical to that tested commit.

Preceding follow-up run 44899700421, code `previous source revision`, passed at attempt 2. API 35's first attempt received Maven Central HTTP 429 during UTP resolution before tests. It is retained as an infrastructure failure, not a failing app case or hidden test retry. The completed quality-delivery results below remain scoped to their earlier recorded artifact.

## Hosted verification

[Run 44897276762](https://github.com/navalcode/shelf-android/actions/workflows/checks.yml) passed on code commit `previous source revision`: build plus native integration and optimized cold-launch checks on API 26 and API 35. Downloaded reports confirm 32 JVM tests tests and nine native cases on each API, with no failures/skips, zero lint errors and an accessible search input from the QA-signed R8 APK on both APIs. The final documentation-only commit preserves that tested runtime, tests, assets, dependencies and workflow exactly.

Previous successful baseline: [44887189457](https://github.com/navalcode/shelf-android/actions/workflows/checks.yml), commit `previous source revision`, 30 JVM and six native cases on API 35. That baseline does not validate this subsequent UI/test revision.

## Corrections and limits

- A fresh review rejected five landscape images because they were actually portrait. They were replaced with captures from a genuine landscape Android viewport, rather than rotating PNGs. A rounded compact container also clipped the search border; its shape was corrected and affected states recaptured.
- The first native harness used an auxiliary test activity. The current nine cases all exercise the production activity and application graph, improving integration coverage and avoiding that OEM-specific auxiliary-launch dependency.
- On the physical phone, the external shell UIAutomator inspector returned a null accessibility root for the optimized app. That automated release smoke is recorded as unsuccessful, despite visible rendering/live results and passing in-process native checks. The OS recorded a later background exit as low memory; no crash is inferred from that event.
- First physical install attempts were cancelled by the device. Installation proceeded only after the tester accepted the native dialog. The final app remains installed for manual testing.
- The first shared transition duplicated text and changed wrapping; identity now shares one consistently sized text group. System-disabled motion bypasses movement and animation delay. Reviews cover native samples, without a web detector verdict.

Images and GIF have [explicit provenance](images/PROVENANCE.json). Automated suites use original fictional books; live API captures are identified separately. [Runtime reproduction](../verification/README.md) documents the cold-launch and retained-task procedures. Human TalkBack confirmation is scoped to the emulator journey above. Production signing, store distribution, formal accessibility conformance and hardware frame-time benchmarks remain outside executed validation.
