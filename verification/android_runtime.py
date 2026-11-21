#!/usr/bin/env python3
"""Cold-launch a QA-signed optimized APK on an explicitly selected test device."""
import argparse
import json
import hashlib
from pathlib import Path
import re
import subprocess
import time
import xml.etree.ElementTree as ET

PACKAGE = "dev.navalcode.shelf"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial", required=True)
    parser.add_argument("--apk", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--adb", default="adb")
    parser.add_argument("--already-installed", action="store_true", help="Verify installed APK bytes instead of reinstalling")
    args = parser.parse_args()

    def adb(*command):
        return subprocess.check_output([args.adb, "-s", args.serial, *command], timeout=60).decode()

    artifact_sha = hashlib.sha256(args.apk.read_bytes()).hexdigest()
    if args.already_installed:
        paths = adb("shell", "pm", "path", PACKAGE).strip().splitlines()
        if len(paths) != 1 or not paths[0].startswith("package:/data/app/"):
            raise RuntimeError("Expected one installed standalone Shelf APK")
        installed_sha = adb("shell", "sha256sum", paths[0].removeprefix("package:")).split()[0]
        if installed_sha != artifact_sha:
            raise RuntimeError("Installed APK differs from the supplied artifact")
    else:
        # An unexpected signing mismatch fails; no automatic uninstall fallback.
        adb("install", "--no-incremental", "-r", str(args.apk.resolve()))
    adb("shell", "am", "force-stop", PACKAGE)
    launch = adb("shell", "am", "start", "-W", "-a", "android.intent.action.MAIN",
                 "-c", "android.intent.category.LAUNCHER", "-f", "0x10200000", "-n", f"{PACKAGE}/.MainActivity")
    if "Status: ok" not in launch:
        raise RuntimeError(f"Activity launch failed: {launch}")
    deadline = time.monotonic() + 30
    dump_retries = 0
    while True:
        adb("shell", "rm", "-f", "/sdcard/shelf-runtime.xml")
        adb("shell", "uiautomator", "dump", "/sdcard/shelf-runtime.xml")
        try:
            xml = adb("exec-out", "cat", "/sdcard/shelf-runtime.xml")
            nodes = list(ET.fromstring(xml).iter("node"))
        except (ET.ParseError, subprocess.CalledProcessError):
            nodes = []  # Android can expose no accessibility root during the first draw.
        if any(n.get("package") == PACKAGE and n.get("resource-id") == "search_input"
               and n.get("bounds") != "[0,0][0,0]" for n in nodes):
            break
        dump_retries += 1
        if time.monotonic() >= deadline:
            raise RuntimeError("Launcher did not expose the accessible search input")
        time.sleep(0.25)
    pid = adb("shell", "pidof", PACKAGE).strip()
    if not pid:
        raise RuntimeError("Application exited after its first frame")
    args.output.parent.mkdir(parents=True, exist_ok=True)
    report = {
        "package": PACKAGE,
        "api": adb("shell", "getprop", "ro.build.version.sdk").strip(),
        "pid": pid,
        "apk_sha256": artifact_sha,
        "accessibility_dump_retries": dump_retries,
        "search_input_accessible": True,
        "launch_timing_ms": dict(re.findall(r"^(ThisTime|TotalTime|WaitTime): (\d+)$", launch, re.M)),
        "scope": "Cold launcher smoke on QA-signed R8 APK; not a hardware benchmark or production signing claim",
    }
    args.output.write_text(json.dumps(report, indent=2) + "\n")
    args.output.with_suffix(".xml").write_text(xml)
    args.output.with_suffix(".png").write_bytes(subprocess.check_output(
        [args.adb, "-s", args.serial, "exec-out", "screencap", "-p"], timeout=20))
    print(json.dumps(report))


if __name__ == "__main__":
    main()
