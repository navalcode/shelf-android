#!/usr/bin/env python3
"""Run a same-certificate platform probe against an already installed optimized Shelf APK."""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess
import threading

PACKAGE = "dev.navalcode.shelf"
RUNNER = "dev.navalcode.shelf.verification/dev.navalcode.shelf.verification.RuntimeProbe"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial", required=True)
    parser.add_argument("--app-apk", type=Path, required=True)
    parser.add_argument("--probe-apk", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--adb", default="adb")
    parser.add_argument("--journey", action="store_true", help="Live HTTP + warm native frame measurements")
    parser.add_argument("--cycles", type=int, default=20)
    args = parser.parse_args()
    if not 1 <= args.cycles <= 50:
        parser.error("cycles must be 1..50")
    base = [args.adb, "-s", args.serial]
    def adb(*command):
        return subprocess.check_output([*base, *command], timeout=60).decode()
    sha = hashlib.sha256(args.app_apk.read_bytes()).hexdigest()
    path = adb("shell", "pm", "path", PACKAGE).strip().removeprefix("package:")
    if not path.startswith("/data/app/") or "\n" in path:
        raise RuntimeError("Expected one installed standalone Shelf APK")
    if adb("shell", "sha256sum", path).split()[0] != sha:
        raise RuntimeError("Installed Shelf APK differs from the supplied artifact")
    args.output.mkdir(parents=True, exist_ok=True)
    adb("install", "--no-incremental", "-r", str(args.probe_apk.resolve()))
    remote = f"/sdcard/Android/data/{PACKAGE}/files"
    adb("shell", "rm", "-f", f"{remote}/result.json")
    command = [*base, "shell", "am", "instrument", "-w", "-r", "-e", "journey",
               str(args.journey).lower(), "-e", "cycles", str(args.cycles), RUNNER]
    process = subprocess.Popen(command, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
    lines = []
    ready = threading.Event()
    def read():
        for line in process.stdout:
            lines.append(line)
            if "SHELF_PROBE_READY" in line:
                ready.set()
    reader = threading.Thread(target=read, daemon=True)
    reader.start()
    if not ready.wait(25):
        process.terminate()
        raise RuntimeError("Probe did not become ready: " + "".join(lines))
    # Explicit foreground launcher action, without bypassing OEM policies or the lock screen.
    launch = adb("shell", "am", "start", "-W", "-a", "android.intent.action.MAIN",
                 "-c", "android.intent.category.LAUNCHER", "-f", "0x10200000", "-n", f"{PACKAGE}/.MainActivity")
    try:
        process.wait(timeout=180 + 15 * args.cycles)
    finally:
        reader.join(timeout=5)
        (args.output / "instrumentation.log").write_text("".join(lines))
        (args.output / "launcher.log").write_text(launch)
    if process.returncode != 0 or "SHELF_PROBE_PASSED" not in "".join(lines):
        raise RuntimeError("Optimized probe did not pass; inspect instrumentation.log")
    report = json.loads(adb("exec-out", "cat", f"{remote}/result.json"))
    if report.get("package") != PACKAGE or not report.get("search_input_accessible"):
        raise RuntimeError("Probe did not expose the application's accessible input")
    report["apk_sha256"] = sha
    report["launcher_assistance"] = "One explicit SDK launcher action after instrumentation initialization"
    (args.output / "result.json").write_text(json.dumps(report, indent=2) + "\n")
    for name in ["optimized-startup.png"] + (["optimized-after-journey.png"] if args.journey else []):
        subprocess.run([*base, "pull", f"{remote}/{name}", str(args.output / name)], check=True)
    print(json.dumps({"passed": True, "api": report["api"], "apk_sha256": sha,
                      "measured_frames": len(report.get("frames", []))}))


if __name__ == "__main__":
    main()
