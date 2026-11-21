# Verification

21 November 2026. Results describe the tested artifact and environment. Build success, automated accessibility checks and sampled animation frames do not establish store approval, human TalkBack usability or hardware performance.

| Check | Executed result |
| --- | --- |
| JVM / Corretto 17 on macOS | 32 tests: Domain 5, cache 9, HTTP 9, presentation 9; no failures or skips. Includes Turkish-locale normalization, a server exceeding the 20-item limit, cancellation, stale successes/failures, TTL and failure recovery. |
| Build and lint | Debug, instrumentation and optimized release APK/AAB built. Lint has zero errors; dependency/target and backup notices remain visible. No suppression baseline. |
| Native application integration | Nine cases launch MainActivity with the real application graph, Retrofit, cache and ViewModel factory. Only HTTP is controlled. Search/detail/Back, HTTP and invalid-data recovery, recreation/cache, IME/focus, accessible actions and absent metadata pass on API 37. |
| Accessibility | Names, roles, action labels, 48dp targets, pane title and polite error/empty announcements checked. API 34+ adds Android Accessibility Test Framework scans. Human TalkBack speech/navigation remains unverified. |
| Viewports | Phone 360×640dp, actual landscape 640×360dp, expanded 800×1280dp and 1280×800dp on the owned API 37 emulator. Spanish/dark/font 1.8 and English/light/font 1.0 exercised. Resizing one emulator is not tablet hardware validation. |
| Motion | Entry/return sampled at logical Compose-clock offsets 0/16/80/160/240/400ms, with system scales 1 and 0; expanded normal-motion samples too. Discrete captures are not FPS or frame-time measurements. |
| Optimized runtime | QA-signed R8 APK cold-launches on API 37 and exposes the accessible input. Live Dune search/detail works. The same optimized APK is installed on the physical test phone. |
| Actual process loss | Optimized APK: Home, background `am kill`, verified absent old PID, same launcher intent, new PID, restored detail fields and original query after Back. Retained-task restoration only; no force-stop/reboot/persistent-cache claim. |
| Physical Android 14 / API 34 | All nine native cases passed, including ATF scans, with explicit foreground-launch assistance required by OEM background-start restrictions. No security settings changed. This run precedes only the compact search-border correction; final optimized APK is subsequently installed. |
| Artifact audit | Advisory AAB report: DEX 2.35 MB, R8 shrinking 98%, optimization 97.04%, obfuscation 97.91%; four native ABIs aligned to 16 KB. APK ELF/ZIP alignment passes; APKs lack bundle R8 metadata and retain that warning. This is tool output, not verified store policy. |

## Readability and physical-validation follow-up

The follow-up source now uses hash-pinned ktfmt, a more explicit dependency graph and a dedicated presentation-error mapper. Local JVM/lint/build checks pass. Four Python tests protect FrameMetrics evidence interpretation. A separate same-certificate instrumentation probe checks the actual optimized app without replacing its graph; startup and a short live-API journey have passed on the API37 emulator. Its software-rendered frame timings are not physical-device performance evidence.

Fresh hosted checks, installation of the updated physical APK, the measured hardware journey and human TalkBack confirmation are still pending. The completed hosted/physical results below belong to the earlier quality delivery, not an assertion that this follow-up has finished validation.

## Hosted verification

[Run 44897276762](https://github.com/navalcode/shelf-android/actions/workflows/checks.yml) passed on code commit `previous source revision`: build plus native integration and optimized cold-launch checks on API 26 and API 35. Downloaded reports confirm 32 JVM tests and nine native cases on each API, with no failures/skips, zero lint errors and an accessible search input from the QA-signed R8 APK on both APIs. The final documentation-only commit preserves that tested runtime, tests, assets, dependencies and workflow exactly.

Previous successful baseline: [44887189457](https://github.com/navalcode/shelf-android/actions/workflows/checks.yml), commit `previous source revision`, 30 JVM and six native cases on API 35. That baseline does not validate this subsequent UI/test revision.

## Corrections and limits

- A fresh review rejected five landscape images because they were actually portrait. They were replaced with captures from a genuine landscape Android viewport, rather than rotating PNGs. A rounded compact container also clipped the search border; its shape was corrected and affected states recaptured.
- The first native harness used an auxiliary test activity. The current nine cases all exercise the production activity and application graph, improving integration coverage and avoiding that OEM-specific auxiliary-launch dependency.
- On the physical phone, the external shell UIAutomator inspector returned a null accessibility root for the optimized app. That automated release smoke is recorded as unsuccessful, despite visible rendering/live results and passing in-process native checks. The OS recorded a later background exit as low memory; no crash is inferred from that event.
- First physical install attempts were cancelled by the device. Installation proceeded only after the tester accepted the native dialog. The final app remains installed for manual testing.
- The first shared transition duplicated text and changed wrapping; identity now shares one consistently sized text group. System-disabled motion bypasses movement and animation delay. Reviews cover native samples, without a web detector verdict.

Images and GIF have [explicit provenance](images/PROVENANCE.json). Automated suites use original fictional books; live API captures are identified separately. [Runtime reproduction](../verification/README.md) documents the cold-launch and retained-task procedures. Production signing, store distribution, human TalkBack and hardware frame-time benchmarks remain outside executed validation.
