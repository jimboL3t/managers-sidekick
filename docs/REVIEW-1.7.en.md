# 1.7.0 validation and setup

[Ελληνικά](REVIEW-1.7.md)

Version 1.7.0 is available from GitHub Releases. Build with `mvn verify`, then run `java -jar target/managers-sidekick-1.7.0.jar`. Use `-Dsidekick.dataDir=/path/to/test-data` before `-jar` to isolate a test database.

## Changes to review

1. Open **Schedule planning → Shift mix & weekend balance**. Set shift-mix weight to 5 and reference months to 0. Keep extra night weight at 0 initially to avoid giving nights extra emphasis; optionally enable weekend balance separately.
2. Check each duty's category (automatic groups 06:00/07:00, 14:00/15:00, 22:00/23:00 correctly). Mark night duties explicitly as before.
3. Set the specialist's monthly workload to **Reserve**, with the desired maximum, and retain only their eligible duty. This must be set for the month being calculated.
4. Calculate. Compare current-month category counts and period targets in the balance dialog and the before/after report. Locked requests remain unchanged.
5. Only then try prior reference months, or history patterns. Balancing a multi-month period can intentionally give unequal counts in the current month.

See [Fairness](FAIRNESS.en.md) for priorities, capacity approximation, category overrides and local-search limits.

## Case investigation (anonymous counts)

The supplied local trial file had balancing weights zero, patterns disabled, no December reserve plan, and all six duties mandatory. The screenshot alone could not establish these settings.

For a controlled comparison, the same team, seed, manual choices and reserve ceiling of 22 were used with 1.6.0 and 1.7.0. The new shift-mix objective used weight 5 and no reference history. Among nine regular staff:

| Category | 1.6.0 range | 1.7.0 range |
|---|---:|---:|
| Morning | 0–13 | 6–9 |
| Afternoon | 2–12 | 6–10 |
| Night | 2–13 | 3–7 |

Regular monthly totals remain 22, 19, 22, 22, 22, 17, 22, 22, 22; leave explains the reduced totals. Reserve work is 12 in both runs. There are no new safety violations. Three dates remain under-covered in both:

- December 26/27: four fixed non-working assignments leave six people for seven required positions.
- December 31: four fixed non-working assignments and two fixed assignments to the same duty leave too few people for the other five duties.

These results are one case, not a general guarantee. The final repair phase preserves daily duty counts, workload and non-working-day layouts by construction. The broader search remains heuristic and may select different coverage patterns for other inputs. User data and screenshots are not included in source or distributable packages.
