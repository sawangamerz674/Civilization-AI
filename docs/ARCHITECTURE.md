# Architecture — Phase 1

## Layering

```
CivilizationAI (bootstrap)
   │
   ├── registry.ModRegistries      (DeferredRegisters — content, empty in Phase 1)
   ├── config.ModConfig            (tunable values, read by every later subsystem)
   ├── event.ModEventBusSubscriber (mod-bus lifecycle: common setup)
   ├── event.ForgeEventSubscriber  (Forge-bus lifecycle: server start/stop, tick hook)
   │        │
   │        ├── save.SaveManager → save.CivilizationSavedData (+ SaveMigrator)
   │        └── (Phase 3) task scheduler, invoked from the tick hook
   │
   └── network.NetworkHandler → network.packets.*
```

## Design principles carried forward from Phase 1

1. **One DeferredRegister owner.** `ModRegistries` is the only class that
   creates `DeferredRegister` instances. Every future phase that adds a
   block, item, block entity, or creative tab adds an entry there — never
   creates a new register elsewhere. This prevents duplicate/mismatched
   registry names.

2. **Config is read, never duplicated.** `ModConfig.COMMON` is the single
   source of truth for tunables. Subsystems read from it directly instead
   of caching their own copies of a value, so a config reload (Forge
   supports live-reloading COMMON config) is always reflected.

3. **Save data is schema-versioned from day one.** `CivilizationSavedData`
   stores `SchemaVersion` in its root tag; `SaveMigrator` is the only place
   that inspects and upgrades an old version. Every future phase that adds
   a persisted field bumps `Constants.SAVE_DATA_VERSION` and adds one
   `migrateVxToVy` step — old saves are never silently dropped.

4. **Networking is centrally registered.** `NetworkHandler.register()` is
   the only place packet IDs are assigned (sequentially, via `nextId()`).
   This guarantees client and server agree on IDs without manual bookkeeping.

5. **Logging is namespaced per subsystem.** `ModLogger.get("Save")`,
   `ModLogger.get("Network")`, etc. — this keeps log filtering easy on
   large servers (`forge.logging.markers` / log4j filters) and is the
   pattern every future phase (AI, Economy, Pathfinding, ...) should follow
   rather than calling `LoggerFactory.getLogger` ad hoc.

6. **Server-authoritative simulation.** All simulation state (Phase 2+)
   lives in `CivilizationSavedData` on the server. Nothing in Phase 1
   assumes client-side simulation; the `NetworkHandler` exists specifically
   because clients will need to be *sent* state, never compute it
   themselves. This is what "multiplayer compatible, server authoritative"
   means concretely at the code level.

7. **Tick budgeting hook exists before it's needed.** `ForgeEventSubscriber
   .onServerTick` is intentionally a no-op stub in Phase 1. It exists now so
   that Phase 3's scheduler has exactly one integration point to fill in,
   rather than requiring new event-bus wiring later.

## What Phase 1 deliberately does NOT contain

- No `Civilization`, `Village`, `Villager` domain model — that's Phase 2.
- No AI/behavior trees, no task scheduler — Phase 3.
- No resources/storage/economy — Phase 4.
- No building planner, terrain analysis, roads — Phase 5.
- No population, relationships, reputation, events, technology, war — Phase 6.
- No commands, public API for other mods, or profiling tooling — Phase 7.

Each of those phases builds strictly on top of the plumbing above without
modifying it, aside from `ModRegistries` gaining entries and
`CivilizationSavedData`/`SaveMigrator` gaining serialized fields.

## Phase 2 additions

```
save.CivilizationSavedData
   └── domain.CivilizationManager        (persistence-agnostic, plain Java)
          ├── domain.Civilization         (id, name, origin GlobalPos, population)
          ├── domain.VillagerProfile      (villager id, civ id, PersonalityProfile)
          │      └── domain.PersonalityProfile (← domain.PersonalityGenerator)
          └── memory.VillagerMemory       (relationships, reputation, locations,
                                            routes, trades, danger, raids, deaths, weather)

save.serializers.CivilizationManagerSerializer
   ├── CivilizationSerializer
   ├── VillagerProfileSerializer
   └── VillagerMemorySerializer  (NbtIoUtil shared by all three)
