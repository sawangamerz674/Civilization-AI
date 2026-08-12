# Configuration Manual

A single COMMON config file is generated on first launch at
`config/civilizationai-common.toml`. Every value below has an in-file
comment too — this page groups them by what they're for, since the TOML
file itself groups them by section rather than by "why would I change
this."

## If a village feels wrong, start here

| Symptom | Try |
|---|---|
| Villages staff up too slowly | Lower `simulation.simulationTicksPerEvaluation` |
| Villages build too many houses too fast | Raise `building.housingCapacityPerHouse` |
| Server lags during construction | Lower `performance.maxBlockPlacementsPerTick` |
| Server lags with many civilizations | Lower `performance.maxVillagersProcessedPerTick`, raise `performance.inactiveVillageSimulationInterval` |
| Roads aren't appearing | Lower `roads.roadPavingTrafficThreshold`, check `systems.roadGenerationEnabled` |
| Too many/few incidents | Adjust the per-type chances hardcoded in `IncidentTriggerService` (not currently exposed as config — see note below) |
| A whole subsystem is causing problems | Every subsystem has an on/off switch — see the table below |

## System toggles (`systems`)

Every major subsystem can be independently disabled. Disabling one never
crashes another — see `ai.CivilizationBrain`'s Javadoc for the exact
per-cycle order and which toggle gates which step.

| Toggle | Disables |
|---|---|
| `economyEnabled` | Price multiplier computation and its feedback into job priority (production itself keeps running) |
| `warSystemEnabled` | Hostile-mob detection and BANDIT_RAID triggering |
| `roadGenerationEnabled` | New road construction (existing roads are unaffected) |
| `technologyEnabled` | Technology tier advancement |
| `jobAssignmentEnabled` | New job assignments (existing jobs are unaffected; production keeps running) |
| `buildingEnabled` | New building construction |
| `reputationEnabled` | Reputation-affecting event recording (trading, attacking, defending) |
| `relationshipSimulationEnabled` | Villager-to-villager relationship interactions |
| `migrationEnabled` | Villagers migrating between civilizations |
| `incidentsEnabled` | All dynamic incidents (famine, drought, disease, fire, festivals, ...) |

## Performance (`performance`)

These are the knobs that most directly protect TPS on a large or
long-running server — see `docs/PERFORMANCE.md` for how they interact.

- `maxVillagersProcessedPerTick` — AI evaluation budget per tick
- `inactiveVillageSimulationInterval` — LOD interval for civilizations with no nearby player
- `maxVillagesPerServer` — hard cap on tracked civilizations
- `maxBlockPlacementsPerTick` — construction/road block-placement budget per tick, shared globally

## Economy (`economy`)

- `resourceStorageCapacity` — base per-resource cap before a WAREHOUSE raises it
- `warehouseCapacityBonus` — flat bonus per completed WAREHOUSE
- `warehouseTriggerStock` — total stock that makes a civilization want another WAREHOUSE

## Building (`building`)

- `housingCapacityPerHouse`, `buildingSiteSearchRadius`, `buildingSiteMaxVariance`,
  `buildingSiteBuffer`, `districtClusterRadius`

## Roads (`roads`)

- `roadPavingTrafficThreshold`, `roadMaxSlope`, `roadBridgeDropThreshold`,
  `roadMaxSlopeStoneRoadsBonus`

## Population (`population`)

- `civilizationClaimRadius` — how far a new villager can be from an
  existing civilization's origin and still be claimed by it, rather than
  founding a new one
- `migrationMinImprovementBlocks` — minimum improvement before a villager migrates

## Reputation (`reputation`) / Defense (`defense`)

- `reputationTradeDelta`, `reputationAttackDelta`, `reputationDefendDelta`
- `defenseThreatRadius`, `defenseThreatThreshold`

## Not (yet) configurable

Incident trigger chances (drought/disease/fire probability per cycle),
incident durations, production yield amounts/chances per profession, and
building resource costs are constants in their respective classes
(`IncidentTriggerService`, `ProductionService`, `BuildingType`) rather than
config values. They were judged more like game-balance content — the kind
of thing that changes with playtesting feedback, not per-server
preference — than server-operator-facing knobs. If that judgment turns out
wrong for your use case, they're one `ForgeConfigSpec` entry away from
being exposed; nothing about their current placement is load-bearing.
