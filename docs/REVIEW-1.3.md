# Review 1.3.0 / Έλεγχος 1.3.0

Version 1.3.0 desktop review checklist.
Λίστα ελέγχου της έκδοσης 1.3.0.

```sh
java -jar target/managers-sidekick-1.3.0.jar
```

- Display settings on the team picker: try 100%, 150%, 200%, then return to 100%. Reopen the app to check persistence.
- Open a team; scroll days horizontally and employees vertically. Names should remain visible and aligned. Edit a day near the end of the month; confirm it updates the intended date.
- Compare alternating row colors on weekdays, weekends and holidays.
- Enable the full-weekend preference in team settings and calculate. Check monthly totals, coverage, locked assignments and observations.

Οι χειροκίνητοι έλεγχοι σε πραγματικό macOS/Windows/Linux desktop παραμένουν απαραίτητοι. Οι αυτοματοποιημένες προεπισκοπήσεις είναι στο `target/display-100.png`, `target/display-150.png`, `target/display-200.png`.

See [Greek guide](ADMIN_GUIDE.md) / [English guide](ADMIN_GUIDE.en.md) for exact weekend semantics and limitations.

## Monthly workloads / Μηνιαίοι στόχοι

Employees → select employee → monthly workload. Try a reduced target, an increased target and a reserve ceiling; calculate and check coverage, totals and locked cells. Switch month to verify isolation. A reserve's unused days remain intentionally blank. Sources of the new checks: `WorkloadTest`.
