## Desktop review 1.4.0

61 Java tests passed. Bundled Temurin 21.0.12.1 on macOS arm64 passed headless Swing, scheduling, history, storage and PDF smoke checks. The native app launched using isolated sample data. The Windows EXE was inspected as an x64 GUI executable with an embedded icon and only KERNEL32/USER32 imports; it was not executed on Windows. See [Desktop packages](DESKTOP.en.md). No release was published.

# Validation — 1.2.0

[Ελληνικά](VALIDATION.md)

Run `mvn clean verify` or `python3 scripts/package_release.py`. The current suite contains **46 tests**. Packaging builds the executable JAR, verifies its entry point and embedded libraries, and produces an allowlisted ZIP plus SHA-256 checksums. Personal data and IDE settings are excluded.

Coverage includes scheduling/rest constraints, monthly work/rest targets, locked manual choices and reset, overnight and adjacent-month checks, leap months, holidays and coverage, storage compatibility, independent team copies, table editing, annual aggregation, dark controls, PDF layout/pagination/totals, language persistence and untranslated custom names, weekday duty operation, and whole-day locks across multiple random seeds.

Headless preview artifacts are generated under `target`, including team-picker, control, logo and PDF previews. The PDF tests verify readable text and total positioning. GUI rendering previews are useful checks, but are not a substitute for native desktop testing.

## Manual acceptance checklist

On macOS, Windows and Linux with desktop Java 21+:

1. Extract the full ZIP into a writable folder, launch with its script, create a team and restart to check persistence.
2. Create duties with overnight times and weekday-only operation, employees with restricted skills and custom leave. Verify dropdown readability, keyboard focus and window sizing.
3. Enter manual assignments, calculate and confirm they remain unchanged. Validate coverage, rest/work counts and neighboring-month observations.
4. Lock a complete and an empty date. Try another combination and confirm all locked cells and blanks remain unchanged. Unlock before editing.
5. Switch Greek/English; check menus, months, built-in leave and PDF labels while custom names remain unchanged.
6. Export/print a 31-day month, long names, more than 10 employees and more than 10 categories; verify continuation totals are not double-counted.
7. Check annual summaries, independent team copies, deletion confirmation, backup and restoration in a separate folder.
8. Confirm missing fonts, older Java, missing JAR and unwritable folders produce actionable failures.

Native cross-platform acceptance is still pending. The heuristic's success is not a proof of optimality, and its failure is not proof that no valid schedule exists. Project defaults are scheduling requirements, not certification of legal compliance.

## 1.1 checks

Five additional tests cover frozen snapshots, independent restoration with new IDs, availability overrides and overnight boundaries, preserved manual exceptions, legacy migration, and native/portable data paths. Native packaging was exercised on macOS Apple Silicon. Windows/Linux install and upgrade acceptance is still pending.

## Fairness 1.2

Four tests verify measurable balancing improvement without loss of coverage, monthly work completion, hard restrictions and preserved manual exceptions, participation versus prohibition, historical targets, read-only reporting, availability-weighted opportunity counts and explicit night classification.

## Website packages 1.2.1

`scripts/verify_public.py` verifies archive contents, regular-file hashes, licenses and PE/ELF/Mach-O target architectures. On macOS arm64 it executes the distributed Java/JAR for headless Swing, scheduling, history, JSON and PDF using temporary data. Results are written to the upload directory VALIDATION.json. Windows/Linux/Intel Mac native GUI acceptance remains pending.

Scheduling update: 64 tests passed, including mandatory-duty priority, optional coverage/persistence, the ten-day limit, and a two-page bilingual quick-start PDF.
