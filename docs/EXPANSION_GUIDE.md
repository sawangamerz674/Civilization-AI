# Expansion Guide

Ideas for extending Civilization AI's own source, roughly ordered by how
much they change existing code.

## Low-risk additions (pure extension, no existing code changes)

- **A new `BuildingType`**: add an enum constant with footprint/cost/
  palette (see `BuildingType`'s existing 7 entries for the pattern), add a
  demand condition to `BuildingPlanner.determineDemand`, done. The
  blueprint generator, construction executor, and persistence all handle
  any `BuildingType` generically already.
- **A new `IncidentType`**: add the enum constant, a duration in
  `IncidentTriggerService.DURATION_TICKS`, and a trigger condition in
  `IncidentTriggerService.evaluate`. If it should have a mechanical effect,
  follow the pattern in `ProductionService` (checks `manager.hasIncident`)
  or `DefenseAwareNeedsEvaluator` (needs-priority adjustment).
- **A new `NeedsEvaluator` decorator**: wrap the existing chain in
  `CivilizationBrain` — see the Javadoc there on why this is safe (no
  decorator knows about any other).

## Medium-risk additions (touch a few existing classes)

- **A new persisted civilization-keyed ledger**: see
  `docs/DEVELOPER_GUIDE.md`'s section on this — it's a well-worn path by
  now (six examples to copy from).
- **Effects for the currently-informational technology tiers**
  (WATER_WELLS, LARGE_FARMS, WINDMILLS, MARKETS, WATCH_TOWERS,
  DEFENSIVE_WALLS, LIBRARIES): each just needs one system to check
  `manager.getTechnologyLedger(civId).hasUnlocked(Technology.X)` and adjust
  its behavior, the same way `RoadPlanner` already does for STONE_ROADS.
  `ProductionService` (WINDMILLS → yield bonus?), `ResourceLedger` capacity
  (WAREHOUSES tier, distinct from the WAREHOUSE *building* that already
  exists — could stack), and `EconomyService` (MARKETS → tighter price
  bands?) are natural candidates.

## Larger undertakings (the ones documented as out of scope)

- **Registry-based `Profession`/`BuildingType`/etc.** — see `docs/API.md`'s
  closing section for what this actually involves.
- **Entity AI/pathing control** (patrol roads, retreat civilians during a
  raid, physically walk to a workplace) — no phase of this mod ever
  overrides villager `Goal`/`Brain` behavior. Building this means learning
  Minecraft's villager AI system in depth and deciding how much of it to
  replace versus layer on top of; it's the single largest undertaking left
  on the table.
- **Trade price integration** — `ReputationService.getTradeMultiplier` is
  computed but not applied. Wiring it in means intercepting a villager's
  `MerchantOffer` cost before the trade GUI opens (a
  `VillagerTradesEvent`-adjacent hook, but for existing offers rather than
  registering new ones) — doable, just not done.
- **Districts with their own persistent identity** (a name, dedicated
  logic) rather than a computed clustering view — see `District`'s Javadoc
  for why this was a deliberate simplification and what wrapping it in a
  persisted type would look like.

## General guidance

- Read the Javadoc on whatever class you're touching before changing it —
  every "why" in this codebase lives there, not in a separate design doc
  that can drift out of sync.
- Preserve the pacing convention ("one thing per cycle") unless you have a
  specific reason not to — it's load-bearing for both perceived emergence
  and actual performance (see `docs/PERFORMANCE.md`).
- Preserve the `domain`/`ai`/`world` split (see `docs/DEVELOPER_GUIDE.md`)
  — it's what keeps the domain layer unit-testable.
