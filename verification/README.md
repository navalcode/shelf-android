# Optimized runtime check

The release APK is minified with the same R8/resource-shrinking configuration as the release bundle. CI signs it with a new temporary QA certificate, launches it on API 26 and API 35 after the Compose suite, and verifies that the process remains alive and the accessible search input is exposed. No public HTTP request is needed to open Shelf.

Use an explicitly selected test emulator. This installs Shelf and force-stops its current process for a cold start. A standard nonincremental install avoids relying on optional APK streaming support. An unexpected signing mismatch fails; the script never uninstalls another build automatically.

```sh
./gradlew :app:assembleRelease
SHELF_QA_DIR="$(mktemp -d)"
keytool -genkeypair -keystore "$SHELF_QA_DIR/key.jks" -storepass android -keypass android -alias shelf-qa -keyalg RSA -keysize 2048 -validity 1 -dname "CN=Shelf QA" -noprompt
"$ANDROID_HOME/build-tools/35.0.0/apksigner" sign --ks "$SHELF_QA_DIR/key.jks" --ks-pass pass:android --out "$SHELF_QA_DIR/shelf.apk" app/build/outputs/apk/release/app-release-unsigned.apk
python3 verification/android_runtime.py --serial emulator-5554 --apk "$SHELF_QA_DIR/shelf.apk" --output .local/runtime/result.json
```

For an APK already installed manually, add `--already-installed`: the checker first compares the installed APK SHA-256 with the supplied file and refuses a mismatch. It never uninstalls Shelf. Transient empty accessibility roots during startup are retried; a stale dump cannot pass the check.

The JSON records SDK, process, APK hash and Android launch timing; the XML/PNG record the actual accessible tree and display. One emulator launch timing is not a hardware performance benchmark. QA signing is not production signing or store distribution.

## Retained-task process restoration

For a separate manual check with the live API:

1. Launch through the actual launcher, search for a book, open detail and note its fields.
2. Press Home and wait until Shelf's activity is STOPPED. Record its PID, then `adb -s SERIAL shell am kill --user 0 dev.navalcode.shelf`. If the process is not yet cached, wait and repeat; verify `pidof` returns no PID.
3. Resume the retained task through the same launcher intent: `adb -s SERIAL shell am start -W -a android.intent.action.MAIN -c android.intent.category.LAUNCHER -f 0x10200000 -n dev.navalcode.shelf/.MainActivity`.
4. Verify a different PID, the same detail fields, Back and the original query. Do not force-stop, clear data or reinstall between steps 2 and 3.

A different launch intent can create a new activity instead of restoring the retained one. This checks real process loss within Android's retained task; it makes no promise for force-stop, task removal, reboot or persistent cache. Current executed results and limits are in [Verification](../docs/VERIFICATION.md).

## Readability gate

`python3 verification/format_kotlin.py --check` checks Kotlin and Gradle Kotlin files with ktfmt0.65 and Kotlin's four-space style. Omit `--check` to format. The tool verifies the pinned SHA-256 on every use; `--jar PATH` supports an already downloaded/offline copy. It caches only in ignored `.gradle/verification-tools`. CI also runs four unit tests for native frame-evidence interpretation.

## Same-certificate optimized runtime probe

`android/RuntimeProbe.java` is a separate, platform-only instrumentation APK. It links against Android's public APIs, has no application/Compose dependency and never replaces Shelf's Application, graph, transport or code. Android requires its certificate to match the installed app. This permits inspecting the actual non-debuggable R8 build without adding production keep rules, debug flags or disabling device protections.

With the same temporary QA keystore used above:

```sh
export SHELF_QA_STORE_PASSWORD=android
python3 verification/build_runtime_probe.py --sdk "$ANDROID_HOME" --keystore "$SHELF_QA_DIR/key.jks" --output "$SHELF_QA_DIR/probe"
python3 verification/run_runtime_probe.py --serial emulator-5554 --app-apk "$SHELF_QA_DIR/shelf.apk" --probe-apk "$SHELF_QA_DIR/probe/shelf-runtime-probe.apk" --output .local/signed-runtime
```

The caller verifies installed APK bytes, installs only the separate probe and explicitly foregrounds Shelf once after instrumentation initializes. It never uninstalls Shelf. The probe preserves existing accessibility services, considers only Shelf while its window has focus, checks the accessible search input and records native output. CI runs this startup-only check without public HTTP on API26/API35.

Add `--journey --cycles 20` for a separate physical-device measurement with the live API: Dune search, three unmeasured navigation warm-ups, then20 detail/Back round trips. `Window.OnFrameMetricsAvailableListener` collects actual rendering duration and each frame's deadline (API31+); it excludes first-draw frames. Native screenshots and an own-package accessibility tree accompany the result. The test deliberately uses real navigation actions without a Compose test clock. It is instrumentation-assisted and measures one warmed route, not an unattended general-device certification.

```sh
python3 verification/summarize_frame_metrics.py .local/signed-runtime/result.json --output .local/signed-runtime/summary.json
```

Percentiles use nearest rank. Missing deadlines remain unknown, not zero jank. Frames exceeding their individual deadline are counted; idle gaps are never converted into FPS. Dropped metric callbacks are disclosed. These are FrameMetrics rendering measurements with inspection overhead, not Perfetto presentation-time or a cold-start benchmark. Run with the device unlocked, normal motion and stable conditions; record the conditions and keep raw data outside the public source package.

The external shell UIAutomator command and this same-certificate instrumentation are different inspection paths. A passing probe establishes only what its native output verifies; it does not rewrite an earlier null-root shell failure as a pass or replace a human TalkBack journey.
