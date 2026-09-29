# Data, backup and restore — 1.0.0

[Ελληνικά](BACKUP.md) · [Installation](DISTRIBUTION.en.md)

## Storage location

All teams, employees, skills, duties, leave types, rules, monthly assignments, holidays, locks and language preference are stored locally in **`data/sidekick.json`**. No cloud account or server is involved.

When launched with the supplied scripts, `data` is beside `managers-sidekick.jar` in the extracted application folder. With a manual `java -jar ...` command it is relative to the terminal's current working directory, which may differ from the JAR folder. Prefer the launchers and a fixed writable folder. Never run two instances against the same data folder.

**Save** writes all teams; many edits and calculations also save automatically. Each save copies the previous file to `sidekick.json.bak`, replacing the prior backup. This is only one previous save, not a version history or independent backup. `.tmp` is a temporary write file.

PDFs are written to the location chosen during export. They are not automatically included in `data` and cannot be imported as editable schedules.

## Making a backup

1. Click Save and close the application.
2. Copy the entire `data` folder into a dated backup folder, such as `Sidekick-backup-2026-09-29`.
3. Keep a copy on a separate drive or suitably protected storage. These files contain employee and leave information.
4. Copy exported PDFs separately if needed, and retain the application ZIP used with the data.

Back up before upgrades, bulk edits and team deletion. Keep multiple dated copies. A test-copy team is stored in the same JSON file and is not protection against file or disk loss.

## Restoring or moving to another computer

1. Close the application on both computers.
2. Preserve a separate copy of current destination data before replacing it.
3. Copy the backed-up `data` folder beside the JAR of the same or a compatible newer version.
4. Launch using its script and verify teams, months, manual assignments and day locks.

Restoration replaces **all teams**; it does not merge them. Periodically test restoring a backup into a separate application folder.

To recover the previous save, first preserve both original files while the app is closed, then copy `sidekick.json.bak` to `sidekick.json`. Keep the originals until you have verified recovery.

Git and release ZIPs do not include personal data. Reverting application code does not restore schedules. Version 0.8.0 does not understand day locks: use a matching backup rather than sharing the current data folder with that version.
