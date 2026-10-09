**Version 1.4.0:** [GitHub downloads](https://github.com/jimboL3t/managers-sidekick/releases/tag/v1.4.0) · [EXE/DMG/Linux packages and data migration](DESKTOP.en.md). Details below also describe the previous portable layout; use the DESKTOP guide for the new installation.

# Website packages — 1.3.0

[Ελληνικά](PUBLICATION.md)

## Upload set

Run `python3 scripts/package_public.py --download` to create `dist/website-1.3.0`. Upload **all its contents**, preserving names and relative paths. `index.html` is a simple download page; reuse its links in your own website if desired. Source downloads are adjacent to binary downloads. Nothing is published automatically.

Packages cover Windows x64, Linux x64 (glibc), macOS Apple Silicon (arm64), and macOS Intel (x64). Each contains **Eclipse Temurin Java 21.0.12.1+1**. These are portable packages, not EXE/MSI/DEB/DMG installers. No existing Java is needed and no system runtime/service is installed. Windows ARM, Linux ARM, Alpine/musl and 32-bit systems are not included.

## Running and data

Extract the entire archive into a permanent writable user folder. Do not move only the JAR or Java executable.

- Windows: Extract All, then double-click `start-windows.cmd`.
- Linux: extract the `.tar.gz`, open a terminal there, then `sh start-linux.sh`. A graphical desktop with AWT/X11 libraries and DejaVu Sans for PDFs is needed.
- macOS: extract the `.tar.gz`, open Terminal there, then `sh start-macos.command`. The application distribution is not publisher-signed/notarized; normal macOS security approval may be required. Do not disable system security protections.

Launchers use their own runtime, ignoring PATH/JAVA_HOME. `SIDEKICK_FONT` can specify a custom PDF font as in previous guides. Windows/macOS normally use system Arial; no proprietary font is bundled.

**These portable packages save to `data/sidekick.json` beside the JAR**, unlike earlier native installers. To migrate: Save, close the old app, back up its `data`, then copy it beside the new JAR. There is no merge or automatic synchronization. Deleting the application folder also deletes any data inside it unless backed up elsewhere.

## Licenses and corresponding materials

Application: GPL-3.0-only, Copyright (C) 2026 Dimitrios Diamantis. LICENSE/COPYING.md, individual library LICENSE/NOTICE files and the complete official runtime legal directories are preserved. Third-party licenses remain their own.

The `-all-sources.zip` contains the matching application source/scripts/resources, source JARs/POMs for all six runtime libraries, full upstream source trees for their projects, the full OpenJDK source archive matching the runtimes, Temurin build scripts at the recorded commit, and upstream metadata/configure arguments. Individual files are also in `sources/`. Keep the source download beside the binaries. GitHub may be added later; retain the source corresponding to each distributed version.

Upstream URLs and SHA-256 hashes are pinned in `packaging/*-lock.json`; downloads must match before packaging. `RELEASE-INVENTORY.json` records provenance. Checksums detect corruption, not publisher identity. This is not independent legal certification or a complete security audit.

## Validation boundary

The developer Mac runs the build and automated tests, plus a final macOS arm64 runtime/JAR smoke test using temporary data and PDF output. Windows/Linux/Intel Mac packages receive archive/platform/hash checks, **not native GUI execution tests**. Verify launch, persistence, calculation and PDF on each target before advertising platform acceptance.

No employee data, Git history, IDE settings or caches are included. Old experimental archives elsewhere in `dist` are outside the upload set. Do not upload the entire `dist` directory.

## Reproduction

Build tools: JDK 21+, Maven 3.9+, Python 3.12+ and curl. Network is needed for Maven and pinned downloads; omit `--download` when `dist/release-cache` is complete. Third-party rebuild instructions and build files are within their upstream archives and require their own toolchains; they are not rebuilt by the application's Maven command. Packaging assembles official runtime binaries without altering them. Updating Java requires refreshed metadata/checksums and matching sources, not just a filename change.

## GitHub

Public repository: https://github.com/jimboL3t/managers-sidekick

Release: https://github.com/jimboL3t/managers-sidekick/releases/tag/v1.3.0

Download URL / URL λήψεων:

```text
https://github.com/jimboL3t/managers-sidekick/releases/download/v1.3.0/managers-sidekick-1.3.0-windows-x64.zip
```

Links include the version; update website buttons when releasing. / Τα links περιλαμβάνουν την έκδοση· ενημερώστε τα κουμπιά του site.
