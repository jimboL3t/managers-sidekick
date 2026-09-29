# Architecture — 1.0.0

[Ελληνικά](ARCHITECTURE.md)

## Components

- `App`: Swing event-dispatch-thread UI, CardLayout team/calendar navigation; scheduling runs in a SwingWorker while edits are blocked.
- `TeamPicker` / `TeamService`: explicit team selection, creation, deletion and deep copies. Copies receive new team/employee/duty UUIDs; assignments and skills are remapped. Leave IDs may remain, but maps, arrays and cells are independent.
- `Model`: stable UUIDs; months keyed by `YYYY-MM`; cells by `employeeUUID:day`. `Month.holidays` and `lockedDays` hold day numbers; `Post.operatingDays` controls weekly operation.
- `ScheduleTableModel`: individual editing without implicit scheduling; locked days reject edits.
- `CalendarRules`: rest targets, category counts and coverage gaps.
- `Scheduler`: bounded multi-start heuristic and validation.
- `Storage`: UTF-8 JSON schema version 1, temporary writes, previous-file backup, atomic replacement where supported.
- `PdfExporter`: PDFBox A4 landscape schedule, totals and legend; embeds an installed Greek-capable TrueType font.
- `YearSummary` / `YearSummaryPanel`: read-only aggregation of saved assignments without creating missing months.
- `Theme`, `ActionButton`, `GlassPanel`, `MonthTable`, `Logo`: consistent dark Swing rendering, glass-style painted surfaces and shared vector branding. No native blur dependency.
- `I18n`: Greek-default UI and English dictionary in `i18n/en.json`; `Data.language` persists selection. User names are not translated.

## Scheduling

Each run performs 180 attempts. Standard calculation uses seed 42; another combination uses a new seed. Existing manual cells and whole locked days are preserved, including empty cells on those dates. Other days are considered in chronological, reversed and shuffled order. Duties with fewer skilled employees receive priority.

Enabled weekdays require at least one worker, or the configured higher demand. Disabled weekdays require none and receive no automatic supplementary work. Holidays follow the weekday pattern. New assignments respect skills, consecutive-work limits, rest intervals and monthly work budget. Additional permitted assignments can fill personal work targets beyond minimum coverage; rest days then fill the quota with an adjacency preference.

Monthly rest target = Saturdays + Sundays + declared holidays, including holidays on weekends. Work budget = days in month − rest target − other leave. The old `weeklyOff` field remains for JSON compatibility but is unused. Manual exceptions remain with observations rather than being silently replaced.

Scoring prioritizes missing coverage, rest-quota deviation, unassigned cells considered by the fill pass and isolated nonworking days. This is not a complete constraint solver and cannot certify infeasibility or optimality. Different seeds may produce the same result. Fairness does not separately balance nights and weekends.

Rest uses local date/time minutes, not timezone-aware instants. Overnight shifts end the next day. Adjacent-month history is consulted; missing history produces warnings. DST transitions require future timezone-aware handling. Work streaks and rest can cross month boundaries.

## Persistence and compatibility

Legacy per-application leave maps are copied into independent team maps while preserving IDs. Default leave IDs/labels are recorded in `builtInLeaves` for display translation. Older files infer these IDs from matching default names once; a legacy custom type with the same name may be recognized as built-in. Newly created custom types remain distinct. Legacy duties without operating-day data retain daily operation, and old months default to no day locks.

Team creation/copy/deletion restores the in-memory team list on save failure. Deletion requires product confirmation. Clear-manual removes only individual manual cells outside locked dates in the selected month. Language changes rebuild the window after saving the preference.

## PDF and annual summaries

PDF pages show up to 10 people and 10 categories per summary block, with continuation pages. Totals count all assigned days, including categories not on the current continuation page; do not sum repeated totals. Unknown historical categories remain counted. Duties/rest/leaves have independent language-specific codes. Long names may be shortened in tables, with details in the legend. Logo paths are rendered directly through Java2D/PDFBox, not as a bitmap.

Annual summaries count assignments, not attendance or hours. Overnight shifts belong to their start date; empty dates are excluded. Current names and rules apply to historical displays.

## Known limits and next steps

One assignment per employee/date; no split shifts, date-specific coverage override, employment start/end dates, employee archival or transfers. No rule snapshots, audit trail, undo, authentication, encryption, cloud synchronization or inter-process file locking. Use one instance per data folder. Priorities include restore points, history snapshots, availability, fairness metrics, a solver evaluation, native installers and a bundled licensed font. See [improvement assessment](IMPROVEMENTS.en.md).
