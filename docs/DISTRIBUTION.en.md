# Installation and distribution — 1.0.0

[Ελληνικά](DISTRIBUTION.md) · [User guide](ADMIN_GUIDE.en.md) · [Backup](BACKUP.en.md)

## What to send

Send **`dist/managers-sidekick-1.0.0-test.zip`**, optionally with its `.sha256` file. Extract the entire ZIP on the destination computer. **The repository's `packaging` folder alone is not runnable:** it contains launcher templates and instructions, not the application JAR.

The ZIP includes `managers-sidekick.jar` with runtime libraries, Windows/Linux launchers, Greek/English documentation, branding and dependency licenses. It excludes Java, employee data, source code, Git, IDE settings and Maven caches. This is a portable distribution requiring Java, not a native installer with a bundled runtime. The `-test` filename suffix is retained for compatibility with the packaging script.

## Requirements and launch

| System | Requirements | Launch |
| --- | --- | --- |
| Windows | Desktop Java 21+ JRE or JDK for your CPU; Arial for Greek PDFs | Double-click `start-windows.cmd` |
| Linux | Graphical desktop; Java 21+ with Swing/AWT, not only headless Java; DejaVu Sans | `sh start-linux.sh` |
| macOS | Java 21+ for Apple Silicon or Intel as appropriate; Arial | `sh start-linux.sh` in Terminal |

No Maven, Python, Git, database, server or IDE is needed on the destination PC. After runtime/font installation the app works offline. Extract into a writable user folder, such as Documents/ManagersSidekick, rather than Program Files. Do not run directly inside the ZIP.

On Windows, install a desktop Java 21+ distribution for the machine, for example [Temurin](https://adoptium.net/temurin/releases/?version=21). Enable PATH integration where available. Open a new Command Prompt and run `java -version`; Java 8, 11 and 17 are insufficient. The launcher prefers a valid `JAVA_HOME/bin/java` over PATH; check both if an older Java is still used. See the [vendor's Windows instructions](https://adoptium.net/installation/windows/).

On Linux, use your distribution's desktop Java 21+ package and Greek-capable fonts, or the [vendor's Linux instructions](https://adoptium.net/installation/linux/). A headless server or SSH session without a display cannot run the GUI. Run the script without sudo. macOS uses the same shell launcher; choose a runtime matching the Mac's architecture.

## PDF fonts

The application looks for Arial on Windows/macOS and `/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf` on Linux. If unavailable, supply a Greek-capable TrueType font:

```sh
SIDEKICK_FONT="/absolute/path/GreekFont.ttf" sh start-linux.sh
```

Windows Command Prompt:

```bat
set "SIDEKICK_FONT=C:\Fonts\GreekFont.ttf"
start-windows.cmd
```

Gson and PDFBox are already included in the JAR. Print exported files using a PDF viewer.

## Moving existing schedules or upgrading

Save and close the old application. Back up its entire `data` folder, then copy that folder next to the extracted JAR on the destination. Keep a backup of any destination data before replacement. Without `data`, the app starts with no teams. There is no synchronization or automatic merge between computers.

For upgrades, extract to a new folder and copy your data while the application is closed. Never run two instances against the same data folder. See [full backup/restore instructions](BACKUP.en.md).

## Troubleshooting

- **Java not found:** install Java, check PATH/JAVA_HOME, and open a new terminal.
- **UnsupportedClassVersionError:** an older Java is being selected.
- **HeadlessException / display connection error:** use a graphical desktop and a desktop runtime.
- **Unable to access jarfile:** extract the whole ZIP and keep the JAR beside the launchers.
- **Permission denied when saving:** move the package to a writable user folder.
- **Unexpectedly empty team list:** check the `data` folder beside the JAR and use the launcher, which sets the working directory consistently.
- **PDF font missing:** install a suitable font or set `SIDEKICK_FONT`.

## Building and verifying a package

Only the build computer needs JDK 21+, Maven 3.9+ and Python 3.9+. From the repository run:

```sh
python3 scripts/package_release.py
```

This runs `mvn clean verify`, checks the executable JAR and dependencies, and creates the ZIP and checksum under `dist`. The file allowlist excludes personal data. Update the license allowlist if runtime dependencies change.

Linux checksum verification:

```sh
sha256sum -c managers-sidekick-1.0.0-test.zip.sha256
```

Windows PowerShell:

```powershell
Get-FileHash .\managers-sidekick-1.0.0-test.zip -Algorithm SHA256
```

Compare with the supplied checksum. The internal `SHA256SUMS.txt` covers archive contents. Hashes check transfer integrity; they are not a digital signature or publisher authentication. Native installers and bundled Java are future work. Platform-specific desktop testing remains necessary.
