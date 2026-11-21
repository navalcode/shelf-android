#!/usr/bin/env python3
"""Format/check Kotlin sources with a pinned, hash-verified ktfmt release."""
import argparse
import hashlib
import os
from pathlib import Path
import subprocess
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
VERSION = "0.65"
SHA256 = "aba9753b6ab387926179ab7318ba8e0c33dad483ebc72f633e79522265b15214"
URL = f"https://github.com/Kotlin/ktfmt/releases/download/v{VERSION}/ktfmt-{VERSION}-with-dependencies.jar"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true")
    parser.add_argument("--jar", type=Path, help="Use an existing verified jar for offline execution")
    args = parser.parse_args()
    jar = args.jar or ROOT / ".gradle/verification-tools" / f"ktfmt-{VERSION}.jar"
    if not jar.exists():
        if args.jar:
            parser.error("The supplied ktfmt jar does not exist")
        data = urllib.request.urlopen(URL, timeout=60).read()
        if hashlib.sha256(data).hexdigest() != SHA256:
            raise RuntimeError("Downloaded ktfmt hash differs from the pinned release")
        jar.parent.mkdir(parents=True, exist_ok=True)
        jar.write_bytes(data)
    if hashlib.sha256(jar.read_bytes()).hexdigest() != SHA256:
        raise RuntimeError("Cached ktfmt hash differs from the pinned release")
    sources = sorted(str(p) for p in ROOT.rglob("*") if p.suffix in {".kt", ".kts"}
                     and not {"build", ".gradle", ".kotlin", ".git"}.intersection(p.relative_to(ROOT).parts))
    java_home = os.environ.get("JAVA_HOME")
    java = str(Path(java_home) / "bin/java") if java_home else "java"
    command = [java, "-jar", str(jar), "--kotlinlang-style"]
    if args.check:
        command += ["--dry-run", "--set-exit-if-changed"]
    subprocess.run([*command, *sources], check=True)


if __name__ == "__main__":
    main()
