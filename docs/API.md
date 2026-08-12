# API

Civilization AI exposes two kinds of public surface for other mods: a
read-oriented **facade** for querying state, and a set of **events** posted
at the moments this mod's own systems act. Both live under
`MineGamer.civilizationai.api` and are the only packages other mods should
depend on — everything else (`domain`, `ai`, `world`, `entity`, `save`) is
this mod's internal implementation and can change shape between versions
without notice.

To use either, add a compile-time dependency on this mod's jar (standard
Forge inter-mod dependency setup — no special API artifact is published
separately).

## Reading state: `CivilizationAIApi`

```java
import MineGamer.civilizationai.api.CivilizationAIApi;

Optional<Civilization> nearest = CivilizationAIApi.getNearestCivilization(level, pos, 128);
Optional<Civilization> owner = CivilizationAIApi.getCivilizationOf(level, villagerUuid);
int reputation = CivilizationAIApi.getReputation(level, civilizationId, playerUuid);
Technology tier = CivilizationAIApi.getTechnologyTier(level, civilizationId);
CivilizationAIApi.triggerIncident(level, civilizationId, IncidentType.BANDIT_RAID);
```

Every method takes a `ServerLevel` and internally resolves the right
underlying data store regardless of which dimension you pass — you never
need to know that civilization data is centrally stored (see
`SaveManager`'s Javadoc).

`Civilization` itself (the object these methods return) exposes read-only
accessors: `getId()`, `getName()`, `getOrigin()` (a `GlobalPos`),
`getPopulation()`, `getVillagerIds()`. Treat it as a snapshot — don't hold
a reference across ticks; re-fetch it.

## Reacting to events: `MineGamer.civilizationai.api.event`

All events extend `CivilizationEvent` (which carries `getCivilizationId()`)
and are posted to `MinecraftForge.EVENT_BUS`. None are cancelable — they
announce something that already happened.

| Event | Posted when | Extra fields |
|---|---|---|
| `CivilizationCreatedEvent` | A civilization is auto-founded by a villager with no nearby claim | `getName()`, `getOrigin()` |
| `VillagerRegisteredEvent` | A villager joins or migrates into a civilization | `getVillagerId()` |
| `BuildingCompletedEvent` | A building finishes construction | `getBuildingType()`, `getOrigin()` |
| `RoadCompletedEvent` | A road finishes construction | `getStart()`, `getEnd()` |
| `IncidentTriggeredEvent` | A dynamic incident (famine, bandit raid, ...) starts | `getIncidentType()` |
| `TechnologyUnlockedEvent` | A civilization advances a technology tier | `getTier()` |

Subscribe the normal Forge way:

```java
@SubscribeEvent
public static void onBuildingCompleted(BuildingCompletedEvent event) {
    if (event.getBuildingType() == BuildingType.TEMPLE) {
        // react — grant an achievement, spawn a decoration, whatever your mod does
    }
}
```

This is this mod's answer to the spec's "register AI behaviors" — rather
than a behavior-tree plugin system, other mods observe these lifecycle
moments and layer their own reactions on top, with no source changes to
Civilization AI required.

## What's *not* implemented: registries for buildings/jobs/resources/technologies

The spec's API section also asks for other mods to be able to **register**
new buildings, jobs, resources, technologies, and civilizations — i.e.
extend the fixed sets this mod ships with, not just react to them.

This is honestly not implemented. `Profession`, `BuildingType`,
`ResourceType`, `IncidentType`, and `Technology` are plain Java enums,
chosen throughout Phases 3–6 specifically because a fixed, well-understood
set was enough to build a complete simulation without the added complexity
of registry plumbing at every use site (job assignment, production yields,
NBT serialization, needs evaluation, and more all switch on these enums
directly).

Converting any one of them to a proper Forge `IForgeRegistry`-backed type
is real, substantial work — the mod ID/type pair replacing an enum
constant, `RegistryEvent.Register` firing at the right point in the mod
lifecycle, and every `switch`/`Map<EnumType, ...>` in the codebase (there
are dozens) becoming a `Map<RegistryObject, ...>` or datapack-driven table
instead. That's a project-sized change in its own right, not something to
retrofit as a Phase 7 afterthought. If a future version of this mod takes
that on, `BuildingType`'s Javadoc (which already flags this limitation) is
the place that conversion would start.

`domain.jobs`/`domain.building` etc. staying enum-based for now is a
deliberate, documented scope boundary — consistent with how every other
phase of this mod has handled a limitation it couldn't fully solve.
