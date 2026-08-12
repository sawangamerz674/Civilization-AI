# Performance Guide

Civilization AI was built around one non-negotiable constraint stated in
Phase 1: **never freeze the server, regardless of population.** Every
phase since has added a new subsystem without relaxing that constraint.
This page explains how, concretely.

## The three budgets

1. **AI evaluation budget** (`ai.TaskScheduler`, every tick). Each
   civilization is only re-evaluated once its interval has elapsed
   (`simulationTicksPerEvaluation` if a player is nearby, the much longer
   `inactiveVillageSimulationInterval` otherwise — this is LOD simulation).
   Within a tick, civilizations are only evaluated until the running total
   of their populations would exceed `maxVillagersProcessedPerTick`; a
   civilization skipped this way is retried on a later tick, not dropped.

2. **Construction block-placement budget** (`world.ConstructionExecutor`,
   every tick, unconditionally). A single `maxBlockPlacementsPerTick`
   budget is split fairly across every active building and road, across
   every civilization, across every dimension. A job whose chunk isn't
   loaded is skipped without consuming any of its queue or the budget.

3. **"One thing per cycle" pacing.** Independent of the two hard budgets
   above, nearly every planner in this mod (`VillageGovernor`,
   `BuildingPlanner`, `RoadPlanner`, `IncidentTriggerService`,
   `RelationshipSimulator`, `MigrationService`) acts on at most one thing
   per civilization per evaluation cycle by design, not just as a
   consequence of budgeting. This means the *steady-state* cost of a
   settled civilization is small regardless of how much simulation state
   it's accumulated.

## Where the real O(n) costs live, and how they're bounded

- **Villager memory** (`memory.VillagerMemory`): every list-shaped category
  (trades, danger, raids, deaths, weather, routes) is a `util.BoundedList`
  with a fixed cap. A villager that lives for a world's entire lifetime
  cannot accumulate unbounded memory — old entries are evicted, not kept
  forever.
- **Resource/economy ledgers**: fixed-size (one entry per `ResourceType`,
  currently 13), never grow.
- **Reputation ledgers**: one entry per player who has ever interacted with
  a civilization — bounded by real player count, not simulation time.
- **Incidents**: pruned every evaluation cycle (`IncidentTriggerService.expireIncidents`);
  never more than a handful active per civilization at once.

## Instrumentation

`util.PerformanceProfiler` records total/avg/max duration and call count
for `TaskScheduler.tick` and `ConstructionExecutor.tick` — the two systems
that run every server tick unconditionally, and therefore the two most
likely to matter if TPS drops. View the report with `/civilization debug`
(also shows the target civilization's current needs, job distribution,
active incidents, and economy state, in case a performance issue is
actually a simulation-state issue rather than a raw-timing one).

This is intentionally lightweight (no call-tree, no sampling) — see
`PerformanceProfiler`'s own Javadoc for why a full profiling library wasn't
pulled in for this.

## Stress-testing checklist

Not automated in this repository (no CI/benchmark harness is included),
but the architecture was built to make manual stress testing meaningful:

- **Population**: spawn/breed villagers until a civilization exceeds
  `populationCapPerVillage`; confirm `TaskScheduler`'s budget correctly
  spreads evaluation across ticks rather than evaluating everyone at once.
- **Many civilizations**: found civilizations up to `maxVillagesPerServer`;
  confirm inactive ones (no nearby player) fall back to the LOD interval.
- **Construction-heavy**: trigger many simultaneous building/road jobs
  (e.g. across several civilizations at once) and confirm
  `maxBlockPlacementsPerTick` caps total placement regardless of how many
  jobs are queued.
- **Save/load**: save and reload a world with populated civilizations and
  confirm construction resumes exactly where it left off —
  `BuildingConstructionSite`/`RoadConstructionSite` persist their exact
  remaining block queue, not just "in progress."
- **Unloaded chunks**: start a construction job, then unload its chunk
  (leave the area) — confirm it neither progresses nor loses queued blocks
  until the chunk loads again.
