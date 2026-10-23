# History patterns — 1.6.0

[Ελληνικά](PATTERNS.md)

Patterns are optional and disabled by default. Processing is local; employee data is not sent to an external service.

## Finding settings

- **Team & duties:** duties, leave types, change history.
- **Employees:** employee details and monthly targets, eligible duties, availability.
- **Schedule planning:** work limits/rest days, night/weekend distribution, history patterns.

Calculation, validation, another combination, Save and PDF remain directly accessible. Work limits, rest-day preferences and allowed leave have separate tabs. Two consecutive rest days may occur on any weekdays; a Saturday/Sunday pair specifically requests a complete weekend off once per month. Both are preferences, not guarantees.

## Getting started

1. Enter past schedules through the calendar. Review them as finalized examples worth following.
2. Select the month to calculate. Open **Schedule planning → Patterns from previous months**.
3. Select approved examples under Training months. Only months before the target month appear.
4. Choose a 1–12 calendar-month reference window (default 6). Only approved months inside that window contribute.
5. Under Employee participation, uncheck anyone to exclude from pattern preferences. Their skills and permitted duties remain unchanged.
6. Click **What does the app recognize?** to see sample counts, time-band percentages and the most frequent triple when supported by enough data.
7. Enable patterns, choose Low/Medium/High influence (default Medium), and accept. Each employee needs at least 5 eligible historical shifts.
8. Click **Calculate shifts**. Changing preferences alone does not rearrange assignments.

## What is learned

The current implementation counts per-employee start-time bands: morning 06:00–13:59, afternoon 14:00–21:59, night 22:00–05:59. Explicitly marked night duties always count as nights. It learns frequencies and adjacent-day pairs/triples, not duty names or arbitrary weekly sequences. Transitions need at least 3 observations for the matching context; a morning → morning → afternoon sequence can therefore influence assignments, but an entire week is not guaranteed to be reproduced.

Leave, rest days, empty cells and declared holidays are excluded and break sequences. Training transitions do not bridge month boundaries. Target-month holidays do not receive pattern penalties. Day of week is not a separate learned feature in this first implementation.

## Priorities and approvals

Manual/day locks, skills, availability, rest and consecutive-day limits remain protected. Mandatory coverage and monthly targets take precedence. With patterns enabled, final candidate ranking prioritizes configured fairness over historical similarity. Influence is a preference weight, not a compliance percentage. The bounded heuristic still requires administrator review.

Approval stores an independent snapshot of shift time bands. Later changes to schedules or duty times do not silently retrain it. Select **Refresh selected snapshots from current historical schedules** and accept to explicitly replace approved examples. Uncheck a month and accept to revoke its approval. Cancel leaves the dialog's settings unchanged.

Newly generated months are never approved automatically. Approve each finalized month yourself to avoid reinforcing the app's own earlier choices. Settings and snapshots live in the same JSON and are included in data-folder backups. Team copies retain independently remapped training data and employee participation.
