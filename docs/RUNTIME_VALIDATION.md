# Optimized runtime and native frame validation

The readability follow-up keeps the same product behavior and UI geometry. Kotlin/Gradle use hash-pinned ktfmt; the dependency graph has named construction steps, and the ViewModel's error mapper is a separate readable function. No application framework or runtime dependency was added.

## Executed emulator route

The installed optimized APK uses R8/resource shrinking and a temporary QA certificate. On the owned Android 17/API 37 arm64 emulator, both the external cold-launch inspector and a separate same-certificate platform probe expose the actual accessible search input. The probe retains the production Application, Retrofit, repository/cache and ViewModel graph; its live Dune journey opens detail and returns through real accessibility actions.

The final measured run used a 360 × 640 dp window, English/light/font 1.0, system animation scales 1, and host GPU acceleration on Apple M1 Pro. Three round trips warmed the route; 20 subsequent round trips completed successfully. Hierarchy inspection occurs after each 450 ms transition settle. FrameMetrics callbacks observe real platform rendering without a Compose test clock. The callback queue is drained before serialization; no metric reports were dropped.

| Measurement | Result |
| --- | --- |
| Measured frames | 880 |
| TOTAL_DURATION p50 / p95 / p99 | 17.506 /23.696 /34.954ms |
| Frames exceeding their own reported DEADLINE | 67/880,7.614% |
| Layout/measure p95 | 0.052ms |
| Draw p95 | 1.976ms |
| Swap buffers p95 | 13.264ms |
| GPU p95 | 18.216ms |

These are separate percentiles, not components to add together. TOTAL_DURATION/deadline exceedance is reported unchanged; it is not a claim of zero jank or hardware FPS. The comparatively high buffer/GPU phases are consistent with a cost in the virtual graphics pipeline; this is an inference, not isolation of the cause. Layout/draw results provide no reason to remove useful behavior just to improve an emulator number. Physical-device performance remains a different validation.

The first run used hierarchy polling during transition and recorded 7.386% deadline exceedance. Moving inspection until after settling recorded 7.727%; the final phase-instrumented run recorded 7.614%. The change did not establish that polling caused the overruns. An earlier SwiftShader run was only a harness check and is not comparable to these host-GPU runs.

[Native samples and conditions](evidence/emulator-frame-metrics.json) preserve all rendering samples. [Derived summary](evidence/emulator-frame-summary.json) is reproducible:

```sh
python3 verification/summarize_frame_metrics.py docs/evidence/emulator-frame-metrics.json --output .local/frame-summary.json
python3 -m unittest discover -s verification -p test_frame_metrics.py
```

The six evidence-parser tests distinguish unavailable deadlines/GPU values from zero and reject mismatched phase batches. [Probe reproduction](../verification/README.md) covers signing, startup-only CI and optional live measurement. This is not Perfetto presentation-time analysis or a cold-start benchmark.

## TalkBack

TalkBack 17 was enabled in the owned emulator in Spanish, with touch exploration and spoken-feedback service bound without a reported service crash. Manuel completed the human search/Dune/detail/Back journey and explicitly confirmed correct announcements and navigation, with the query preserved. This is a tester-confirmed route on that emulator, not formal accessibility conformance or a physical-device claim. The prior accessibility state was restored afterwards; Shelf remains installed and open. The physical phone was left unchanged.

## Hosted checks

[Android checks](https://github.com/navalcode/shelf-android/actions/workflows/checks.yml) runs formatting, unit tests, lint/build and native/optimized-startup checks on API26/API35. [Verification](VERIFICATION.md) records the latest publication result.
