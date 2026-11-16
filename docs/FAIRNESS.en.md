# Fair distribution — 1.7.0

[Ελληνικά](FAIRNESS.md)

## Parallel duties: the recommended setup

In **Schedule planning → Shift mix & weekend balance**, set **Morning / Afternoon / Night** to **5**. Zero disables the objective; it remains the default for compatibility. A visible status message identifies disabled balancing. Settings take effect when you calculate, not when opening the dialog.

Duties are grouped by their start time: morning 06:00–13:59, afternoon 14:00–21:59, night 22:00–05:59 or explicitly marked Night duty. Thus duties starting at 06:00 and 07:00 count together, as do 14:00 and 15:00. In each duty's editor, **Fairness time band** can override this grouping. This override affects shift-mix balancing only; the separate Night duty checkbox still controls night prohibitions, extra night balancing and history-pattern classification. Mark every night duty accordingly.

This balances category totals between employees, not equal numbers of every individual duty. Nor does it require each employee to work equal amounts of morning, afternoon and night: the available assignments determine those totals.

## Controls

- **Morning / Afternoon / Night (0–10):** balances all three categories together; try 5.
- **Extra night weight (0–10):** gives explicitly marked night duties additional priority; 0 is reasonable when shift-mix balancing already covers nights.
- **Weekend weight (0–10):** balances work starting on Saturday or Sunday separately; try 5 if needed.
- **Previous reference months (0–12):** 0 compares this month only. With history, previous burdens are compensated over the whole period, so this month's counts need not be equal. Missing months are skipped. This history does not require pattern approval.
- **Balance shift mix / nights / weekends:** independent employee participation. Excluded employees' assignments are outside the relevant totals, but they can still work.
- **No nights / No weekends:** actual automatic-assignment prohibitions, even when balancing is disabled. Manual assignments remain protected and violations are reported.

All weights default to zero. Historical pattern learning is separate and does not enable fairness; a learned personal preference may itself be unequal. Fairness takes priority over pattern similarity in final ranking.

## Reserve staff and optional duties

Set the employee's **monthly workload** to **Reserve**, with a maximum number of shifts. This is month-specific: selecting it for November does not also select it for December. Restrict skills to the duties they can cover. The reserve has a ceiling, not a quota to fill, and is automatically excluded from fairness comparisons for that month. Locked manual reserve assignments remain preserved even above the ceiling.

In 1.7, reserves fill mandatory gaps only; optional duties do not create extra automatic reserve shifts. Regular employees can still fill optional duties after mandatory coverage. Redundant automatic reserve assignments are removed if regular quota completion covers the same slot. A reserve assignment is a heuristic result, not proof that no alternative could cover it without reserve staff.

## What a fair target means

For each category, the indicative target is:

**Actual category assignments among participants × employee capacity / total participant capacity.**

Shift-mix capacity is eligible dates capped by the employee's monthly shift target, summed over the reference period. Skills, operating days, availability and prohibitions are respected; fixed non-working dates are excluded. Earlier months worked as a reserve are omitted from shift-mix history. Future months are never included. Equal capacity means equal targets; fewer shifts or leave can justify lower targets. A target of 6.4 is indicative, not a mandatory integer quota.

The report shows **this month's [morning, afternoon, night] counts**, followed by **period actual/target** for each category. Reserve/excluded staff are labelled separately.

Night/weekend objectives retain their existing opportunity-based capacity calculation. Historical calculations use current skills, availability and duty classifications, not archived staffing rules. Capacity is an approximation: interactions between rest, adjacent months and locked assignments can prevent reaching a target. Zero-capacity categories have no calculated share; their manual assignments still appear in actual counts.

## Algorithm and limits

The scheduler first compares mandatory gaps, monthly quota deviations, empty days, reserve use and the full-weekend preference. It then compares weighted fairness before paired-rest preferences and history-pattern similarity. Multiple bounded searches include fairness-guided candidates.

A new final improvement pass swaps automatic work assignments between two regular employees on the same date, only when weighted fairness strictly improves. It checks skills, availability, prohibitions, operating days, rest (including adjacent months) and consecutive-work limits. It never changes manual cells, locked days, leave/rest layouts, employee shift totals, daily duty coverage or reserve assignments. This pass cannot repair a case needing a multi-day move, nor guarantee a global optimum. Previously locked violations are reported, not silently corrected.

If coverage remains impossible under the fixed choices, inspect the red dates and validation notes. More historical data alone cannot fix insufficient staff or contradictory locks. Test on a team copy and back up before upgrading.
