# Administrator guide — 1.0.0

[New in 1.2: configurable night/weekend balancing. Administrator guide](FAIRNESS.en.md).

**New in 1.1.0:** [History, availability and native installation](RELEASE-1.1.en.md). Native packages use a different data location; read before migrating.

[Ελληνικά](ADMIN_GUIDE.md) · [Installation](DISTRIBUTION.en.md) · [Backup](BACKUP.en.md)

## Teams and initial setup

Start on **Work teams**, select a team and open its calendar (button, double-click or Enter). Return using the teams button. **New team** creates an independent team with its own employees, duties, leave types, rules and months.

**Copy for testing** duplicates the entire team and its history, assignments, skills, holidays and locks with independent data. The copy is marked as a test copy. Changes do not affect the original and cannot automatically merge back. Deleting a team requires confirmation and removes its contents, without affecting other teams or copies. There is no in-app undo: keep a backup.

1. Create a team and open its calendar.
2. Create duties with a name and start/end time in `HH:mm`. An end earlier than the start means the following day; equal times are rejected. Select operating weekdays and required staff per selected day (minimum 1). All seven days are initially enabled. **Every day** and **Weekdays only** are shortcuts; arbitrary combinations such as Monday/Wednesday are supported. All days may be disabled. Holidays do not override the weekday pattern.
3. Create employees and tick their permitted duties. No checked skills means no automatically assigned work. The **Skills** matrix edits everyone's permissions together.
4. In settings choose allowed leave types, maximum consecutive workdays (default 5), minimum rest between shifts (default 11 hours), and the preference for adjacent rest days.

Rest is always available. Default leave types include annual, medical, sick, parental, student and unpaid leave. A new leave type belongs only to the current team. Changes to duties, hours, names or rules also affect the display and validation of historical months; export important historical PDFs first.

## Planning a month

The calendar initially selects the current month. Use month/year selectors, arrows or the next-month shortcut to view future and historical months. Double-click a cell to select a duty, leave or rest. Changes save immediately without recalculating other cells. A star marks a manual assignment, preserved by subsequent calculations. Selecting an empty cell removes the assignment and its individual lock.

Calculate only when ready, using **Calculate shifts**. The selected month is the month being calculated. Changing a cell, holiday or operating day does not automatically recalculate. The scheduler automatically assigns work and rest; other leave is entered manually.

Manual exceptions outside skills or operating days remain possible and are reported. **Unlock cell** lets the next calculation replace that assignment. **Clear manual entries** requires confirmation and removes manual assignments in the selected team/month, except on locked days; automatic assignments, holidays and other months remain. Then calculate again if needed.

## Rest targets, holidays and coverage

Every employee's monthly rest target is the number of Saturdays plus Sundays plus declared holiday dates in that month. Four Saturdays + four Sundays + two holidays means 10 rest days. A holiday adds one even when it falls on a weekend, regardless of whether the employee works that day. Other leave does not count as rest. Holiday dates are stored per team/month.

Work target = days in month − rest target − other assigned leave. September 2026 without holidays or leave means 30 − 8 = 22 shifts. Actual/target rest and work counts appear next to employees. There is no separate hard two-rest-days-per-calendar-week quota; adjacent rest days are a preference.

Coverage is a minimum: extra staff may be assigned to an operating duty to reach monthly work targets. Closed duties receive no automatic assignments, including supplementary shifts. Four people × 22 shifts provide 88 assignments while three daily duties need 90: at least two assignments are missing even before other constraints.

Weekend backgrounds are purple; holidays are green and take precedence. Red date text/underlining means an operating duty lacks staff. Partial schedules can still be saved and exported. The observations panel reports coverage, empty cells and target deviations without blocking popups. **Validate** does not change assignments. Review staffing, skills, manual choices and adjacent-month history when issues remain. The heuristic cannot guarantee finding every feasible schedule.

## Whole-day locks and alternative schedules

Enable **Day locks**, then click a date header to lock or unlock the entire column, including empty cells and automatic assignments. Locked days also prevent manual edits; unlock first. Locks persist per team/month. The checkbox cannot hide the controls while the displayed month has locked dates.

**Another combination** searches again with different randomness, preserving locked days and individual manual choices elsewhere. A different result is not guaranteed and the application does not enumerate all solutions. Results are saved like normal calculations; use a test copy to compare scenarios. Clearing manual entries does not alter locked dates.

## Language, annual overview and PDF

Select **Ελληνικά / English** at the top right of the team screen. Greek is the initial default; the choice is remembered. UI labels, built-in leave labels, weekdays, months and future PDFs follow the selected language. User-entered names remain unchanged. Existing PDFs are not rewritten.

Select a team and open **Annual overview** to choose a year and see months with assignments, work/rest/leave totals and an employee breakdown by category. This is read-only, counts saved assignments rather than attendance, excludes empty days and attributes overnight shifts to their start date. Current names/categories are used.

**Save** is purple and **Export PDF** green. Export creates A4 landscape pages with the monthly schedule, dates above weekday abbreviations, a category breakdown and a legend with full names/times. Greek codes are Υ1… for duties, Ρ for rest and Α1… for leave; English codes are D1…, R and L1…. The asterisk marks individual manual assignments. Open the PDF in a viewer to print.

The final total counts all assigned days (work, rest and leave), not hours or only workdays. Empty days are excluded. More than 10 employees or 10 categories produce continuation pages; the full monthly total repeats on category continuation pages and must not be added again. Long names may be shortened in tables; the legend provides details. The M/S logo is included as `brand/logo.svg`.

## Saving and operating limits

Save and many editing actions write all teams to the local JSON file. Use one application instance per data folder. There is no login, cloud synchronization or concurrent editing. See [backup and restore](BACKUP.en.md) before upgrading or deleting data.

The 1.0.0 release includes day locks and the glass-style interface on `main`. The earlier baseline remains tagged `v0.8.0`. With a clean/committed working tree, `git switch -c maintenance/0.8 v0.8.0` creates a branch from that baseline. Switching code does not restore data; use a matching backup in a separate folder. Version 0.8.0 does not understand day locks.