```

Principles established here that later phases must keep following:

1. **Domain stays persistence-agnostic.** Nothing under `domain/` or
   `memory/` imports NBT, `SavedData`, or any Forge networking type. Only
   `save.serializers.*` knows how to turn these objects into bytes. This
   means Phase 3's AI code can unit-test against `CivilizationManager`
   directly without spinning up a Minecraft server.

2. **One dirty-marker, not many `setDirty()` calls scattered around.**
   `CivilizationManager` takes a single `Runnable` at construction and
   calls it from every mutating method. Subsystems that mutate
   civilization state never need to remember to mark anything dirty
   themselves — creating a civilization, registering a villager, or
   recording a memory entry all do it automatically.

3. **Unbounded-looking lists are bounded at the data-structure level.**
   `util.BoundedList<T>` is the only way `VillagerMemory` stores
   trades/danger/raids/deaths/weather/routes. A category's cap is a
   private constant in `VillagerMemory`, not something call sites can
   accidentally bypass.

4. **Package-private mutation, public read.** `Civilization.addVillager`/
   `removeVillager` and `VillagerProfile.setCivilizationId` are
   package-private — only `CivilizationManager` (same package) can call
   them, so population/assignment consistency can't be violated from
   outside the manager.

## Phase 3 additions

```
event.ForgeEventSubscriber.onServerTick
   └── ai.TaskScheduler (singleton; per-civilization interval + budget tracking)
          └── ai.CivilizationBrain.evaluate(civilization, manager, gameTime, random)
                 └── ai.VillageGovernor.evaluate(...)
                        ├── domain.needs.NeedsEvaluator            (interface)
                        │      └── domain.needs.JobRatioNeedsEvaluator  (impl)
                        │             → List<domain.needs.NeedScore>, most urgent first
                        └── ai.DepartmentManager                   (interface)
                               └── ai.ProfessionDepartmentManager  (impl)
                                      └── domain.JobAssignmentService
                                             → domain.CivilizationManager.assignProfession(...)
                                                    → domain.VillagerProfile.profession
