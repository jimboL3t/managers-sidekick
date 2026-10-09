# Version 1.4.0 — desktop packages

[Ελληνικά](DESKTOP.md)

Version 1.4.0 is downloadable from GitHub. Full desktop installation/upgrade acceptance on Windows, Linux and Mac remains in progress.

## Windows 64-bit: portable with an application icon

Extract the **entire** `managers-sidekick-1.4.0-windows-x64.zip` into a writable folder, such as Documents. Open **managersSidekick.exe** (MS icon); optionally create a desktop shortcut. No installer or separate Java installation is required. Keep the EXE, JAR and `runtime` directory together.

Data lives in **`data/sidekick.json` beside the EXE**, including when launched through a shortcut with a different working directory. To upgrade, close the application, back up the old `data` folder, extract the new version into a separate directory, and copy the entire old `data` folder there **before first launch**. Keep the old folder until you verify your data.

## macOS: DMG and Applications

Open the DMG for your Mac architecture and drag `ManagersSidekick.app` to Applications. Launch it from Applications. The local builder produces a DMG for the build host: Apple Silicon (arm64) or Intel (x64). Java is included.

Data lives in **`~/ManagersSidekick/data/sidekick.json`**, outside the application. Use Finder → Go → Go to Folder → `~/ManagersSidekick/data` to locate it.

For subsequent upgrades, close the application, back up `data`, and replace only the `.app` in Applications. The new application loads the existing JSON from the same location. Do not run two versions against the same database simultaneously.

### First migration from an older portable Mac package

1. Close both versions.
2. Back up the old portable `data` directory to a safe location.
3. If `~/ManagersSidekick/data` does not already contain data, copy the old `data` folder there, including `sidekick.json`.
4. If the destination already contains data, back it up separately and choose which database to retain. There is **no automatic merge**. Do not overwrite a database without a backup.
5. Open the new application and check teams, months and history. The original folder is never deleted automatically.

## Backups and restore

With the app closed, copy the entire active `data` directory to a dated folder on another drive/location. To restore, close the app and copy the backup back to the same data location. The automatic `sidekick.json.bak` from the preceding save is not a substitute for independent backups. Distribution packages contain no personal data.

These review packages have no Windows signing certificate or Apple notarization. The OS may request approval on first launch. Do not disable system protections.

## Build and validation

On macOS with a JDK, Maven, Python 3.12+ and MinGW (`brew install mingw-w64`):

```sh
python3 scripts/package_desktop.py
```

The script verifies existing runtime/source archives in `dist/release-cache`, runs Java tests and produces `dist/release-1.4.0`. It does not commit, upload, or change the website. The currently locked DMG build targets Apple Silicon only. Bundled Java comes from `packaging/runtime-lock.json`, not from the build machine's JDK. Supply the matching full source bundle alongside a future public distribution.

Windows acceptance requires execution on Windows: launch, save, restart, PDF export, and a shortcut with a different working directory. Inspecting the EXE on macOS does not replace Windows execution testing.

## Linux x64

Extract the entire tar.gz and run `sh start-linux.sh` from its folder in a graphical desktop session. Java is bundled; desktop libraries and a Greek-capable font (e.g. DejaVu Sans) must be present. Data remains in the portable `data` folder. Back up and copy that folder into the new package before upgrading.

All packages include `quickStartGuide.pdf`; it is also a separate release download. macOS includes it inside the application bundle. Download all-sources alongside binaries when redistributing. ARM Linux is not included; these Linux binaries require x64.
