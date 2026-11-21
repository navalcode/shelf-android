#!/usr/bin/env python3
"""Build a separate platform-only instrumentation APK; never changes or installs Shelf."""
import argparse
import os
from pathlib import Path
import subprocess
import zipfile


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--sdk", type=Path, required=True)
    parser.add_argument("--keystore", type=Path, required=True, help="Same QA certificate as the installed app")
    parser.add_argument("--output", type=Path, required=True, help="New build directory outside source")
    args = parser.parse_args()
    source = Path(__file__).parent / "android"
    if args.output.exists():
        parser.error("Output directory must not exist")
    args.output.mkdir(parents=True)
    classes = args.output / "classes"
    classes.mkdir()
    dex = args.output / "dex"
    dex.mkdir()
    tools = args.sdk / "build-tools/35.0.0"
    android = args.sdk / "platforms/android-36/android.jar"
    java_home = Path(os.environ["JAVA_HOME"])
    def run(*command):
        subprocess.run([str(arg) for arg in command], check=True)
    run(java_home / "bin/javac", "--release", "8", "-cp", android, "-d", classes, source / "RuntimeProbe.java")
    run(tools / "d8", "--lib", android, "--min-api", "26", "--output", dex, *sorted(classes.rglob("*.class")))
    unsigned = args.output / "probe-unsigned.apk"
    run(tools / "aapt2", "link", "--manifest", source / "AndroidManifest.xml", "-I", android, "-o", unsigned)
    with zipfile.ZipFile(unsigned, "a", compression=zipfile.ZIP_DEFLATED) as archive:
        archive.write(dex / "classes.dex", "classes.dex")
    aligned = args.output / "probe-aligned.apk"
    run(tools / "zipalign", "-f", "4", unsigned, aligned)
    run(tools / "apksigner", "sign", "--ks", args.keystore, "--ks-key-alias", "shelf-qa", "--ks-pass", "env:SHELF_QA_STORE_PASSWORD", "--out", args.output / "shelf-runtime-probe.apk", aligned)
    print(args.output / "shelf-runtime-probe.apk")


if __name__ == "__main__":
    main()