```

This is the AI hierarchy from the original design doc realized directly in
code: Civilization Brain → Village Governor → Department Managers →
Individual Villagers.

Principles established here that later phases must keep following:

1. **Needs evaluation is pluggable.** `NeedsEvaluator` and `DepartmentManager`
   are both interfaces; `CivilizationBrain` is the only place their concrete
   implementations are chosen. Phase 4's economy can introduce a richer
   `NeedsEvaluator` that reads real resource stock instead of population
   ratios without anything in `ai.TaskScheduler` or `ai.VillageGovernor`
   changing.

2. **One need acted on per evaluation cycle, not a full sweep.** This is
   deliberate pacing, not a limitation — see the Javadoc on
   `VillageGovernor`. It's what makes staffing look like gradual, emergent
   growth instead of villages snapping to target ratios instantly.

3. **Job assignment never poaches.** `JobAssignmentService` only ever pulls
   from villagers currently on `Profession.NONE`. Reassigning an already-
   employed villager (e.g. because of a resource shortage) is intentionally
   left for a later phase once there's a real reason (Phase 4 economy) to
   justify disrupting an existing assignment.

4. **LOD is a first-class scheduling concern, not an afterthought.**
   `ActivityTracker` + the two-tier interval in `TaskScheduler`
   (`simulationTicksPerEvaluation` vs `inactiveVillageSimulationInterval`)
   exist from the first tick any AI code runs, not bolted on once
   performance became a problem.

5. **Scope boundary: no entity integration yet.** Nothing in this phase
   spawns, detects, or listens for real `Villager` entities, and nothing
   creates a `Civilization` in response to gameplay — `TaskScheduler` simply
   iterates whatever civilizations already exist in
   `CivilizationManager`. Wiring actual villager entities to found and join
   civilizations is Phase 6 (population growth) territory; Phase 3 only
   guarantees that once a civilization and its villagers exist in the data
   model, they behave correctly.

## Phase 4 additions

```
ai.CivilizationBrain.evaluate
   ├── domain.resource.ProductionService.produce(...)        (always runs)
   │      → domain.resource.ResourceLedger (per-civilization stock + reservations)
   ├── domain.economy.EconomyService.updatePrices(...)        (if economyEnabled)
   │      → domain.economy.EconomyLedger (per-civilization price multipliers)
   └── ai.VillageGovernor.evaluate(...)                       (if jobAssignmentEnabled)
          └── domain.economy.EconomyAwareNeedsEvaluator        (wraps Phase 3's evaluator)
                 ├── domain.needs.JobRatioNeedsEvaluator        (delegate — unchanged)
                 └── domain.economy.ResourceNeedLinkage         (scarcity → need pressure)

save.serializers.CivilizationManagerSerializer  (now 5 parallel lists)
   ├── CivilizationSerializer / VillagerProfileSerializer / VillagerMemorySerializer  (unchanged)
   ├── ResourceLedgerSerializer   (new)
   └── EconomyLedgerSerializer    (new)
```

Principles established here that later phases must keep following:

1. **Ledgers are civilization-keyed, not villager-keyed.** `ResourceLedger`
   and `EconomyLedger` live in `CivilizationManager`'s own maps alongside
   `civilizations`, distinct from the villager-keyed `villagerProfiles`/
   `villagerMemories` maps from Phase 2. Anything that's a property of the
   civilization as a whole (not of one villager) belongs in this map shape.

2. **Reserve, don't withdraw, for anything a later system will "spend."**
   `ResourceLedger.reserve`/`commitReservation`/`releaseReservation` exist
   specifically so Phase 5's building planner can hold materials against a
   construction site without another system spending them first, and can
   cleanly give them back if a build is cancelled. `getAvailable()` — stock
   minus reserved — is the number every future consumer should check, not
   raw stock.

3. **Mutable-by-reference ledgers need an explicit dirty marker.** Unlike
   `VillagerProfile`/`Civilization` mutations (which always go through a
   `CivilizationManager` method that calls the dirty marker itself),
   `ResourceLedger`/`EconomyLedger` are handed out by reference and mutated
   directly by `ProductionService`/`EconomyService` for performance (no
   per-unit-produced manager round-trip). `CivilizationManager#markDirty()`
   exists for exactly this case — anything that mutates a ledger obtained
   from the manager must call it afterward. `CivilizationBrain` does this
   once per evaluation cycle rather than once per resource.

4. **Config toggles gate the smallest meaningful unit of behavior.**
   `jobAssignmentEnabled` now gates only the assignment step; production
   and economy still run when it's off. This was a deliberate fix versus
   Phase 3's first draft (which gated the entire evaluation cycle) once
   there was a second, independent behavior sharing the same cycle.

5. **Economy is additive, never required.** `EconomyAwareNeedsEvaluator`
   degrades to exactly its delegate's behavior when `economyEnabled` is
   off or no `EconomyLedger` exists yet. Nothing downstream needs to know
   whether economy influenced a given `NeedScore`.

6. **Scope boundary: no physical transport, no crafted-item tracking.**
   Resources move from "produced" to "in storage" abstractly — no villager
   entity carries anything, no block inventory exists. `Profession`'s fixed
   enum (see its Javadoc) also means BLACKSMITH consumes IRON/COAL without
   depositing a tracked "tools" output; a registry-based profession/item
   API is Phase 7 territory. Both are documented gaps, not oversights.

## Phase 5 additions

```
ai.CivilizationBrain.evaluate                    (still on the LOD-aware evaluation cadence)
   ├── ai.BuildingPlanner.evaluate                (queues at most 1 new building per civ)
   │      ├── world.SiteSelector → world.TerrainAnalyzer   (survey + flatness/fluid checks)
   │      ├── domain.building.BuildingBlueprintGenerator   (procedural, no schematics)
   │      └── → domain.building.BuildingConstructionSite   (queued, not yet placed)
   └── ai.RoadPlanner.evaluate                    (queues at most 1 new road per civ)
          ├── reads memory.VillagerMemory → TravelRoute (Phase 2's traffic data, finally consumed)
          ├── world.RoadPathGenerator             (ground-following; refuses if too steep)
          └── → domain.road.RoadConstructionSite  (queued, not yet placed)

event.ForgeEventSubscriber.onServerTick           (every tick, unconditionally)
   └── world.ConstructionExecutor.tick            (global per-tick block budget, all civs, all dimensions)
          ├── places BlockPlacements for every active ConstructionJob (buildings + roads)
          ├── skips any job whose chunk isn't loaded (no progress lost, just deferred)
          └── on a job's last placement → job.onComplete(manager, gameTime)
                 ├── BuildingConstructionSite → commits reserved cost, registers a Building
                 └── RoadConstructionSite → registers a RoadSegment

domain.building.DistrictClusterer                 (computed on demand, never persisted)
   → groups a civilization's Buildings into Districts by type + proximity
```

Principles established here that later phases must keep following:

1. **`domain`/`world` split is about who touches `ServerLevel`, not who's "pure."**
   Everything under `domain.building`/`domain.road`/`domain.construction` —
   including `BuildingBlueprintGenerator`, which does real procedural
   generation — stays free of any Minecraft world-state dependency, working
   only from a `TerrainSurvey` value object. `world` is the only package
   that imports `ServerLevel`. This is what makes `BuildingBlueprintGenerator`
   testable with a hand-built `TerrainSurvey` and no running game.

2. **Decide now, execute continuously.** `BuildingPlanner`/`RoadPlanner`
   run on the throttled AI evaluation cadence and only ever *queue* a job.
   `ConstructionExecutor` runs every tick unconditionally and is the only
   thing that ever calls `setBlock`. Never merge these — a planner that
   also executes would either place blocks at the slow AI cadence (looks
   broken) or need its own budgeting logic duplicated from the executor.

3. **One construction job at a time, per civilization, per kind.**
   `hasActiveBuildingSite`/`hasActiveRoadSite` gate new job creation the
   same way Phase 3's "one need per cycle" gates job assignment — gradual,
   inspectable progress instead of a civilization launching five
   simultaneous builds the instant five needs exist.

4. **A job's chunk being unloaded is not an error.** `ConstructionExecutor`
   skips a job entirely (never partially consumes its queue) when its
   origin chunk isn't loaded. Any future system that adds more job kinds
   must preserve this — losing queued placements because a player wasn't
   nearby would be a silent progress bug, not a performance trade-off.

5. **Districts are a view, not a store.** `DistrictClusterer` recomputes
   from `Building`s on demand and is never serialized. If a future phase
   needs districts to have their own persistent state (a name, a
   dedicated governor), that becomes a new class wrapping this computed
   view — the view itself should stay derived.

6. **Scope boundaries carried forward, stated plainly:** road pathing
   refuses steep routes rather than routing around them; roads have no
   resource cost; specialty buildings (WORKSHOP, GUARD_TOWER, FARM, TEMPLE,
   SCHOOL) cap at one per civilization; stairs/bridges use a fixed
   block/orientation rather than context-aware shapes. Each is a documented
   simplification with a clear "what a fuller version would do" left in the
   relevant class's Javadoc, not a silent gap.

## Phase 6 additions

```
entity.VillagerRegistrationService                (finally activates every prior phase in real gameplay)
   ├── handleVillagerJoin  → claims into nearest civ within radius, or founds a new one
   └── handleVillagerDeath → records DeathMemory into every civ-mate, unregisters

event.VillagerLifecycleEventHandler   (EntityJoinLevelEvent / LivingDeathEvent → the service above)
event.ReputationEventHandler          (LivingHurtEvent / TradeWithVillagerEvent / LivingDeathEvent(Monster))
   └── domain.reputation.ReputationService.recordEvent(...)
          └── domain.reputation.ReputationLedger (civilization-keyed, per-player)

ai.CivilizationBrain.evaluate         (extended — see this class's own Javadoc for full order)
   ├── domain.resource.ProductionService     (now incident-aware: DROUGHT/DISEASE reduce yields)
   ├── domain.incident.IncidentTriggerService.evaluate(...)     (expire + maybe trigger one, from real state)
   ├── ai.DefenseService.evaluate(...)                          (real Monster detection → triggerIncident(BANDIT_RAID))
   ├── domain.technology.TechnologyService.evaluate(...)        (advances ≤1 tier from real prosperity)
   ├── ai.RelationshipSimulator.evaluate(...)                   (1 interaction/cycle → VillagerMemory.relationships)
   ├── ai.MigrationService.evaluate(...)                        (resolves 1 real entity/cycle, may re-register)
   └── VillageGovernor.evaluate(..., NEEDS_EVALUATOR, ...)
          └── domain.incident.DefenseAwareNeedsEvaluator         (3rd decorator layer)
                 └── domain.economy.EconomyAwareNeedsEvaluator    (Phase 4, unchanged)
                        └── domain.needs.JobRatioNeedsEvaluator   (Phase 3, unchanged)
```

Principles established here that later phases must keep following:

1. **Entity integration lives in its own package, thin at the Forge-event edge.**
   `entity.VillagerRegistrationService` holds all the actual logic;
   `event.VillagerLifecycleEventHandler` does nothing but type-check and
   delegate. Any future entity hook (a future phase adding e.g. Iron Golem
   tracking) should follow this same split rather than putting logic
   directly in an `@SubscribeEvent` method.

2. **The evaluator decorator chain keeps growing without any layer knowing
   about the others.** `DefenseAwareNeedsEvaluator` wraps
   `EconomyAwareNeedsEvaluator` wraps `JobRatioNeedsEvaluator` — three
   phases, three classes, zero coupling between them beyond the shared
   `NeedsEvaluator` interface. `CivilizationBrain` is still the only place
   the chain is assembled. A future phase adding a fourth signal (say,
   morale) adds a fourth decorator here, not a modification to any
   existing one.

3. **"One thing per cycle" now spans nine independent systems.** Job
   assignment, building, roads, incidents, relationships, and migration
   each act on at most one thing per civilization per evaluation cycle.
   This is why a freshly founded civilization visibly grows over many
   cycles rather than fully resolving itself instantly — by now this is
   the mod's single most load-bearing design convention, not a
   phase-specific choice.

4. **Real detection feeds the incident system; the incident system feeds
   needs.** `DefenseService` doesn't boost SAFETY directly — it triggers a
   BANDIT_RAID incident, which `DefenseAwareNeedsEvaluator` reads
   independently. This indirection means any future system that wants to
   make a civilization more defensive (a scripted event, a command) only
   needs to call `IncidentTriggerService.triggerIncident(..., BANDIT_RAID, ...)`,
   not know anything about the needs system at all.

5. **`hasIncident` (no game-time) vs. `hasActiveIncident`/`getActiveIncidents`
   (game-time-checked) are different tools for different callers.** The
   former trusts that pruning already happened earlier in the same cycle
   (true for anything called from within `CivilizationBrain.evaluate`,
   after `IncidentTriggerService.evaluate` has run); the latter is
   authoritative and safe to call from anywhere, including a future
   command handler that isn't part of the per-cycle pipeline. Don't use
   `hasIncident` outside the per-cycle pipeline without re-checking this
   assumption still holds.

6. **Scope boundaries carried forward, stated plainly:** no "stealing"
   detection (no claimed-container concept exists); reputation's trade
   multiplier is computed but not wired into the live trade GUI; FIRE,
   HARVEST_FESTIVAL, MARKET_BOOM, and CIVIL_UNREST are informational
   incidents with no downstream mechanical effect yet; "build walls,"
   "patrol roads," "repair defenses," and "retreat civilians" from the
   spec's WAR SYSTEM are not implemented — all four need entity AI/pathing
   control that no phase of this mod has ever added. Each is documented in
   the relevant class's Javadoc, not silently missing.

## Phase 7 additions

```
command.CivilizationCommands                      (/civilization info|list|stats|reputation|debug|create|reset|export|import)
   ├── save.export.ExportService / ImportService   (reuse every existing serializer)
   └── util.PerformanceProfiler.formatReport()     (surfaced via /civilization debug)

api.CivilizationAIApi                              (read-oriented facade — the ONLY stable surface for other mods)
api.event.*                                        (posted from the exact moments they already happen)
   ├── entity.VillagerRegistrationService  → CivilizationCreatedEvent, VillagerRegisteredEvent
   ├── ai.MigrationService                 → VillagerRegisteredEvent (on migration)
   ├── world.ConstructionExecutor          → BuildingCompletedEvent, RoadCompletedEvent
   ├── notification.IncidentNotifier       → IncidentTriggeredEvent (+ chat broadcast to nearby players)
   └── ai.CivilizationBrain                → TechnologyUnlockedEvent (before/after tier comparison)

util.PerformanceProfiler                            (wraps TaskScheduler.tick / ConstructionExecutor.tick)
src/test/java/...                                   (real JUnit 5 tests — see docs/DEVELOPER_GUIDE.md)
```

Principles established here that close out the mod:

1. **`api` is the only package with a compatibility promise.** Every other
   package (`domain`, `ai`, `world`, `entity`, `save`, ...) is this mod's
   internal implementation, documented throughout as such, and free to
   change shape in a future version. `api.CivilizationAIApi` and
   `api.event.*` are the two things another mod should ever import. This
   is stated once, here and in `docs/API.md`, rather than repeated on
   every internal class — the boundary is the point.

2. **Events are posted from where the thing already happens, not from a
   new central dispatcher.** `BuildingCompletedEvent` is posted by
   `ConstructionExecutor` at the exact line a job completes;
   `TechnologyUnlockedEvent` is posted by `CivilizationBrain` at the exact
   point a tier changes. No new "event coordinator" class was introduced —
   doing so would have meant every event-producing site reporting to a
   middleman instead of just posting directly, for no benefit.

3. **Profiling wraps at the outermost, unconditional entry points.**
   `PerformanceProfiler` measures `TaskScheduler.tick` and
   `ConstructionExecutor.tick` — the two things that run every server tick
   no matter what — rather than instrumenting every individual planner.
   This answers the question that actually matters ("is AI or construction
   the one costing time this tick") without the overhead or noise of
   fine-grained per-method timing.

4. **Honest gap, stated once more, plainly:** the spec's "register
   buildings/jobs/resources/technologies" API surface is not implemented.
   `docs/API.md`'s closing section explains exactly what implementing it
   would require and why it wasn't attempted as part of this phase. Every
   other phase of this mod has had at least one boundary like this,
   documented rather than hidden; this is Phase 7's.
