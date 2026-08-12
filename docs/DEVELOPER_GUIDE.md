# Developer Guide

Start with `docs/ARCHITECTURE.md` — it has a per-phase "additions" section
with a package diagram and the specific design principles established at
each step. This page is the practical companion: where to actually put new
code, and how to run/test it.

## Package map

```
MineGamer.civilizationai
├── CivilizationAI              mod entry point
├── registry/                   DeferredRegisters (content registration point)
├── config/                     ModConfig — every tunable value
├── util/                       Constants, ModLogger, BoundedList, PerformanceProfiler
├── domain/                     pure data model + game-logic services — NO Level/NBT dependency
│   ├── needs/                  NeedType, NeedsEvaluator + JobRatioNeedsEvaluator
│   ├── resource/                ResourceType, ResourceLedger, ProductionService
│   ├── economy/                 EconomyLedger, EconomyService, EconomyAwareNeedsEvaluator
│   ├── building/                BuildingType, Building(ConstructionSite), blueprint generator, districts
│   ├── road/                    RoadSegment, RoadConstructionSite
│   ├── construction/             ConstructionJob/BlockPlacement/ConstructionStage (shared building+road primitives)
│   ├── reputation/               ReputationLedger, ReputationEvent, ReputationService
│   ├── incident/                 IncidentType, Incident, IncidentTriggerService, DefenseAwareNeedsEvaluator
│   └── technology/               Technology, TechnologyLedger, TechnologyService
├── memory/                      VillagerMemory and its sub-categories (Phase 2)
├── ai/                          per-cycle planners/services — CAN touch ServerLevel/MinecraftServer
│   (CivilizationBrain, VillageGovernor, BuildingPlanner, RoadPlanner, DefenseService,
│    RelationshipSimulator, MigrationService, TaskScheduler, ActivityTracker)
├── world/                       the only package that calls setBlock — TerrainAnalyzer, SiteSelector,
│                                RoadPathGenerator, BlockStateResolver, ConstructionExecutor
├── entity/                      real Villager entity integration — VillagerRegistrationService
├── event/                       thin Forge @SubscribeEvent listeners, delegate to entity/ai/world
├── notification/                 player-facing chat output (IncidentNotifier)
├── save/                        persistence — CivilizationSavedData, SaveManager, SaveMigrator
│   ├── serializers/              one (de)serializer class per persisted type
│   └── export/                   ExportService/ImportService
├── command/                      /civilization command tree
└── api/                          the ONLY packages other mods should depend on
    └── event/                     Forge events this mod posts
```

## The one rule that matters most: where does new code go?

Ask: **does this code need to read or write real Minecraft world state
(blocks, entities, chunks)?**

- **No** → `domain` (or a subpackage). Write it as plain Java operating on
  this mod's own data types (`Civilization`, `CivilizationManager`,
  `ResourceLedger`, ...). This is what makes the domain layer unit-testable
  without a running game — see `src/test/java` for real examples.
- **Yes, and it's a per-cycle decision** (should we build X, assign Y) →
  `ai`. These classes can take `ServerLevel`/`MinecraftServer` but should
  still delegate actual world mutation to `world`.
- **Yes, and it's actually placing/reading blocks** → `world`.
- **Yes, and it's about a real entity** (villager, player, mob) →
  `entity`, or a thin listener in `event` that delegates to a service
  class (never put logic directly in an `@SubscribeEvent` method — see
  `VillagerLifecycleEventHandler` for the pattern).

## Adding a new persisted field

1. Add the field to the relevant domain class (with a `reconstruct(...)`
   static factory if the class needs one for deserialization — follow the
   existing pattern in that package).
2. Update its serializer's `write`/`read` methods.
3. Bump `Constants.SAVE_DATA_VERSION`.
4. Add a `migrateVxToVy` step in `SaveMigrator` — even if it's a no-op
   (most are — see any existing one for the documentation pattern), this
   keeps the schema history one bump per change and makes intent explicit
   for the next person reading it.

## Adding a new civilization-keyed (not villager-keyed) map

`CivilizationManager` already has this pattern six times over (resource,
economy, reputation, technology ledgers; buildings; roads). Copy the
shape of the most similar existing one:
`getOrCreateXLedger`/`getXLedger`/`getAllXLedgers`, wired into `restore(...)`,
`removeCivilization`'s cleanup, and the serializer's write/read.

## Running tests

```
./gradlew test
```

`src/test/java` has real JUnit 5 tests against the domain layer — some
pure Java (`BoundedListTest`, `ResourceLedgerTest`, `TechnologyTest`), some
using vanilla Minecraft types like `RandomSource`/`GlobalPos` but no
running server (`PersonalityGeneratorTest`, `JobRatioNeedsEvaluatorTest`).
The latter compile and run against the same Forge/Minecraft dependency the
main source set uses — no separate test harness needed.

## Extending the mod yourself

See `docs/API.md` for the supported extension points (events + the read
API), and `docs/EXPANSION_GUIDE.md` for guidance on modifying this mod's
own source rather than building alongside it.
