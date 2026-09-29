#!/usr/bin/env python3
"""Build a desktop testing ZIP from an explicit allowlist; never include user data."""
import hashlib
import os
from pathlib import Path
import shutil
import subprocess
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[1]
NS = {"m": "http://maven.apache.org/POM/4.0.0"}


def main():
    pom = ET.parse(ROOT / "pom.xml").getroot()
    version = pom.findtext("m:version", namespaces=NS)
    maven = shutil.which("mvn.cmd" if os.name == "nt" else "mvn")
    if not maven:
        raise SystemExit("Maven is required only on the build computer.")
    subprocess.run([maven, "clean", "verify", "-q"], cwd=ROOT, check=True)
    jar = ROOT / "target" / f"managers-sidekick-{version}.jar"
    with zipfile.ZipFile(jar) as executable:
        manifest = executable.read("META-INF/MANIFEST.MF")
        if b"Main-Class: gr.sidekick.App" not in manifest:
            raise SystemExit("Missing runnable manifest; refusing to ship.")
        for entry in ("gr/sidekick/App.class", "com/google/gson/Gson.class", "org/apache/pdfbox/pdmodel/PDDocument.class"):
            executable.getinfo(entry)

    prefix = f"managers-sidekick-{version}"
    output = ROOT / "dist" / f"{prefix}-test.zip"
    output.parent.mkdir(exist_ok=True)
    files = {"managers-sidekick.jar": jar.read_bytes(), "START-HERE.md": (ROOT / "packaging/START-HERE.md").read_bytes()}
    files["START-HERE.en.md"] = (ROOT / "packaging/START-HERE.en.md").read_bytes()
    files["brand/logo.svg"] = (ROOT / "src/main/resources/brand/logo.svg").read_bytes()
    for path in sorted((ROOT / "docs").glob("*.md")):
        files[f"docs/{path.name}"] = path.read_bytes()
    files["start-linux.sh"] = (ROOT / "packaging/start-linux.sh").read_bytes()
    files["start-windows.cmd"] = (ROOT / "packaging/start-windows.cmd").read_text().replace("\n", "\r\n").encode("ascii")
    # Match the pinned runtime dependencies of 0.4.0, including transitive jars.
    runtime = [
        "com/google/code/gson/gson/2.14.0/gson-2.14.0.jar",
        "com/google/errorprone/error_prone_annotations/2.48.0/error_prone_annotations-2.48.0.jar",
        "org/apache/pdfbox/pdfbox/3.0.7/pdfbox-3.0.7.jar",
        "org/apache/pdfbox/pdfbox-io/3.0.7/pdfbox-io-3.0.7.jar",
        "org/apache/pdfbox/fontbox/3.0.7/fontbox-3.0.7.jar",
        "commons-logging/commons-logging/1.3.5/commons-logging-1.3.5.jar",
    ]
    notices = ["# Included runtime libraries", "", "All libraries below use Apache License 2.0.",
               "Original LICENSE/NOTICE files supplied in their jars are retained here.",
               "The application is supplied for testing; no source license is assigned by this package.", ""]
    for relative in runtime:
        source = ROOT / ".maven-repository" / relative
        notices.append(f"- {source.stem}")
        with zipfile.ZipFile(source) as dependency:
            for name in dependency.namelist():
                if not name.endswith("/") and Path(name).name.upper().startswith(("LICENSE", "NOTICE", "COPYING")):
                    files[f"licenses/{source.stem}/{Path(name).name}"] = dependency.read(name)
    files["licenses/THIRD-PARTY.md"] = "\n".join(notices).encode("utf-8")
    files["SHA256SUMS.txt"] = "".join(f"{hashlib.sha256(content).hexdigest()}  {name}\n" for name, content in sorted(files.items())).encode("utf-8")
    with zipfile.ZipFile(output, "w", zipfile.ZIP_DEFLATED) as archive:
        for name, content in sorted(files.items()):
            info = zipfile.ZipInfo(f"{prefix}/{name}", (2026, 1, 1, 0, 0, 0))
            info.create_system = 3
            info.external_attr = (0o100755 if name.endswith(".sh") else 0o100644) << 16
            info.compress_type = zipfile.ZIP_DEFLATED
            archive.writestr(info, content)
    with zipfile.ZipFile(output) as archive:
        if archive.testzip():
            raise SystemExit("ZIP integrity check failed.")
        assert not any("/data/" in name or "/.git/" in name for name in archive.namelist())
    checksum = hashlib.sha256(output.read_bytes()).hexdigest()
    output.with_suffix(output.suffix + ".sha256").write_text(f"{checksum}  {output.name}\n")
    print(f"Created: {output}\nSHA-256: {checksum}")


if __name__ == "__main__":
    main()
