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
