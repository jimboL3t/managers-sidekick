# Proposed improvements

[Ελληνικά](IMPROVEMENTS.md)

This is a proposal, not a list of implemented features. The comparison uses vendor documentation rather than hands-on product testing; it does not evaluate pricing or endorse a purchase.

## Market reference points

[When I Work](https://wheniwork.com/features/employee-scheduling-software) describes availability, time-off requests, qualifications and preferences in its scheduling workflow. Its [shift coverage guide](https://help.wheniwork.com/articles/getting-your-shifts-covered/) also describes employee shift swap/drop requests. These suggest useful future availability and request workflows for Sidekick.

[Deputy's scheduling page](https://www.deputy.com/features/scheduling-software) describes priorities such as equal spread of hours and cost when assigning employees. Its [demand forecasting page](https://www.deputy.com/features/demand-forecasting) describes using business data to estimate staffing needs. These provide reference points for fairness and date-specific coverage; full business forecasting would be excessive for Sidekick's current scope.

## Recommended order for Sidekick

| Priority | Proposal | Benefit and scope |
| --- | --- | --- |
| 1 | Restore points, undo and preview-before-apply | Recover from recalculation and compare alternatives without overwriting the working schedule immediately. Add backup/restore buttons and dated retention. |
| 1 | Historical snapshots and change log | Keep published months tied to the duty times/rules used at publication; record assignment changes. |
| 1 | Feasibility explanations | Show staffing arithmetic and distinguish missing skills, unavailable employees, rest conflicts and locks; the current heuristic must not claim infeasibility. |
| 2 | Availability and employment dates | Separate “cannot work” from preferred duties/days and leave; support part-time targets and joining/leaving dates. |
| 2 | Fairness metrics and configurable goals | Compare nights, weekends, holidays and hours, including previous months; show the tradeoff with coverage. |
| 2 | Native installers with Java/font bundled | Remove manual runtime/font setup; build and test separately for supported OS/CPU combinations. |
| 3 | Templates and date-specific demand | Copy recurring patterns while allowing one-off closures, holidays or increased coverage. |
| 3 | CSV/Excel import/export and calendar export | Reduce repeated typing and simplify sharing; define import validation and conflict handling first. |
| Later | Employee requests, approved swaps and notifications | Requires identity, permissions, synchronization and a server/mobile or web interface; treat it as a separate architectural phase. |

Start with restore points and preview/accept/reject for “Another combination”, then historical snapshots and availability. Evaluate a full constraint solver against realistic datasets before choosing a library. Preserve the offline workflow and manual/day locks. These priorities are an engineering assessment of the current app, not vendor claims or promised release dates.
