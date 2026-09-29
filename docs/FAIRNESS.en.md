# Fairer distribution — 1.2.0

[Ελληνικά](FAIRNESS.md)

## Setup

1. In **Duties**, mark each relevant duty as **Night duty**. Classification is explicit, not inferred from its name or hours. Existing duties initially have no night designation.
2. Open **Fair distribution** in the calendar toolbar.
3. Set separate night/weekend weights from 0 to 10. **0 disables that objective**; try 5/5 initially. Weights are priorities, not shift counts or maximum limits.
4. Choose 0–12 previous reference months. Zero means the selected month only; two means that month plus the preceding two calendar months. Previous months without saved assignments are omitted and reported.
5. Choose each employee's participation and prohibitions, confirm, then calculate or request another combination. Editing settings saves them without recalculating assignments.

Both weights default to zero for new and existing teams. Employees initially participate in both objectives and have no prohibitions.

## Participation versus prohibition

| Employee setting | Effect |
| --- | --- |
| Balance nights | Include the employee in night targets and comparisons |
| Balance weekends | Include the employee in weekend targets and comparisons |
| No nights | Prohibit automatic assignments to explicitly designated night duties |
| No weekends | Prohibit automatic work starting on Saturday/Sunday |

Exclusion from balancing is not a work prohibition: excluded staff can still receive eligible shifts, but their assignments are outside the participant totals and the algorithm does not equalize their counts. Participation can differ between objectives. Prohibitions remain active even with weights zero. Manual assignments and whole-day locks remain preserved, with observations if they violate a prohibition. Skills and availability are separate settings.

## Counting and proportional targets

The unit is one assignment on its **start date**, not hours or complete free weekends. Saturday plus Sunday work counts as two. A Friday night ending Saturday counts as Friday night, not weekend work; a Saturday night counts in both categories. Holidays have no separate fairness objective in this release.

For each participant, opportunities are calendar dates in the reference period with at least one eligible duty in that category: operating that day, permitted by skills and availability, and not prohibited. Assigned non-rest leave removes a date. Multiple eligible duties on one date count as one opportunity. Overnight availability checks the following date where applicable.

**Indicative target = actual category assignments among participants × employee opportunities / total participant opportunities.**

For example, two employees with 20 and 10 opportunities share 12 actual nights proportionally as 8 and 4, rather than 6 each. Equal opportunities give equal targets. Previous-month assignments count toward the cumulative burden, encouraging compensation in the current month without changing earlier schedules.

Opportunities approximate capacity, not full feasibility. They do not model contracts, part-time hours, employment start dates or every interaction of rest and locks. Those hard rules are checked separately when assigning work. Historical counting uses current duty classifications, skills and availability, not archived snapshots. Review results after major staffing/rule changes. A participant with zero opportunities has no calculated target and is identified in the report.

## Scheduling priorities

Every new automatic assignment still respects skills, availability, operating days, prohibitions, rest intervals and consecutive-work limits. Individual manual assignments and locked days are preserved.

Candidate schedules are compared lexicographically: fewer uncovered requirements, smaller rest-quota deviation, fewer unfilled editable cells, then the combined fairness and paired-rest preference score. Fairness penalizes squared differences from proportional targets, multiplied by each category's weight. Higher weights emphasize balancing relative to paired rest and the other objective.

With an enabled objective the scheduler keeps 180 baseline attempts and adds 180 guided attempts, so calculations may take longer. Baseline candidates remain available: fairness cannot select worse coverage, quota or completeness merely for a better preference score. This is still a bounded heuristic, not proof of optimality or a hard cap on nights. Exact equality may be impossible.

## Reviewing results

After calculation the observations area shows **Before / After** when an objective is enabled. Reopen **Fair distribution** for current actual/target counts, configured weights, exclusions and missing-history notices. Dialog metrics use the settings saved when the dialog opened; confirm changes and reopen to refresh them.

Fractional targets are comparison values, not instructions to schedule fractional shifts. Targets change if total assigned work changes, so compare coverage/completeness first. Before/after text is temporary; History stores the pre-calculation snapshot including fairness settings. Calculations still save automatically: use a test-copy team for experiments.
