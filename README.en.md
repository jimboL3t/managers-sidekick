# Manager’s Sidekick — 1.0.0

[Ελληνικά](README.md)

A local Java Swing application for team shift planning, employee skills, leave, holidays, locked assignments and A4 landscape PDF export. Greek is the default; English can be selected on the team screen. User-entered names are preserved.

## Features

Independent teams and test copies; month navigation; duty operating weekdays and daily staffing demand; employee skill matrix; manual assignments preserved during calculation; whole-day locks including blank cells; alternative schedule search; configurable consecutive-work/rest rules; monthly rest and work targets; nonblocking coverage observations; annual summaries; PDF schedules, category totals and legends; dark glass-style controls and M/S branding.

Manual cell changes do not trigger recalculation. The bounded heuristic does not guarantee every feasible solution or a different result on each attempt. This is a single-administrator local app, with no cloud synchronization, login or concurrent editing.

## Use on another computer

Send **`dist/managers-sidekick-1.0.0-test.zip`**, extract it completely and install desktop Java 21+ for that OS/CPU. Windows: `start-windows.cmd`. Linux/macOS: `sh start-linux.sh`. No Maven, Python or IDE is needed on the destination. The `packaging` source folder alone is insufficient: it does not contain the application JAR.

The ZIP includes libraries, launchers, bilingual guides and licenses, but not Java or employee data. To transfer existing schedules, save/close the app and copy its `data` folder beside the extracted JAR. Keep a separate backup first.

## Documentation

- [Installation and troubleshooting](docs/DISTRIBUTION.en.md)
- [Administrator guide](docs/ADMIN_GUIDE.en.md)
- [Data location, backup and restore](docs/BACKUP.en.md)
- [Architecture and limits](docs/ARCHITECTURE.en.md)
- [Validation and desktop checklist](docs/VALIDATION.en.md)
- [Proposed improvements and market references](docs/IMPROVEMENTS.en.md)

## Build

Requires JDK 21+ and Maven 3.9+; packaging also needs Python 3.9+.

```sh
mvn clean verify
java -jar target/managers-sidekick-1.0.0.jar
python3 scripts/package_release.py
```

Run from the repository root. Maven uses `.maven-repository`. Data is written to `data/sidekick.json` relative to the working directory, with the previous save in `.bak`. Launchers consistently use their own folder. Only run one instance per data folder.

Runtime dependencies: Gson 2.14.0 and PDFBox 3.0.7 (Apache-2.0). Tests use JUnit Jupiter 5.14.2 (EPL-2.0). Swing is included in Java. PDF needs installed Arial (Windows/macOS) or DejaVu Sans (Linux), or a custom Greek-capable TrueType font via `SIDEKICK_FONT` in launchers / `-Dsidekick.font=/path/font.ttf` in a direct Java command.

## Release history

1.0.0 adds glass-style controls and backup documentation, incorporating day locks and alternative calculations developed on `feature/day-locks`. Earlier releases introduced weekday duty operation (0.8), language selection (0.7), vector branding and improved PDF totals (0.6), annual summaries (0.5), monthly work targets (0.4), independent teams/copies (0.3) and holiday/rest targets (0.2). `v0.8.0` remains available as a source baseline. Git does not back up personal schedules.

Review observations before publishing a real schedule. Default rules implement project requirements and do not constitute legal-compliance certification.
